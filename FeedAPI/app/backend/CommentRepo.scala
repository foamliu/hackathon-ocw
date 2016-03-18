package backend

import scala.annotation.implicitNotFound
import scala.concurrent.Await
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import scala.concurrent.duration.Duration

import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.JsObjectDocumentWriter
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.ReadPreference
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONDocument
import reactivemongo.bson.BSONObjectID
import reactivemongo.bson.Producer.nameValue2Producer

class CommentMongoRepo(reactiveMongoApi: ReactiveMongoApi) {
    // BSON-JSON conversions
    import play.modules.reactivemongo.json._
    protected def collection = reactiveMongoApi.db.collection[JSONCollection]("comments")

    def find(selector: JsObject)(implicit ec: ExecutionContext): Future[List[JsObject]] =
        collection.find(selector).sort(Json.obj("like" -> -1)).cursor[JsObject](ReadPreference.Primary).collect[List](25)
        
    def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = collection.update(selector, update)

    def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] =
        collection.update(BSONDocument("_id" -> document.get("_id").getOrElse(BSONObjectID.generate)), document, upsert = true)
   
    def like(id: String)(implicit ec: ExecutionContext): JsObject = {
        val jsonFuture = collection.findAndUpdate(
            BSONDocument("_id" -> BSONObjectID(id)),
            BSONDocument("$inc" -> BSONDocument("like" -> 1)),
            fetchNewObject = true).map(_.value)

        val json: Option[JsObject] = Await.result(jsonFuture, Duration.Inf)

        json match {
            case Some(result) => result
            case None => Json.obj("result" -> "no match") 
        }
    }
}