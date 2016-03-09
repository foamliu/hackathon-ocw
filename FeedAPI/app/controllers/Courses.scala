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
import play.api.Play.current

case class Course(itemID: Long, var title: String, description: String, piclink: String, courselink: String)

object Course {
    
    implicit val courseReads: Reads[Course] = (
      (JsPath \\ "item_id").read[Long] and 
      (JsPath \\ "title").read[String] and 
      (JsPath \\ "description").read[String] and 
      (JsPath \\ "piclink").read[String] and 
      (JsPath \\ "courselink").read[String]
    )(Course.apply _)
            
    implicit val courseWrites = new Writes[Course] {
        def writes(c: Course): JsValue = 
            Json.obj(
                "item_id" -> c.itemID,
                "title" -> c.title,
                "description" -> c.description,
                "piclink" -> c.piclink,
                "courselink" -> c.courselink
            ) 
    } 
}