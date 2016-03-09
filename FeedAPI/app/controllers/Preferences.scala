package controllers

import java.io._
import java.util._

import javax.inject.Inject

import scala.collection.JavaConversions._
import scala.io._

import play.api._
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._ // JSON library
import play.api.libs.json.Reads._ // Custom validation helpers
import play.api.libs.functional.syntax._ // Combinator syntax
import play.api.Logger
import play.api.Play.current

class Preferences extends Controller {
    
    import controllers.PrefFields._

    def add(id: Long) = Action(parse.json) {

        val userID: Long = id
        Logger.debug (userID toString)

        request =>
        {
            val json: JsValue = request.body
            
            val user_id = (json \ UserID).as[Long]
            val item_id = (json \ ItemID).as[Long]
            val pref = (json \ Preference).as[Float]
            Logger.info("%d,%d,%f".format(user_id,item_id,pref))
          
            val file = Application.getPrefFile
            val bw = new BufferedWriter(new FileWriter(file, true))
            bw.write("%d,%d,%f".format(user_id,item_id,pref))
            bw.newLine()
            bw.close()
          
            Ok(user_id.toString)
        }
    }

}

object PrefFields {
  val UserID = "user_id"
  val ItemID = "item_id"
  val Preference = "pref"
}
