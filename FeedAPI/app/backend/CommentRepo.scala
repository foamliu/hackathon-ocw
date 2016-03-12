package backend

import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json.JsObject
import scala.concurrent.ExecutionContext
import scala.concurrent.Future
import reactivemongo.api.ReadPreference
import reactivemongo.bson.BSONDocument
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.BSONObjectID

class CommentMongoRepo(reactiveMongoApi: ReactiveMongoApi) {
    // BSON-JSON conversions
    import play.modules.reactivemongo.json._
    protected def collection = reactiveMongoApi.db.collection[JSONCollection]("comments")

    def find(selector: JsObject)(implicit ec: ExecutionContext): Future[List[JsObject]] =
        collection.find(selector).cursor[JsObject](ReadPreference.Primary).collect[List]()

    def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] =
        collection.update(BSONDocument("_id" -> document.get("_id").getOrElse(BSONObjectID.generate)), document, upsert = true)

}