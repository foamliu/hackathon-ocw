package controllers

import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsPath
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Reads
import play.api.libs.json.Reads.LongReads
import play.api.libs.json.Reads.StringReads
import play.api.libs.json.Reads.functorReads
import play.api.libs.json.Writes
import play.api.mvc.Controller
import play.api.mvc.Action
import play.api.Logger
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.ReactiveMongoComponents
import reactivemongo.bson.{ BSONObjectID, BSONDocument }
import play.modules.reactivemongo.json.collection._
import play.modules.reactivemongo.json.BSONFormats
import play.modules.reactivemongo.ReactiveMongoApi
import java.util.Date
import javax.inject.Inject
import reactivemongo.bson.BSONDocumentReader

case class Comment(itemID: Long, authorID: Long, authorName: String, posted: Date, text: String, like: Int)

object Comment {
    
    implicit val commentReads: Reads[Comment] = (
      (JsPath \\ "item_id").read[Long] and 
      (JsPath \\ "author_id").read[Long] and 
      (JsPath \\ "author_name").read[String] and 
      (JsPath \\ "posted").read[Date] and 
      (JsPath \\ "text").read[String] and
      (JsPath \\ "like").read[Int]
    )(Comment.apply _)
            
    implicit val commentWrites = new Writes[Comment] {
        def writes(c: Comment): JsValue = 
            Json.obj(
                "item_id" -> c.itemID,
                "author_id" -> c.authorID,
                "author_name" -> c.authorName,
                "posted" -> c.posted,
                "text" -> c.text,
                "like" -> c.like
            ) 
    } 
}

class Comments @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends Controller 
    with MongoController with ReactiveMongoComponents{
    // BSON-JSON conversions
    import play.modules.reactivemongo.json._
    
    def commentRepo = new backend.CommentMongoRepo(reactiveMongoApi)
    
    def add(id: Long) = Action(parse.json) {
        request =>
        {         
            val json: JsValue = request.body            
            val c = json.as[Comment]
            
            commentRepo
                .save(BSONDocument(
                        "_id" -> BSONObjectID.generate,
                        "item_id" -> c.itemID,
                        "author_id" -> c.authorID,
                        "author_name" -> c.authorName,
                        "posted" -> c.posted,
                        "text" -> c.text,
                        "like" -> c.like
                        ))
                
            Ok(json)
        }
    }
    
    def get(id: Long) = Action.async { implicit request =>
        val itemID: Long = id
        Logger.debug (itemID.toString())
        
        commentRepo.find(Json.obj("item_id" -> id))
            .map(comments => Ok(Json.toJson(comments.reverse)))
            
    }
    
    def like(id: String) = Action { 
        Ok(commentRepo.like(id))
    }
}