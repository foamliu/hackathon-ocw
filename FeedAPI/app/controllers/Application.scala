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

case class Course(itemID: Long, title: String, description: String, piclink: String, courselink: String)

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


object Application {
    
    private val howMany = 5
    private val n = 5 // Nearest N User Neighborhood
    private val pref_file = "prefs.csv"
    private val item_file = "app/assets/jsons/items.json"

    private var courses: Seq[Course] = null

    private def getCourses() : Seq[Course] = {
        
        if (courses == null)
        {
            val source: String = Source.fromFile(item_file)("UTF-8").getLines.mkString
            val json: JsValue = Json.parse(source)
            
            courses = json.as[Seq[Course]]
        }

        courses

    }
    
    private def getPrefFile() : File = {
        
        var file: File = new File(pref_file)

        if (!file.exists())
        {
            val bw = new BufferedWriter(new FileWriter(file))
            //val items: Seq[Course]  = getCourses
            //items.foreach(i => {bw.write("%d,%d,%f".format(0, i.itemID, 1.0));bw.newLine()})
            bw.write("%d,%d,%f".format(1, 1, 1.0))
            bw.newLine()
            bw.close()
        }

        file
    }

    private def recommend(userID: Long) : List[Long] = {
        
        var model: GenericBooleanPrefDataModel = new GenericBooleanPrefDataModel(
				GenericBooleanPrefDataModel.toDataMap(new FileDataModel(getPrefFile())))

		var similarity: UserSimilarity = new LogLikelihoodSimilarity(model)
		var neighborhood: UserNeighborhood = new NearestNUserNeighborhood(n, similarity, model);
	
		var recommender: Recommender = new GenericUserBasedRecommender(model, neighborhood, similarity)
		var recommendations = recommender.recommend(userID, howMany)

        for (r <- recommendations) yield r.getItemID

    }
    
    private def getCandidates(userID: Long) : Seq[Course] = {
        
        val items: Seq[Course]  = getCourses
        val itemIDs: List[Long] = recommend(userID)
        
        val candidates: Seq[Course] = items.filter(i => itemIDs.contains(i.itemID))
        
        if (candidates.size > 0)
        {
            candidates
        }
        else
        {
            items.take(howMany)
        }

    }
}

class Application extends Controller {

    def index = Action {
        Ok(views.html.index("Your new application is ready."))
    }
  
    def getCandidates = Action {
        
        val userID: Long = 1L //TODO
        
        val candidates: Seq[Course] = Application.getCandidates(userID)
        val jsonArray = Json.toJson(candidates)
        
        Ok(Json.stringify(jsonArray))
    }
  
    def addPreference = Action(parse.json) {
        
        request =>
        {
            val json: JsValue = request.body
            
            val user_id = (json \ "user id").as[Long]
            val item_id = (json \ "item id").as[Long]
            val pref = (json \ "pref").as[Float]
          
            val file = new File(Application.pref_file)
            val bw = new BufferedWriter(new FileWriter(file, true))
            bw.write("%d,%d,%f".format(user_id,item_id,pref))
            bw.newLine()
            bw.close()
          
            Ok(user_id.toString)
        }
    }

}



