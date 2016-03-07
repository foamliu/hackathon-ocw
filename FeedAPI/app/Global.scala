import javax.inject.Inject

import play.api.Play.current
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.iteratee.Enumerator
import play.api.libs.json.Json
import play.api.{ Logger, Application, GlobalSettings }

import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection

class Global @Inject() (
  val reactiveMongoApi: ReactiveMongoApi) extends GlobalSettings {

  def collection = reactiveMongoApi.db.collection[JSONCollection]("users")

  val users = List(
    Json.obj(
      "text" -> "Have you heard about the Web Components revolution?",
      "username" -> "Eric",
      "avatar" -> "../images/avatar-01.svg",
      "favorite" -> false
    ),
    Json.obj(
      "text" -> "Loving this Polymer thing.",
      "username" -> "Rob",
      "avatar" -> "../images/avatar-02.svg",
      "favorite" -> false
    )
  )

  override def onStart(app: Application) {
    Logger.info("Application has started")

    collection.bulkInsert(users.toStream, ordered = true).
      foreach(i => Logger.info("Database was initialized"))
  }

  override def onStop(app: Application) {
    Logger.info("Application shutdown...")

    collection.drop().onComplete {
      case _ => Logger.info("Database collection dropped")
    }
  }
}
