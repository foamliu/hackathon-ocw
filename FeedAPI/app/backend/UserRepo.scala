package backend

import scala.concurrent.ExecutionContext
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.concurrent.duration._
import play.api.libs.json.JsObject
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json.Json
import reactivemongo.api.commands.WriteResult
import reactivemongo.bson.{BSONObjectID, BSONDocument}
import reactivemongo.bson.BSONDocumentReader
import reactivemongo.api.ReadPreference
import scala.util.Failure
import scala.util.Success
import scala.concurrent.Await

trait UserRepo {
    def list()(implicit ec: ExecutionContext): Future[List[JsObject]]

    def find(selector: JsObject)(implicit ec: ExecutionContext): Future[List[JsObject]]

    def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

    def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]

    def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult]
    
    def nextID()(implicit ec: ExecutionContext): Long
}

class UserMongoRepo(reactiveMongoApi: ReactiveMongoApi) extends UserRepo {
    // BSON-JSON conversions
    import play.modules.reactivemongo.json._

    protected def collection = reactiveMongoApi.db.collection[JSONCollection]("users")

    def list()(implicit ec: ExecutionContext): Future[List[JsObject]] =
        collection.find(Json.obj()).cursor[JsObject](ReadPreference.Primary).collect[List]()

    def find(selector: JsObject)(implicit ec: ExecutionContext): Future[List[JsObject]] =
        collection.find(selector).cursor[JsObject](ReadPreference.Primary).collect[List]()

    def update(selector: BSONDocument, update: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = collection.update(selector, update)

    def remove(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] = collection.remove(document)

    def save(document: BSONDocument)(implicit ec: ExecutionContext): Future[WriteResult] =
        collection.update(BSONDocument("_id" -> document.get("_id").getOrElse(BSONObjectID.generate)), document, upsert = true)

    def nextID()(implicit ec: ExecutionContext): Long = {    
        def counters = reactiveMongoApi.db.collection[JSONCollection]("counters")
       
        val jsonFuture = counters.findAndUpdate(
                BSONDocument("_id" -> "userid"), 
                BSONDocument("$inc" -> BSONDocument("seq" -> 1)),
                fetchNewObject = true
                ).map(_.value)
                
        val json: Option[JsObject] = Await.result(jsonFuture, Duration.Inf)
        
        json match {
            case Some(json) => (json \ "seq").as[Long]
            case None => 0
        }
    }

}
