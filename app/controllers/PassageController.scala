package controllers

import java.sql.Timestamp
import javax.inject.Inject
import dao.{CommentDAO, PassageDAO}
import models.{Role, User, Comment}
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

  def getComments = Action { implicit request =>
    request.session.get("loginUser") match {
      case Some(lu) =>
        cache.get[(User,Role)](lu) match {
          case Some((u, r)) =>
            //TODO getComments based on loginUser

            Ok(views.html.message())
          case None => Redirect(routes.Login.index)
        }
      case None => Redirect(routes.Login.index)
    }
  }

  def createComment = Action { implicit request =>
    commentForm.bindFromRequest.fold(
      formWithErrors => {
        Ok(formWithErrors.errors.map(_.message).mkString(", "))
      },
      data => {
        request.session.get("loginUser") match {
          case Some(lu) =>
            cache.get[(User,Role)](lu) match {
              case Some((u, r)) =>
                val fromId = u.id
                val fromName = u.userName
                val comment = Comment(0, data.content, data.passageId,
                  new Timestamp(System.currentTimeMillis), fromId, fromName, data.toId, data.toName)
                commentDAO.insert(comment)
                Ok("Success")
              case None => Ok(Application.LOGIN_FIRST(data.passageId))
            }
          case None => Ok(Application.LOGIN_FIRST(data.passageId))
        }
      }
    )
  }

  val commentForm = Form(
    mapping(
      "content" -> nonEmptyText,
      "passageId" -> number,
      "toId" -> optional(number),
      "toName" -> optional(text)
    )(CommentForm.apply)(CommentForm.unapply)
      verifying(Application.tooLong("content", 200), fields => fields.content.length < 200)
  )
}

case class CommentForm(content: String,
                       passageId: Int,
                       toId: Option[Int] = None,
                       toName: Option[String] = None)