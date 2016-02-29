package controllers

import play.api._
import play.api.mvc._

class Application extends Controller {

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }
  
  def getCandidates = Action {
  	Ok("""{
  "courses": [
    {
      "title": "量子力學",
      "teacher": "易富國",
      "description": "易富國主講的量子力學",
      "pic_link": "http://ocw.aca.ntu.edu.tw/ntu-ocw/files/110px/100S221.jpg",
      "course_link": "http://ocw.aca.ntu.edu.tw/ntu-ocw/index.php/ocw/cou/100S221"
    },
    {
      "title": "狹義相對論的意義",
      "teacher": "張海潮",
      "description": "",
      "pic_link": "http://ocw.aca.ntu.edu.tw/ntu-ocw/files/110px/100S222.jpg",
      "course_link": "http://ocw.aca.ntu.edu.tw/ntu-ocw/index.php/ocw/cou/100S222"
    },
    {
      "title": "化學鍵",
      "teacher": "鄭原忠 、王瑜、陳竹亭",
      "description": "由原子基本性質與原子與分子軌域介紹化學鍵結的形成 ...",
      "pic_link": "http://ocw.aca.ntu.edu.tw/ntu-ocw/files/110px/100S223.jpg",
      "course_link": "http://ocw.aca.ntu.edu.tw/ntu-ocw/index.php/ocw/cou/100S223"
    },
    {
      "title": "微分幾何及其在物理的應用",
      "teacher": "蘇武沛",
      "description": "易富國主講的量子力學",
      "pic_link": "http://ocw.aca.ntu.edu.tw/ntu-ocw/files/110px/100S224.jpg",
      "course_link": "http://ocw.aca.ntu.edu.tw/ntu-ocw/index.php/ocw/cou/100S224"
    },
    {
      "title": "《新百家學堂》中華文化薪傳講座",
      "teacher": "張  亨等",
      "description": "本課程邀請到數年來致力耕耘於文學、語言、經濟領域 ...",
      "pic_link": "http://ocw.aca.ntu.edu.tw/ntu-ocw/files/110px/100S227.jpg",
      "course_link": "http://ocw.aca.ntu.edu.tw/ntu-ocw/index.php/ocw/cou/100S227"
    }
  ]
}
""")
  }


}
