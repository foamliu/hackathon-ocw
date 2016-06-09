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

class CourseRepo(reactiveMongoApi: ReactiveMongoApi) {
      // BSON-JSON conversions
    import play.modules.reactivemongo.json._

    protected def collection = reactiveMongoApi.db.collection[JSONCollection]("course")
    
    def list()(implicit ec: ExecutionContext): Future[List[JsObject]] =
        collection.find(Json.obj()).cursor[JsObject](ReadPreference.Primary).collect[List]()
}