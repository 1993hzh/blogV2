package controllers

import java.sql.Timestamp
import javax.inject.Inject

import dao.CommentDAO
import models.{Comment, Role, User}
import play.api.cache.CacheApi
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}

/**
 * Created by Leo.
 * 2015/11/10 22:24
 */
class CommentController @Inject()(cache: CacheApi) extends Controller {
  lazy val commentDAO = CommentDAO()

  def getComments(page: Int) = Action { implicit request =>
    request.session.get("loginUser") match {
      case Some(lu) =>
        cache.get[(User,Role)](lu) match {
          case Some((u, r)) =>
            val result = getInMessage(u.id, page)
            Ok(views.html.message(result._1, result._2, result._3))
          case None => Redirect(routes.Login.index)
        }
      case None => Redirect(routes.Login.index)
    }
  }

  def getInMessage(userId: Int, page: Int): (List[(Comment, String)], Int, Int) = {
    val count = commentDAO.getInMessagesCountSync(userId)
    val totalPage = Application.getTotalPage(count)
    val pageNo = Application.getPageNum(page, totalPage)
    val inMessages = commentDAO.getInMessagesByLoginUserSync(pageNo, userId = userId)
    (inMessages, pageNo, totalPage)
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
                  new Timestamp(System.currentTimeMillis), fromId, fromName, toId = data.toId, toName = data.toName)
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