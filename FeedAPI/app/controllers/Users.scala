package controllers

import javax.inject.Inject

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._ // JSON library

import reactivemongo.bson.{ BSONObjectID, BSONDocument }
import reactivemongo.core.actors.Exceptions.PrimaryUnavailableException
import reactivemongo.api.commands.WriteResult

import play.modules.reactivemongo.{
  MongoController, ReactiveMongoApi, ReactiveMongoComponents
}

class Users @Inject() (val reactiveMongoApi: ReactiveMongoApi)
    extends Controller with MongoController with ReactiveMongoComponents {
    
    import controllers.UserFields._
    
    def userRepo = new backend.UserMongoRepo(reactiveMongoApi)    
    
    def list = Action.async {implicit request =>
        userRepo.find()
            .map(users => Ok(Json.toJson(users.reverse)))
            .recover {case PrimaryUnavailableException => InternalServerError("Please install MongoDB")}
    }
    
    def add = Action.async(BodyParsers.parse.json) { implicit request =>
        val id = (request.body \ DeviceId).as[Long]
        val userid = (request.body \ UserID).as[String]

        userRepo.save(BSONDocument(
                DeviceId -> id,
                UserID -> userid
            )).map(le => Redirect(routes.Users.list()))
    }
    
    def update(id: String) = Action.async(BodyParsers.parse.json) { implicit request =>
        val openid = (request.body \ OpenID).as[String]
        val nickname = (request.body \ Nickname).as[String]
        val sex = (request.body \ Sex).as[String]
        val province = (request.body \ Province).as[String]
        val city = (request.body \ City).as[String]
        val country = (request.body \ Country).as[String]
        val headimgurl = (request.body \ HeadImgUrl).as[String]
        
        userRepo.update(BSONDocument(DeviceId -> BSONObjectID(id)), BSONDocument("$set" -> BSONDocument(
                OpenID -> openid,
                Nickname -> nickname,
                Sex -> sex,
                Province -> province,
                City -> city,
                Country -> country,
                HeadImgUrl -> headimgurl
            ))).map(le => Ok(Json.obj("success" -> le.ok)))
    
    }
}

object UserFields {
  val DeviceId = "_id"
  val UserID = "userid"
  val OpenID = "openid"
  val Nickname = "nickname"
  val Sex = "sex"
  val Province = "province"
  val City = "city"
  val Country = "country"
  val HeadImgUrl = "headimgurl"
}
