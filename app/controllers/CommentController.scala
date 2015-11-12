package controllers

import java.sql.Timestamp
import javax.inject.Inject

import dao.CommentDAO
import models.{CommentStatus, Comment, Role, User}
import play.api.Logger
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

  def markInMessageAs(markType: String, commentId: Int) = Action {
    Ok(commentDAO.markAsSync(commentId, CommentStatus.getStatus(markType.toUpperCase)))
  }

  def markInMessagesAs(markType: String, commentIds: Any) = Action {
    commentIds match {
      case cs: String =>
        val cList = cs.split(",").collect {
          case c: String =>
            try {
              c.trim.toInt
            } catch {
              case ne: NumberFormatException => Logger.info("Try to format " + c + " to Int failed: " + ne.getLocalizedMessage)
            }
        }
        Ok(commentDAO.marksAsSync(cList.map(c => c.toString.toInt).toSet, CommentStatus.getStatus(markType.toUpperCase)))
      case _ =>
        Logger.info("markAsRead with commentIdList error with: " + commentIds)
        Ok("markAsRead with commentIdList error with: " + commentIds)
    }
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