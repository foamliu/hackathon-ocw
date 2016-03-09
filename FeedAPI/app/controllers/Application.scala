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

import org.apache.mahout.cf.taste.impl.model._
import org.apache.mahout.cf.taste.impl.model.file._
import org.apache.mahout.cf.taste.impl.neighborhood._
import org.apache.mahout.cf.taste.impl.recommender._
import org.apache.mahout.cf.taste.impl.similarity._
import org.apache.mahout.cf.taste.model._
import org.apache.mahout.cf.taste.neighborhood._
import org.apache.mahout.cf.taste.recommender._
import org.apache.mahout.cf.taste.similarity._

object Application {
    
    private val howMany = 5
    private val n = 2 // Nearest N User Neighborhood
    private val pref_file = Play.application.path + "/" + "prefs.csv"
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
    
    def getPrefFile() : File = {
        
        var file: File = new File(pref_file)
        
        Logger.debug ("Pref file's absolute path = " + file.getAbsolutePath)
        Logger.debug ("Pref file exists: " + file.exists)
        Logger.debug ("Play.application.path: " + Play.application.path)
        Logger.debug ("Play.application.getFile.getAbsolutePath: " + Play.application.getFile("/").getAbsolutePath)

        if (!file.exists())
        {
            val bw = new BufferedWriter(new FileWriter(file))
            //val items: Seq[Course]  = getCourses
            //items.foreach(i => {bw.write("%d,%d,%f".format(0, i.itemID, 1.0));bw.newLine()})
            bw.write("1,1,1.0")
            bw.newLine()
            bw.close
            
            file = new File(pref_file)
        }

        file
    }

    private def recommend(userID: Long) : List[Long] = {
        
        Logger.debug ("UserID: " + userID)
        
        val file: File = getPrefFile()
        
        Logger.debug ("Pref file exists: " + file.exists)
        
        var model: GenericBooleanPrefDataModel = new GenericBooleanPrefDataModel(
				GenericBooleanPrefDataModel.toDataMap(new FileDataModel(file)))
				
		Logger.debug ("NumItems = " + model.getNumItems + " NumUsers = " + model.getNumUsers)
		Logger.debug ("UserIDs: " + model.getUserIDs)

		var similarity: UserSimilarity = new LogLikelihoodSimilarity(model)
		var neighborhood: UserNeighborhood = new NearestNUserNeighborhood(n, similarity, model);
	
		var recommender: Recommender = new GenericUserBasedRecommender(model, neighborhood, similarity)
		var recommendations = recommender.recommend(userID, howMany)
		Logger.debug ("NumRecommendations: " + recommendations.size)

        for (r <- recommendations) yield r.getItemID

    }
    
    private def getCandidates(userID: Long) : Seq[Course] = {
        
        val items: Seq[Course]  = getCourses
        Logger.debug (items.size + " items are loaded successfully.")
        
        val itemIDs: List[Long] = recommend(userID)
        Logger.debug ("NumItemIDs: " + itemIDs.size)
        Logger.debug ("ItemIDs: " + itemIDs)
        
        val candidates: Seq[Course] = items.filter(i => itemIDs.contains(i.itemID))
        Logger.debug ("NumCandidates: " + candidates.size)
        
        if (candidates.size > 0)
        {
            candidates
        }
        else
        {
            var randomItems = scala.util.Random.shuffle(items).take(howMany)
            randomItems.foreach(c => c.title = "*" + c.title)
            randomItems
        }

    }
}

class Application extends Controller {

    def index = Action {
        //Ok(views.html.index("Your new application is ready."))
        Ok("Your new application is ready.")
    }
  
    def getCandidates(id: Long) = Action {
        
        val userID: Long = id
        Logger.debug (userID toString)
        
        val candidates: Seq[Course] = Application.getCandidates(userID)

        val json: JsValue = Json.obj("courses" -> candidates)
        
        Ok(Json.stringify(json))
    }

}



