package backend

import scala.annotation.implicitNotFound
import scala.concurrent.ExecutionContext
import scala.concurrent.Future

import play.api.libs.json.JsObject
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.BSONObjectIDFormat
import play.modules.reactivemongo.json.JsObjectDocumentWriter
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.ReadPreference
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONObjectID

class RatingRepo(reactiveMongoApi: ReactiveMongoApi) {
      // BSON-JSON conversions
    import play.modules.reactivemongo.json._

    protected def collection = reactiveMongoApi.db.collection[JSONCollection]("ratings")
    
    def list()(implicit ec: ExecutionContext): Future[List[JsObject]] =
        collection.find(Json.obj()).cursor[JsObject](ReadPreference.Primary).collect[List]()

    def find(selector: JsObject)(implicit ec: ExecutionContext): Future[List[JsObject]] =
        collection.find(selector).cursor[JsObject](ReadPreference.Primary).collect[List]()

    def update(selector: JsObject, update: JsObject)(implicit ec: ExecutionContext): Future[WriteResult] = collection.update(selector, update)

    def remove(document: JsObject)(implicit ec: ExecutionContext): Future[WriteResult] = collection.remove(document)

    def save(document: JsObject)(implicit ec: ExecutionContext): Future[WriteResult] =
        collection.update(Json.obj("_id" -> BSONObjectID.generate), document, upsert = true)    
}