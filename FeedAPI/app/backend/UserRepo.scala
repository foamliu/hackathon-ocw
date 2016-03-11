package backend

import play.api.libs.json.{JsObject, Json}
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import reactivemongo.api.ReadPreference
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{BSONObjectID, BSONDocument}

import scala.concurrent.{ExecutionContext, Future}

trait UserRepo {
  def list()(implicit ec: ExecutionContext): Future[List[JsObject]]
  
  def find(query: JsObject)(implicit ec: ExecutionContext): Future[List[JsObject]]

  def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

  def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]
}

class UserMongoRepo(reactiveMongoApi: ReactiveMongoApi) extends UserRepo {
    // BSON-JSON conversions
    import play.modules.reactivemongo.json._

    protected def collection =
        reactiveMongoApi.db.collection[JSONCollection]("users")

    def list()(implicit ec: ExecutionContext): Future[List[JsObject]] =
        collection.find(Json.obj()).cursor[JsObject](ReadPreference.Primary).collect[List]()
    
    def find(query: JsObject)(implicit ec: ExecutionContext): Future[List[JsObject]] =
        collection.find(query).cursor[JsObject](ReadPreference.Primary).collect[List]()

    def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = collection.update(selector, update)

    def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = collection.remove(document)

    def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] =
        collection.update(BSONDocument("_id" -> document.get("_id").getOrElse(BSONObjectID.generate)), document, upsert = true)
}
