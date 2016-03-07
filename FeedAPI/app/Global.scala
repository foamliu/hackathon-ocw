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
      "userid" -> 88,
      "openid" -> "OPENID",
      "nickname" -> "foamliu",
      "sex" -> "1",
      "province" -> "Shanghai",
      "city" -> "Shanghai",
      "country" -> "CN",
      "headimgurl" -> "http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/46"
    ),
    Json.obj(
      "userid" -> 15,
      "openid" -> "OPENID",
      "nickname" -> "diyan",
      "sex" -> "1",
      "province" -> "Shanghai",
      "city" -> "Shanghai",
      "country" -> "CN",
      "headimgurl" -> "http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/46"
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
