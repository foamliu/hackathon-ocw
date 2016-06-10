package controllers

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
    
object Application {
  
    private val howMany = 20 // 每次推荐多少条
    private val n = 2 // Nearest N User Neighborhood
    private val item_file = "app/assets/jsons/items.json"

    // MongoDB 相关参数
    private val mongoHost = Play.current.configuration.getString("mongodb.ip")
    private val mongoPort = Play.current.configuration.getInt("mongodb.port")
    private val mongoDBName = Play.current.configuration.getString("mongodb.db")

    private var courses: Seq[Course] = null  // 课程列表
    private var recommender: Recommender = null  // 推荐器
    private var tags: Seq[(String, Int)] = null  // 标签列表
    
    lazy val reactiveMongoApi = current.injector.instanceOf[ReactiveMongoApi]
    def courseRepo = new backend.CourseRepo(reactiveMongoApi)
    def ratingRepo = new backend.RatingRepo(reactiveMongoApi)
   
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
    
    private def loadCourses(): Seq[Course] = {
      try {
        //val source: String = Source.fromFile(item_file)("UTF-8").getLines.mkString
        //val json: JsValue = Json.parse(source)
        //json.as[Seq[Course]].filter(_.enabled)
        val futureCourses: Future[JsArray] = courseRepo.list().map(courses => Json.arr(courses))
        Logger.info(futureCourses.toString())
        val courses: JsArray = Await.result(futureCourses, Duration.Inf)
        Logger.info(courses.toString())
        courses.as[Seq[Course]].filter(_.enabled)
      } catch {
        case e: Exception              => Logger.warn(e.getMessage)
        e.printStackTrace()
        val source: String = Source.fromFile(item_file)("UTF-8").getLines.mkString
        val json: JsValue = Json.parse(source)
        json.as[Seq[Course]].filter(_.enabled)
      }
    }
    
    /*
     * 得到二元组列表: 标签 -> 出现次数。
     */
    private def calculateTags(): Seq[(String, Int)] = {
        val items: Seq[Course] = getCourses

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
    
    private def createNewRecommender: Recommender = {
        val model = new MongoDBDataModel(mongoHost.get, mongoPort.get, mongoDBName.get, "ratings", false, false, null)
        var similarity: UserSimilarity = new CachingUserSimilarity(new LogLikelihoodSimilarity(model), model)
        var neighborhood: UserNeighborhood = new NearestNUserNeighborhood(n, Double.NegativeInfinity, similarity, model, 1.0)
        new GenericBooleanPrefUserBasedRecommender(model, neighborhood, similarity)
    }

    private def search(keyword: String): Seq[Course] = {
        getCourses().filter(_.title.contains(keyword)).take(howMany)
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
    private def getCandidates(userID: Long): Seq[Course] = {

        val items: Seq[Course] = getCourses
        val itemIDs: List[Long] = recommend(userID)
        var candidates: Seq[Course] = items.filter(i => itemIDs.contains(i.itemID))

        if (candidates.size == 0) {
            candidates = scala.util.Random.shuffle(items).take(howMany)
        }

        candidates
    }

    /*
     * 目前基于标签的推荐实际上是随机得出的。
     */
    private def getCandidatesByTag(userID: Long, tag: String): Seq[Course] = {
        val items: Seq[Course] = getCourses
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
            courses = loadCourses
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
        val candidates: Seq[Course] = Application.getCandidates(userID)
        val json: JsValue = Json.obj("courses" -> candidates)
        Ok(Json.stringify(json))
    }

    /*
     * 基于标签进行推荐。
     */
    def getCandidatesByTag(id: Long, tag: String) = Action {
        val candidates: Seq[Course] = Application.getCandidatesByTag(id, tag)
        val json: JsValue = Json.obj("courses" -> candidates)
        Ok(Json.stringify(json))
    }

    /*
     * 搜索标题中带有指定关键字的公开课。
     */
    def search(keyword: String) = Action {
        val candidates: Seq[Course] = Application.search(keyword)
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
}



