package controllers

import java.io._

import play.api._
import play.api.mvc._
import play.api.libs.json._ // JSON library
import play.api.libs.json.Reads._ // Custom validation helpers
import play.api.libs.functional.syntax._ // Combinator syntax

class Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  
  def getCandidates = Action {
  	Ok("""{
  "courses": [
    {
      "title": "解释暗物质",
      "teacher": "",
      "description": "宇宙的两种基本组成物质：暗物质和暗能量。它们组成宇宙的96%，但是不能被直接地测量，然而它们的影响是非常大的。(翻译:Donghua Lin,审译:Wang Qian)",
      "pic_link": "http://imgsize.ph.126.net/?enlarge=true&imgurl=http://vimg1.ws.126.net/image/snapshot_movie/2013/2/P/B/M8LN77SPB.jpg_180x100x1x95.jpg",
      "course_link": "http://open.163.com/movie/2012/1/6/2/M839BCIJ0_M83JKP762.html"
    },
    {
      "title": "预报“设计与弹性思维”展览",
      "teacher": "",
      "description": "Paola Antonelli 预报了突破性的展览「设计与弹性思维」－ 充满了反映我们现在思考方式的产品与设计。(翻译:Manlai You,审译:Chun-wen Chen)",
      "pic_link": "http://imgsize.ph.126.net/?enlarge=true&imgurl=http://vimg1.ws.126.net/image/snapshot_movie/2013/2/9/9/M8LN7U999.jpg_180x100x1x95.jpg",
      "course_link": "http://open.163.com/movie/2012/1/6/7/M8399V2RD_M83JKK967.html"
    },
    {
      "title": "无法触摸的音乐",
      "teacher": "",
      "description": "Pamelia Kurstin演奏并谈论了特雷门琴，这种电子乐器不需要接触就可以弹奏，而且并不限于科幻电影配乐。她演奏的曲目有“秋叶”， “怒放的生命”，还有David Mash的“听，言语离去了”。(翻译:Amy Zerotus,审译:Dian Liu)",
      "pic_link": "http://imgsize.ph.126.net/?enlarge=true&imgurl=http://vimg1.ws.126.net/image/snapshot_movie/2013/11/F/8/M9DRKC6F8.jpg_180x100x1x95.jpg",
      "course_link": "http://open.163.com/movie/2008/2/K/I/M82VAN1CC_M83JKFMKI.html"
    },
    {
      "title": "伊斯梅尔的故事",
      "teacher": "",
      "description": "放了他制作的电影“伊斯梅尔”的片段，这是一部感情强烈且丰富的、关于塞拉利昂的童军的电影。(翻译:Jingjing Wang,审译:Zhu Jie)",
      "pic_link": "http://imgsize.ph.126.net/?enlarge=true&imgurl=http://vimg1.ws.126.net/image/snapshot_movie/2013/11/8/D/M9DMMQH8D.jpg_180x100x1x95.jpg",
      "course_link": "http://open.163.com/movie/2008/10/Q/B/M82V7SDU0_M83JJQLQB.html"
    },
    {
      "title": "尼尔·图罗克教授的TED获奖愿望",
      "teacher": "",
      "description": "2008年的TED获奖者，物理学家Neil Turok指出有天赋的非洲年轻人十分渴求机会。他呼吁通过解放和培养非洲大陆的创造天赋，改变非洲的未来。",
      "pic_link": "http://imgsize.ph.126.net/?enlarge=true&imgurl=http://vimg1.ws.126.net/image/snapshot_movie/2013/11/N/7/M9DP7J2N7.jpg_180x100x1x95.jpg",
      "course_link": "http://open.163.com/movie/2012/1/E/K/M82V5CG9H_M83JJ86EK.html"
    }
  ]
}
""")
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



