package controllers

import java.util.Date

import javax.inject.Inject
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsPath
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.Reads
import play.api.libs.json.Reads.LongReads
import play.api.libs.json.Reads.StringReads
import play.api.libs.json.Reads.functorReads
import play.api.libs.json.Writes
import play.api.mvc.Action
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.ReactiveMongoComponents
import play.modules.reactivemongo.json.BSONDocumentFormat
import play.modules.reactivemongo.json.BSONDocumentWrites
import play.modules.reactivemongo.json.JsObjectDocumentWriter
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.Producer.nameValue2Producer

case class Comment(itemID: Long, authorID: Long, authorName: String, posted: Date, text: String, timeline: Long, like: Int)

object Comment {
    
    implicit val commentReads: Reads[Comment] = (
        (JsPath \\ "item_id").read[Long] and
        (JsPath \\ "author_id").read[Long] and
        (JsPath \\ "author_name").read[String] and
        (JsPath \\ "posted").read[Date] and
        (JsPath \\ "text").read[String] and
        (JsPath \\ "timeline").read[Long] and
        (JsPath \\ "like").read[Int])(Comment.apply _)

    implicit val commentWrites = new Writes[Comment] {
        def writes(c: Comment): JsValue =
            Json.obj(
                "item_id" -> c.itemID,
                "author_id" -> c.authorID,
                "author_name" -> c.authorName,
                "posted" -> c.posted,
                "text" -> c.text,
                "timeline" -> c.timeline,
                "like" -> c.like)
    }   

}

class Comments @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends Controller
        with MongoController with ReactiveMongoComponents {
    // BSON-JSON conversions
    import play.modules.reactivemongo.json._

    def commentRepo = new backend.CommentMongoRepo(reactiveMongoApi)

    def add(id: Long) = Action(parse.json) {
        request =>
            {
                val json: JsValue = request.body
                val c = json.as[Comment]
                
                var bson: BSONDocument = json.as[BSONDocument];
                bson  ++= "_id" -> BSONObjectID.generate

                commentRepo.save(bson)

                Ok(Json.toJson(bson))
            }
    }

    def get(id: Long) = Action.async { implicit request =>
        val itemID: Long = id
        Logger.debug(itemID.toString())

        commentRepo.find(Json.obj("item_id" -> id))
            .map(comments => Ok(Json.toJson(comments)))

    }

    def like(id: String) = Action {
        Ok(commentRepo.like(id))
    }
}