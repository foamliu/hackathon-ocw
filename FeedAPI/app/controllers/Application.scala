package controllers

import scala.collection.JavaConversions.asScalaBuffer
import scala.collection.mutable.ListBuffer
import scala.concurrent.ExecutionContext.Implicits.global
import scala.io.Codec.string2codec
import scala.io.Source

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

    private val howMany = 20
    private val n = 2 // Nearest N User Neighborhood
    private val item_file = "app/assets/jsons/items.json"

    private val mongoHost = Play.current.configuration.getString("mongodb.ip")
    private val mongoPort = Play.current.configuration.getInt("mongodb.port")
    private val mongoDBName = Play.current.configuration.getString("mongodb.db")

    private var courses: Seq[Course] = null
    private var recommender: Recommender = null

    private def getCourses(): Seq[Course] = {

        if (courses == null) {
            val source: String = Source.fromFile(item_file)("UTF-8").getLines.mkString
            val json: JsValue = Json.parse(source)

            courses = json.as[Seq[Course]].filter(_.enabled)
        }

        courses
    }

    private def search(keyword: String): Seq[Course] = {
        getCourses().filter(_.title.contains(keyword)).take(howMany)
    }

    private def createNewRecommender: Recommender = {
        val model = new MongoDBDataModel(mongoHost.get, mongoPort.get, mongoDBName.get, "ratings", false, false, null)
        var similarity: UserSimilarity = new CachingUserSimilarity(new LogLikelihoodSimilarity(model), model)
        var neighborhood: UserNeighborhood = new NearestNUserNeighborhood(n, Double.NegativeInfinity, similarity, model, 1.0)
        new GenericBooleanPrefUserBasedRecommender(model, neighborhood, similarity)
    }

    private def getRecommender(): Recommender = {

        if (recommender == null) {
            recommender = createNewRecommender
        }

        recommender
    }

    private def recommend(userID: Long): List[Long] = {
        Logger.debug("userID=%d".format(userID))
        var list = List[Long]()

        try {
            var recommendations = getRecommender.recommend(userID, howMany)
            if (recommendations.size > 0) {
                Logger.info("userID=%d recommendations.size=%d".format(userID, recommendations.size))
            }

            for (r <- recommendations) list = r.getItemID :: list

        } catch {
            case nsue: NoSuchUserException => Logger.warn(nsue.getMessage)
            case e: Exception              => Logger.warn(e.getMessage)
        }

        list
    }

    private def getCandidates(userID: Long): Seq[Course] = {

        val items: Seq[Course] = getCourses
        val itemIDs: List[Long] = recommend(userID)
        var candidates: Seq[Course] = items.filter(i => itemIDs.contains(i.itemID))

        if (candidates.size == 0) {
            candidates = scala.util.Random.shuffle(items).take(howMany)
        }

        candidates
    }

    def refresh() = {
        if (null != getRecommender) {
            val t0 = System.nanoTime()
            recommender = createNewRecommender
            val t1 = System.nanoTime()

            Logger.debug("Data model refreshment is done, elapsed time: %f sec, number of users: %d, number of items: %d.".format((t1 - t0) / 1000000000.0, recommender.getDataModel.getNumUsers, recommender.getDataModel.getNumItems))
        }
    }

    // 标签列表排序：用户看过的以课程标签多到少排列，未看过的随机排列
    private def getTags(userID: Long, itemIDs: ListBuffer[Long]): Seq[(String, Int)] = {
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

        val interestedItems = items.filter(item => itemIDs.contains(item.itemID))
        val interested = scala.collection.mutable.Map[String, Int]() //用户学过课程的标签
        for (item <- interestedItems) {
            val tagList: Array[String] = item.tags.split(" ")
            for (tag <- tagList) {
                val number: Int = interested.getOrElse(tag, 0)
                interested.update(tag, number + 1)
            }
        }

        val tags = scala.collection.mutable.Map[String, Int]()
        for (tag <- allTags.keys) {
            val number: Int = interested.getOrElse(tag, 0)
            tags.update(tag, number)
        }

        val pairs: List[(String, Int)] = tags.toList sortBy { _._2 }
        val resultList: ListBuffer[(String, Int)] = new ListBuffer[(String, Int)]()
        for (tag <- pairs.reverse.map(_._1)) {
            resultList.append((tag, allTags.getOrElse(tag, 0)))
        }

        resultList.toSeq
    }

    private def getCandidatesByTag(userID: Long, tag: String): Seq[Course] = {
        val items: Seq[Course] = getCourses
        scala.util.Random.shuffle(items.filter(_.tags.contains(tag)).take(howMany))
    }
}

class Application @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends Controller
        with MongoController with ReactiveMongoComponents {

    def index = Action {
        Ok("Your new application is ready.")
    }

    def getCandidates(userID: Long) = Action {
        Logger.debug(userID toString)
        val candidates: Seq[Course] = Application.getCandidates(userID)
        val json: JsValue = Json.obj("courses" -> candidates)
        Ok(Json.stringify(json))
    }

    def addCrashReport = Action(parse.json) {
        request =>
            {
                val json: JsValue = request.body
                val jsonString: String = Json.stringify(json)
                Logger.warn(jsonString);
                Ok("Ok")
            }
    }

    def echo(message: String) = Action {
        Ok(message)
    }

    def search(keyword: String) = Action {
        val candidates: Seq[Course] = Application.search(keyword)
        val json: JsValue = Json.obj("courses" -> candidates)
        Ok(Json.stringify(json))
    }

    implicit def tuple2Writes[A, B](implicit a: Writes[A], b: Writes[B]): Writes[Tuple2[A, B]] = new Writes[Tuple2[A, B]] {
        def writes(tuple: Tuple2[A, B]) = JsArray(Seq(a.writes(tuple._1), b.writes(tuple._2)))
    }

    // BSON-JSON conversions
    import play.modules.reactivemongo.json._
    def ratingRepo = new backend.RatingRepo(reactiveMongoApi)
    private def getItemIDs(userID: Long): ListBuffer[Long] = {        
        val itemIDs: ListBuffer[Long] = new ListBuffer[Long]()
        ratingRepo.find(Json.obj("user_id" -> userID)).map(ratings => {
            for (rating <- ratings) {
                val itemID: Long = (rating \ "item_id").as[Long]
                itemIDs.+=(itemID) 
            }
        }).recover { case PrimaryUnavailableException => InternalServerError("Please install MongoDB") }

        itemIDs
    }

    def getTags(userID: Long) = Action {
        val itemIDs = getItemIDs(userID)
        val tags = Application.getTags(userID, itemIDs)
        val json: JsValue = Json.obj("tags" -> tags)
        Ok(Json.stringify(json))
    }

    def getCandidatesByTag(id: Long, tag: String) = Action {
        val candidates: Seq[Course] = Application.getCandidatesByTag(id, tag)
        val json: JsValue = Json.obj("courses" -> candidates)
        Ok(Json.stringify(json))
    }
}



