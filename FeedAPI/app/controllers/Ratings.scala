package controllers

import java.util.Date

import javax.inject.Inject
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsObject
import play.api.libs.json.JsPath
import play.api.libs.json.JsValue
import play.api.libs.json.JsValue.jsValueToJsLookup
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.Reads
import play.api.libs.json.Reads.FloatReads
import play.api.libs.json.Reads.LongReads
import play.api.libs.json.Reads.functorReads
import play.api.libs.json.Writes
import play.api.mvc.Action
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.ReactiveMongoComponents

case class Rating(user_id: Long, item_id: Long, preference: Double, created_at: Date)

object Rating {

    implicit val courseReads: Reads[Rating] = (
        (JsPath \\ "user_id").read[Long] and
        (JsPath \\ "item_id").read[Long] and
        (JsPath \\ "preference").read[Double] and
        (JsPath \\ "created_at").read[Date])(Rating.apply _)

    implicit val courseWrites = new Writes[Rating] {
        def writes(r: Rating): JsObject =
            Json.obj(
                "user_id" -> r.user_id,
                "item_id" -> r.item_id,
                "preference" -> r.preference,
                "created_at" -> r.created_at)
    }   

}

class Ratings @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends Controller
        with MongoController with ReactiveMongoComponents {
    // BSON-JSON conversions
    import play.modules.reactivemongo.json._

    def ratingRepo = new backend.RatingRepo(reactiveMongoApi)

    def add(id: Long) = Action(parse.json) {

        val userID: Long = id
        Logger.debug(userID toString)

        request =>
            {
                val json: JsValue = request.body

                val user_id = (json \ "user_id").as[Long]
                val item_id = (json \ "item_id").as[Long]
                val pref = (json \ "pref").as[Float]
                Logger.info("%d,%d,%f".format(user_id, item_id, pref))

                val rating = Rating(user_id, item_id, pref, new Date())
                val jsonRating: JsObject = Json.toJson(rating).as[JsObject]
                ratingRepo.save(jsonRating)

                Ok(userID.toString)
            }
    }
}

