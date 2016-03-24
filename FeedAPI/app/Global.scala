import scala.concurrent.duration.DurationInt

import javax.inject.Inject
import play.api.Application
import play.api.GlobalSettings
import play.api.Logger
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.json.collection.JSONCollection

object Global extends GlobalSettings {

    override def onStart(app: Application) {

        play.api.Play.mode(app) match {
            case play.api.Mode.Test => // do not schedule anything for Test
            case _                  => refreshDaemon(app)
        }

    }

    def refreshDaemon(app: Application) = {
        Logger.info("Scheduling the data model refresh daemon")
        Akka.system(app).scheduler.schedule(0 seconds, 1 hours) {            
            controllers.Application.refresh()
        }
    }

}

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
            "headimgurl" -> "http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/46"),
        Json.obj(
            "userid" -> 15,
            "openid" -> "OPENID",
            "nickname" -> "diyan",
            "sex" -> "1",
            "province" -> "Shanghai",
            "city" -> "Shanghai",
            "country" -> "CN",
            "headimgurl" -> "http://wx.qlogo.cn/mmopen/g3MonUZtNHkdmzicIlibx6iaFqAc56vxLSUfpb6n5WKSYVY0ChQKkiaJSgQ1dZuTOgvLLrhJbERQQ4eMsv84eavHiaiceqxibJxCfHe/46"))

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
