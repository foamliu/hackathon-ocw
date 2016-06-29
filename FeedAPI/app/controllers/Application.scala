package controllers

import org.joda.time._
import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Codec.string2codec
import scala.io.Source
import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration
import org.apache.mahout.cf.taste.common.NoSuchUserException
import org.apache.mahout.cf.taste.impl.model.mongodb.MongoDBDataModel
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood
import org.apache.mahout.cf.taste.impl.recommender.GenericBooleanPrefUserBasedRecommender
import org.apache.mahout.cf.taste.impl.similarity.CachingUserSimilarity
import org.apache.mahout.cf.taste.impl.similarity.LogLikelihoodSimilarity
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood
import org.apache.mahout.cf.taste.recommender.Recommender
import org.apache.mahout.cf.taste.similarity.UserSimilarity
import javax.inject.Inject
import play.api.Logger
import play.api.Play
import play.api.Play.current
import play.api.libs.json.JsArray
import play.api.libs.json.JsValue
import play.api.libs.json.JsValue.jsValueToJsLookup
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.Reads.traversableReads
import play.api.libs.json.Writes
import play.api.mvc.Action
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.ReactiveMongoComponents
import reactivemongo.core.actors.Exceptions.PrimaryUnavailableException
import org.joda.time.format.DateTimeParser
import org.joda.time.format.DateTimeFormatter
import org.joda.time.format.DateTimeFormatterBuilder
import org.joda.time.format.DateTimeFormat

object Application {

  private val howMany = 20 // 每次推荐多少条
  private val n = 2 // Nearest N User Neighborhood
  private val item_file = "app/assets/jsons/items.json"
  private val course_file = "app/assets/jsons/courses.json"

  // MongoDB 相关参数
  private val mongoHost = Play.current.configuration.getString("mongodb.ip")
  private val mongoPort = Play.current.configuration.getInt("mongodb.port")
  private val mongoDBName = Play.current.configuration.getString("mongodb.db")

  private var items: Seq[Item] = null // 课程列表
  private var courses: Seq[Course] = null
  private var recommender: Recommender = null // 推荐器
  private var tags: Seq[(String, Int)] = null // 标签列表
  private var rates: Seq[Rating] = null
  private var ranks: Seq[(Long, Double)] = null // 评分列表
  private var visited: scala.collection.mutable.Map[Long, ListBuffer[Long]] = null

  lazy val reactiveMongoApi = current.injector.instanceOf[ReactiveMongoApi]
  def courseRepo = new backend.CourseRepo(reactiveMongoApi)
  def ratingRepo = new backend.RatingRepo(reactiveMongoApi)

  private def getItems(): Seq[Item] = {
    if (items == null) {
      items = loadItems
    }
    items
  }
  
  private def getCourses(): Seq[Course] = {
    if (courses == null) {
      courses = loadCourses
    }
    courses
  }

  private def getTags(userID: Long): Seq[(String, Int)] = {
    if (tags == null) {
      tags = calculateTags
    }
    tags
  }

  private def getRecommender(): Recommender = {
    if (recommender == null) {
      recommender = createNewRecommender
    }
    recommender
  }
  
  private def getRates(): Seq[Rating] = {
    if (rates == null) {
      rates = loadRates
    }
    rates
  }
  
  private def getRanks(): Seq[(Long, Double)] = {    
    if (ranks == null) {
      ranks = calculateRanks.sortBy(f => f._2).reverse
    }
    ranks
  }
  
  private def getVisited(): scala.collection.mutable.Map[Long, ListBuffer[Long]] = {
    if (visited == null) {
      visited = calculateVisited
    }
    visited
  }

  private def loadItems(): Seq[Item] = {
    try {
      val futureCourses: Future[JsArray] = courseRepo.list().map(courses => Json.arr(courses))
      val courses: JsArray = Await.result(futureCourses, Duration.Inf)
      //Logger.info("HEAD: " + courses.head.toString())
      courses.head.as[Seq[Item]].filter(_.enabled)
    } catch {
      case e: Exception =>
        Logger.warn(e.getMessage)
        val source: String = Source.fromFile(item_file)("UTF-8").getLines.mkString
        val json: JsValue = Json.parse(source)
        json.as[Seq[Item]].filter(_.enabled)
    }
  }
  
  private def loadCourses(): Seq[Course] = {
    var courses = new collection.mutable.ListBuffer[Course]();

    val lines = Source.fromFile(course_file)("UTF-8").getLines
    
    lines.foreach(line => {
      Logger.warn(line)
      courses.append(Json.parse(line).as[Course])
      
      }) 
    courses.toSeq
  }

  /*
     * 得到二元组列表: 标签 -> 出现次数。
     */
  private def calculateTags(): Seq[(String, Int)] = {
    val items: Seq[Item] = getItems

    val allTags = scala.collection.mutable.Map[String, Int]() //所有标签
    for (item <- items) {
      val tagList: Array[String] = item.tags.split(" ")
      for (tag <- tagList) {
        if (!tag.isEmpty()) {
          val number: Int = allTags.getOrElse(tag, 0)
          allTags.update(tag, number + 1)
        }
      }
    }

    allTags.toSeq
  }
  
  private def loadRates(): Seq[Rating] = {
    var rates: Seq[Rating] = null
    try {
      val futureRatings: Future[JsArray] = ratingRepo.list().map(ratings => Json.arr(ratings))
      val ratings: JsArray = Await.result(futureRatings, Duration.Inf)
      rates = ratings.head.as[Seq[Rating]]      
    } catch {
      case e: Exception =>
        Logger.warn(e.getMessage)
        e.printStackTrace()
    }
    
    rates
  }

  private def calculateRanks(): Seq[(Long, Double)] = {
    val allRanks = scala.collection.mutable.Map[Long, Double]()
    try {
      for (item <- getItems.filter(_.enabled)) {
        val id: Long = item.itemID
        val clicks: Int = getRates.filter(_.item_id == id).map(r => r.user_id).distinct.length
        val parsers  = Array( 
          DateTimeFormat.forPattern( "yyyy-MM-dd HH:mm" ).getParser(),
          DateTimeFormat.forPattern( "yyyy-MM-dd" ).getParser(),
          DateTimeFormat.forPattern( "yyyy-MM" ).getParser(),
          DateTimeFormat.forPattern( "yyyy-M" ).getParser()
          )
        val formatter : DateTimeFormatter = new DateTimeFormatterBuilder().append( null, parsers ).toFormatter()

        var d1: DateTime = formatter.parseDateTime("2000-01-01")
        try {
          d1 = formatter.parseDateTime(item.posted)
        } catch {
          case e: Exception => Logger.warn(e.getMessage)
        }
        val d2: DateTime = DateTime.now
        val hours = Hours.hoursBetween(d1, d2).getHours
        val rank: Double = (clicks + 1) / Math.pow(hours + 2, 0.01)
        allRanks.update(id, rank)
      }
    } catch {
      case e: Exception =>
        Logger.warn(e.getMessage)
        e.printStackTrace()
    }      
    allRanks.toSeq
  }
  
  private def calculateVisited(): scala.collection.mutable.Map[Long, ListBuffer[Long]] = {
    val visited = scala.collection.mutable.Map[Long, ListBuffer[Long]]()
    try {
      for (rate <- getRates) {
        var alist : ListBuffer[Long] = visited.getOrElse(rate.user_id, new ListBuffer[Long])
        alist.+= (rate.item_id)
        visited.update(rate.user_id, alist)
      }
    } catch {
      case e: Exception =>
        Logger.warn(e.getMessage)
        e.printStackTrace()
    }
    visited
  }

  private def createNewRecommender: Recommender = {
    val model = new MongoDBDataModel(mongoHost.get, mongoPort.get, mongoDBName.get, "ratings", false, false, null)
    var similarity: UserSimilarity = new CachingUserSimilarity(new LogLikelihoodSimilarity(model), model)
    var neighborhood: UserNeighborhood = new NearestNUserNeighborhood(n, Double.NegativeInfinity, similarity, model, 1.0)
    new GenericBooleanPrefUserBasedRecommender(model, neighborhood, similarity)
  }

  private def search(keyword: String): Seq[Item] = {
    getItems().filter(_.title.contains(keyword)).take(howMany)
  }

  /*
     * 进行推荐，得到课程ID的列表.
     */
  private def recommend(userID: Long): List[Long] = {
    //Logger.debug("userID=%d".format(userID))
    var list = List[Long]()

    try {
      var recommendations = getRecommender.recommend(userID, howMany)
      if (recommendations.size > 0) {
        Logger.info("Real recommendations: userID=%d, result size=%d.".format(userID, recommendations.size))
      }

      for (r <- recommendations) list = r.getItemID :: list

    } catch {
      case nsue: NoSuchUserException => Logger.warn(nsue.getMessage)
      case e: Exception              => Logger.warn(e.getMessage)
    }

    list
  }

  /*
     * 目前推荐栏会调用服务器这个方法。
     */
  private def getCandidates(userID: Long): Seq[Item] = {

    val items: Seq[Item] = getItems
    var itemIDs: Seq[Long] = recommend(userID)
    try {
      if (itemIDs.size == 0) {
        //candidates = scala.util.Random.shuffle(items).take(howMany)
        val myVisited = getVisited.getOrElse(userID, null)
        if (myVisited != null) {
          itemIDs = getRanks.filter(r => !myVisited.contains(r._1)).sortBy(f => f._2).reverse.take(100).map(f => f._1)
        } else {
          itemIDs = getRanks.sortBy(f => f._2).reverse.take(100).map(f => f._1)
        }
        
        itemIDs = scala.util.Random.shuffle(itemIDs).take(howMany)
        
      }
    } catch {
      case e: Exception =>
        Logger.warn(e.getMessage)
        e.printStackTrace()
    }
    
    var candidates: Seq[Item] = items.filter(i => itemIDs.contains(i.itemID))
    candidates
  }

  /*
     * 目前基于标签的推荐实际上是随机得出的。
     */
  private def getCandidatesByTag(userID: Long, tag: String): Seq[Item] = {
    val items: Seq[Item] = getItems
    scala.util.Random.shuffle(items.filter(_.tags.contains(tag))).take(howMany)
  }

  /*
     * 指定用户看过哪些课程，
     */
  private def getItemIDs(userID: Long): ListBuffer[Long] = {
    val itemIDs: ListBuffer[Long] = new ListBuffer[Long]()
    ratingRepo.find(Json.obj("user_id" -> userID)).map(ratings => {
      for (rating <- ratings) {
        val itemID: Long = (rating \ "item_id").as[Long]
        itemIDs.+=(itemID)
      }
    }).recover { case PrimaryUnavailableException => Logger.error("Please install MongoDB") }

    itemIDs
  }

  /*
     * 定时任务，每小时执行一次。
     */
  def refresh() = {
    if (null != getRecommender) {
      val t0 = System.nanoTime()
      //刷新推荐器
      recommender = createNewRecommender
      //刷新课程列表
      items = loadItems
      rates = loadRates
      ranks = calculateRanks
      visited = calculateVisited
      //刷新标签列表
      tags = calculateTags
      val t1 = System.nanoTime()

      Logger.debug("Data model refreshment is done, elapsed time: %f sec, number of users: %d, number of items: %d.".format((t1 - t0) / 1000000000.0, recommender.getDataModel.getNumUsers, recommender.getDataModel.getNumItems))
    }
  }
}

class Application @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends Controller
    with MongoController with ReactiveMongoComponents {

  def index = Action {
    //Ok("Your new application is ready.")
    Ok(views.html.index("学啥"))
  }

  /*
     * 内容原样返回，用于获取性能测试基线。
     */
  def echo(message: String) = Action {
    Ok(message)
  }

  /*
     * 为用户推荐课程，以JSON形式返回。
     */
  def getCandidates(userID: Long) = Action {
    //Logger.debug(userID toString)
    val candidates: Seq[Item] = Application.getCandidates(userID)
    val json: JsValue = Json.obj("courses" -> candidates)
    Ok(Json.stringify(json))
  }

  /*
     * 基于标签进行推荐。
     */
  def getCandidatesByTag(id: Long, tag: String) = Action {
    val candidates: Seq[Item] = Application.getCandidatesByTag(id, tag)
    val json: JsValue = Json.obj("courses" -> candidates)
    Ok(Json.stringify(json))
  }

  /*
     * 搜索标题中带有指定关键字的公开课。
     */
  def search(keyword: String) = Action {
    val candidates: Seq[Item] = Application.search(keyword)
    val json: JsValue = Json.obj("courses" -> candidates)
    Ok(Json.stringify(json))
  }

  implicit def tuple2Writes[A, B](implicit a: Writes[A], b: Writes[B]): Writes[Tuple2[A, B]] = new Writes[Tuple2[A, B]] {
    def writes(tuple: Tuple2[A, B]) = JsArray(Seq(a.writes(tuple._1), b.writes(tuple._2)))
  }
  /*
     * 获取所有公开课的标签，以二元组JSON形式返回，标签名字 -> 标签出现的次数。
     */
  def getTags(userID: Long) = Action {
    val tags = Application.getTags(userID)
    val json: JsValue = Json.obj("tags" -> tags)
    Ok(Json.stringify(json))
  }

  /*
     * 客户端汇报崩溃信息。
     */
  def addCrashReport = Action(parse.json) {
    request =>
      {
        val json: JsValue = request.body
        val jsonString: String = Json.stringify(json)
        Logger.warn(jsonString);
        Ok("Ok")
      }
  }
  
  def getCourseByItemHash(hash: String) = Action {
    val courses = Application.getCourses()
    Logger.warn(String.valueOf(courses.size))
    Ok("OK")
  }
}



