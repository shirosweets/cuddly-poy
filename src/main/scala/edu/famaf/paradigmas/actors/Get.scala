package edu.famaf.paradigmas

import scalaj.http.{Http, HttpResponse}
import scala.xml.XML
import org.json4s.JsonDSL._
import org.json4s._
import org.json4s.jackson.JsonMethods._



abstract class URL_R{
  //Request HTTP
  def get_body(url: String): String = {
    try {
      Http(url).timeout(
        connTimeoutMs = 2000,
        readTimeoutMs = 5000).asString.body
    }
    catch{
      case e: Exception => ""
    }
  }
  def parser(url: String): Seq[String]
}

class RSS_Parse extends URL_R {
  //Parse Rss
  def parser(url: String): Seq[String] = {
    val response = get_body(url)
    response match {
    case "" => Seq()
    case _ =>
      val xml = XML.loadString(response)
        // Extract text from title and description
        (xml \\ "item").map { item =>
          ((item \ "title").text + " " + (item \ "description").text) }
    }
  }
}

class REDDIT_Parse extends URL_R {
  // Parse Reddit
  implicit val formats = DefaultFormats
  def parser(url: String): Seq[String] = {
    val response = get_body(url)
    response match {
      case "" => Seq()
      case _ =>
        val result = (parse(response) \ "data" \ "children" \ "data")
          .extract[List[Map[String, Any]]]
          // Parsear JSON
          val filterContent = result.flatten.filter{
            case (v , _) =>
              v == "title" || v == "selftext" }.map(x => x._2.toString)
          val pattern = "(https?|ftp|file)://[-a-zA-Z0-9+&@#/%?=~_|!:,.;]*[-a-zA-Z0-9+&@#/%=~_|]".r
          filterContent.map(x => pattern.replaceAllIn(x,"")).toSeq
    }
  }
}