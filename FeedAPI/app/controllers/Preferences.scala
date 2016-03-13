package controllers

import java.io.BufferedWriter
import java.io.FileWriter

import play.api.Logger
import play.api.libs.json.JsValue
import play.api.libs.json.JsValue.jsValueToJsLookup
import play.api.libs.json.Reads.FloatReads
import play.api.libs.json.Reads.LongReads
import play.api.mvc.Action
import play.api.mvc.Controller

class Preferences extends Controller {

    import controllers.PrefFields._

    def add(id: Long) = Action(parse.json) {

        val userID: Long = id
        Logger.debug(userID toString)

        request =>
            {
                val json: JsValue = request.body

                val user_id = (json \ UserID).as[Long]
                val item_id = (json \ ItemID).as[Long]
                val pref = (json \ Preference).as[Float]
                Logger.info("%d,%d,%f".format(user_id, item_id, pref))

                val file = Application.getPrefFile
                val bw = new BufferedWriter(new FileWriter(file, true))
                bw.write("%d,%d,%f".format(user_id, item_id, pref))
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
