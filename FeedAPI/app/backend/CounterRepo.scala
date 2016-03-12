package backend

import scala.concurrent.ExecutionContext
import play.modules.reactivemongo.json.collection.JSONCollection
import play.api.libs.json.JsObject
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.bson.BSONDocument

object CounterRepo {
    // BSON-JSON conversions
    import play.modules.reactivemongo.json._
    
    def nextID(reactiveMongoApi: ReactiveMongoApi)(implicit ec: ExecutionContext): Long = {
        def counters = reactiveMongoApi.db.collection[JSONCollection]("counters")

        val jsonFuture = counters.findAndUpdate(
            BSONDocument("_id" -> "userid"),
            BSONDocument("$inc" -> BSONDocument("seq" -> 1)),
            fetchNewObject = true).map(_.value)

        val json: Option[JsObject] = Await.result(jsonFuture, Duration.Inf)

        json match {
            case Some(json) => (json \ "seq").as[Long]
            case None       => 0
        }
    }
}