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

case class Course(itemID: Long, var title: String, description: String, piclink: String, courselink: String, duration: String, source: String, school: String, instructor: String, language: String, tags: String, link: String, enabled: Boolean)

object Course {

    implicit val courseReads: Reads[Course] = (
        (JsPath \\ "item_id").read[Long] and
        (JsPath \\ "title").read[String] and
        (JsPath \\ "description").read[String] and
        (JsPath \\ "piclink").read[String] and
        (JsPath \\ "courselink").read[String] and
        (JsPath \\ "duration").read[String] and
        (JsPath \\ "source").read[String] and
        (JsPath \\ "school").read[String] and
        (JsPath \\ "instructor").read[String] and
        (JsPath \\ "language").read[String] and
        (JsPath \\ "tags").read[String] and
        (JsPath \\ "link").read[String] and
        (JsPath \\ "enabled").read[Boolean])(Course.apply _)

    implicit val courseWrites = new Writes[Course] {
        def writes(c: Course): JsValue =
            Json.obj(
                "item_id" -> c.itemID,
                "title" -> c.title,
                "description" -> c.description,
                "piclink" -> c.piclink,
                "courselink" -> c.courselink,
                "duration" -> c.duration,
                "source" -> c.source,
                "school" -> c.school,
                "instructor" -> c.instructor,
                "language" -> c.language,
                "tags" -> c.tags,
                "link" -> c.link,
                "enabled" -> c.enabled)
    }
}