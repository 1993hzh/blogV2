package controllers

import java.sql.Timestamp
import javax.inject.Inject
import dao.{CommentDAO, PassageDAO}
import models.{User, Comment}
import play.api.cache.CacheApi
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}

/**
 * Created by Leo.
 * 2015/11/7 20:00
 */
class PassageController @Inject()(cache: CacheApi) extends Controller {

  lazy val passageDAO = PassageDAO()
  lazy val commentDAO = CommentDAO()

  def passage(id: Int) = Action { implicit request =>
    val passageDetail = passageDAO.getDetail(id)
    passageDetail match {
      case Some(p) => Ok(views.html.passage(p._1, p._2, p._3))
      case None => Redirect(routes.Index.index)
    }
  }

  def createComment = Action(parse.form(commentForm)) { implicit request =>
    val loginFirst = "Please <a href=\"/login\">login</a> first."

    request.session.get("loginUser") match {
      case Some(lu) =>
        cache.get[User](lu) match {
          case Some(u) =>
            val fromId = u.id
            val fromName = u.userName
            val data = request.body
            val comment = Comment(0, data.content, data.passageId,
              new Timestamp(System.currentTimeMillis), fromId, fromName, data.toId, data.toName)
            commentDAO.insert(comment)
            Ok("Success")
          case None => Ok(loginFirst)
        }
      case None => Ok(loginFirst)
    }
  }

  val commentForm = Form(
    mapping(
      "content" -> nonEmptyText,
      "passageId" -> number,
      "toId" -> optional(number),
      "toName" -> optional(text)
    )(CommentForm.apply)(CommentForm.unapply)
  )
}

case class CommentForm(content: String,
                       passageId: Int,
                       toId: Option[Int] = None,
                       toName: Option[String] = None)