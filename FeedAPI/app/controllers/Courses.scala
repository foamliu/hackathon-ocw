package controllers

import play.api.libs.functional.syntax.functionalCanBuildApplicative
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json.JsPath
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json.Json.toJsFieldJsValueWrapper
import play.api.libs.json.Reads
import play.api.libs.json.Reads.LongReads
import play.api.libs.json.Reads.StringReads
import play.api.libs.json.Reads.functorReads
import play.api.libs.json.Writes


case class CourseItem(title: String, link: String, var piclink: String)

object CourseItem {
  
    implicit val courseWrites = new Writes[CourseItem] {
        def writes(c: CourseItem): JsValue =
            Json.obj(
                "title" -> c.title,
                "link" -> c.link,
                "picklink" -> c.piclink)
    }
    
    implicit val courseReads: Reads[CourseItem] = (
        (JsPath \\ "title").read[String] and
        (JsPath \\ "link").read[String] and 
        (JsPath \\ "link").read[String])(CourseItem.apply _)

}

case class Course(title: String, description: String, piclink: String, link: String, instructor: String, items: Seq[CourseItem])

object Course {

    implicit val courseReads: Reads[Course] = (
        (JsPath \\ "coursetitle").read[String] and
        (JsPath \\ "coursedescription").read[String] and
        (JsPath \\ "coursepiclink").read[String] and
        (JsPath \\ "courselink").read[String] and
        (JsPath \\ "courseinstructor").read[String] and
        (JsPath \\ "items").read[Seq[CourseItem]])(Course.apply _)

    implicit val courseWrites = new Writes[Course] {
        def writes(c: Course): JsValue =
            Json.obj(
                "coursetitle" -> c.title,
                "coursedescription" -> c.description,
                "coursepiclink" -> c.piclink,
                "courselink" -> c.link,
                "courseinstructor" -> c.instructor,
                "items" -> c.items)
    }
}