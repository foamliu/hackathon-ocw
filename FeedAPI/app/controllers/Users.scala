package controllers

import scala.concurrent.Await
import scala.concurrent.Future
import scala.concurrent.duration.Duration

import backend.CounterRepo
import javax.inject.Inject
import play.api.Logger
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsObject
import play.api.libs.json.JsPath
import play.api.libs.json.JsValue
import play.api.libs.json.JsValue.jsValueToJsLookup
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.Reads
import play.api.libs.json.Reads.LongReads
import play.api.libs.json.Reads.functorReads
import play.api.libs.json.Writes
import play.api.mvc.Action
import play.api.mvc.BodyParsers
import play.api.mvc.Controller
import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.ReactiveMongoApi
import play.modules.reactivemongo.ReactiveMongoComponents
import reactivemongo.core.actors.Exceptions.PrimaryUnavailableException

case class User(userID: Long, deviceID: String, openID: String, nickname: String, sex: String, province: String, city: String, country: String, headimgurl: String)

object User {
    import controllers.UserFields._

    implicit val userReads: Reads[User] = (
        (JsPath \\ UserID).read[Long] and
        (JsPath \\ DeviceID).read[String] and
        (JsPath \\ OpenID).read[String] and
        (JsPath \\ Nickname).read[String] and
        (JsPath \\ Sex).read[String] and
        (JsPath \\ Province).read[String] and
        (JsPath \\ City).read[String] and
        (JsPath \\ Country).read[String] and
        (JsPath \\ HeadImgUrl).read[String])(User.apply _)

    implicit val userWrites = new Writes[User] {
        def writes(c: User): JsValue =
            Json.obj(
                UserID -> c.userID,
                DeviceID -> c.deviceID,
                OpenID -> c.openID,
                Nickname -> c.nickname,
                Sex -> c.sex,
                Province -> c.province,
                City -> c.city,
                Country -> c.country,
                HeadImgUrl -> c.headimgurl)
    }
}

class Users @Inject() (val reactiveMongoApi: ReactiveMongoApi)
        extends Controller with MongoController with ReactiveMongoComponents {

    import controllers.UserFields._

    def userRepo = new backend.UserMongoRepo(reactiveMongoApi)

    def list = Action.async { implicit request =>
        userRepo.list()
            .map(users => Ok(Json.toJson(users.reverse)))
            .recover { case PrimaryUnavailableException => InternalServerError("Please install MongoDB") }
    }
    
    def get(id: Long) = Action.async { implicit request =>
        userRepo.find(Json.obj(UserID -> id))
            .map(users => Ok(Json.toJson(users.reverse)))
            .recover { case PrimaryUnavailableException => InternalServerError("Please install MongoDB") }
    }

    def register = Action.async(BodyParsers.parse.json) { implicit request =>
        val deviceid = (request.body \ DeviceID).as[String]
        Logger.info(deviceid)
        
        var found: Boolean = false
        val userIDFuture: Future[Long] = userRepo
            .find(Json.obj(DeviceID -> deviceid))
            .map(users => users.size match {
                    case 0 => CounterRepo.nextID(reactiveMongoApi)
                    case _ => found = true; (users(0) \ UserID).as[Long] 
                })
        
        val userID: Long = Await.result(userIDFuture, Duration.Inf)
        Logger.info(userID toString)
        Logger.info(found toString)
        
        if (!found)
        {
            userRepo
                .save(Json.obj(
                    UserID -> userID,
                    DeviceID -> deviceid))
                .map(_ => Ok(Json.obj("userid" -> userID)))
        }
        else
        {
            Future(Ok(Json.obj("userid" -> userID)))
        }
    }

    def update(id: Long) = Action.async(BodyParsers.parse.json) { implicit request =>        
        userRepo.save(request.body.as[JsObject])

        Future(Ok(request.body))
    }
}

object UserFields {
    val UserID = "_id"
    val DeviceID = "deviceid"
    val OpenID = "openid"
    val Nickname = "nickname"
    val Sex = "sex"
    val Province = "province"
    val City = "city"
    val Country = "country"
    val HeadImgUrl = "headimgurl"
}
