package controllers

import scala.collection.JavaConversions.asScalaBuffer
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

import play.api.Logger
import play.api.Play
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.Reads.traversableReads
import play.api.mvc.Action
import play.api.mvc.Controller

object Application {

    private val howMany = 10
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

            courses = json.as[Seq[Course]]
        }

        courses
    }

    private def getRecommender(): Recommender = {

        if (recommender == null) {
            val model = new MongoDBDataModel(mongoHost.get, mongoPort.get, mongoDBName.get, "ratings", true, true, null)

            var similarity: UserSimilarity = new CachingUserSimilarity(new LogLikelihoodSimilarity(model), model)
            var neighborhood: UserNeighborhood = new NearestNUserNeighborhood(n, Double.NegativeInfinity, similarity, model, 1.0);

            recommender = new GenericBooleanPrefUserBasedRecommender(model, neighborhood, similarity)
        }

        recommender
    }

    private def recommend(userID: Long): List[Long] = {
        Logger.debug("userID=%d".format(userID))
        var list = List[Long]()

        try {
            var recommendations = getRecommender.recommend(userID, howMany)
            if (recommendations.size > 0)
            {
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
        val candidates: Seq[Course] = items.filter(i => itemIDs.contains(i.itemID))

        if (candidates.size > 0) {
            candidates
        } else {
            scala.util.Random.shuffle(items).take(howMany)
        }

    }
    
    def refresh() = {
        if (null != recommender)
        {
            val t0 = System.nanoTime()
            recommender.refresh(null);
            val t1 = System.nanoTime()

            Logger.info("Data model refreshment is done, elapsed time: %f sec, number of users: %ld, number of items: %ld.".format((t1 - t0) / 1000000000.0, recommender.getDataModel.getNumUsers, recommender.getDataModel.getNumItems))
        }
    }
}

class Application extends Controller {

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

}



