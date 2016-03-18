package controllers

import javax.inject.Inject
import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import reactivemongo.bson.{ BSONObjectID, BSONDocument }
import reactivemongo.core.actors.Exceptions.PrimaryUnavailableException
import reactivemongo.api.commands.WriteResult
import play.modules.reactivemongo.{
    MongoController,
    ReactiveMongoApi,
    ReactiveMongoComponents
}
import scala.util.Failure
import scala.util.Success
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.Future
import backend.CounterRepo

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
        userRepo.update(
                Json.obj(UserID -> id.toString()), 
                Json.obj("$set" -> request.body))

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
