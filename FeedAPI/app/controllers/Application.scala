package controllers

import java.io._
import java.util._

import scala.collection.JavaConversions._
import scala.io._

import play.api._
import play.api.mvc._
import play.api.libs.json._ // JSON library
import play.api.libs.json.Reads._ // Custom validation helpers
import play.api.libs.functional.syntax._ // Combinator syntax

import org.apache.mahout.cf.taste.impl.model._
import org.apache.mahout.cf.taste.impl.model.file._
import org.apache.mahout.cf.taste.impl.neighborhood._
import org.apache.mahout.cf.taste.impl.recommender._
import org.apache.mahout.cf.taste.impl.similarity._
import org.apache.mahout.cf.taste.model._
import org.apache.mahout.cf.taste.neighborhood._
import org.apache.mahout.cf.taste.recommender._
import org.apache.mahout.cf.taste.similarity._

case class Course(item_id: Long, title: String, description: String, piclink: String, courselink: String)

class Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  
  def getCandidates = Action {
      
		var model: GenericBooleanPrefDataModel = new GenericBooleanPrefDataModel(
				GenericBooleanPrefDataModel.toDataMap(new FileDataModel(new File("prefs.csv"))))

		var similarity: UserSimilarity = new LogLikelihoodSimilarity(model)
		var neighborhood: UserNeighborhood = new NearestNUserNeighborhood(2, similarity, model);
	
		var recommender: Recommender = new GenericUserBasedRecommender(model, neighborhood, similarity)

		var recommendations = recommender.recommend(1, 1)
        println (recommendations.size)
        recommendations.foreach(println)
        
        implicit val courseReads: Reads[Course] = (
          (JsPath \\ "item_id").read[Long] and 
          (JsPath \\ "title").read[String] and 
          (JsPath \\ "description").read[String] and 
          (JsPath \\ "piclink").read[String] and 
          (JsPath \\ "courselink").read[String]
        )(Course.apply _)

        val source: String = Source.fromFile("app/assets/jsons/items.json")("UTF-8").getLines.mkString
        val json: JsValue = Json.parse(source)
        
        var courses = json.as[Seq[Course]]
        
        println(courses)
        
        Ok("Ok")
  }
  
  def addPreference = Action(parse.json) { request =>
    {
        val json: JsValue = request.body
        
        val user_id = (json \ "user id").as[Long]
        val item_id = (json \ "item id").as[Long]
        val pref = (json \ "pref").as[Float]
      
        val file = new File("prefs.csv")
        val bw = new BufferedWriter(new FileWriter(file, true))
        bw.write("%d,%d,%f".format(user_id,item_id,pref))
        bw.newLine()
        bw.close()
      
        Ok(user_id.toString)
    }
  }

}



