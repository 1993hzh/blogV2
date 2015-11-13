package controllers

import java.sql.Timestamp
import javax.inject.Inject

import dao.{PassageDAO, CommentDAO}
import models.{CommentStatus, Comment, Role, User}
import play.api.Logger
import play.api.cache.CacheApi
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext.Implicits.global

/**
  * Created by Leo.
  * 2015/11/10 22:24
  */
class CommentController @Inject()(cache: CacheApi) extends Controller {
  lazy val commentDAO = CommentDAO()
  lazy val passageDAO = PassageDAO()

  def getComments(page: Int) = Action { implicit request =>
    request.session.get("loginUser") match {
      case Some(lu) =>
        cache.get[(User, Role)](lu) match {
          case Some((u, r)) =>
            val result = getInMessage(u.id, page)
            Ok(views.html.message(result._1, result._2, result._3))
          case None => Redirect(routes.Login.index)
        }
      case None => Redirect(routes.Login.index)
    }
  }

  private def getInMessage(userId: Int, page: Int): (List[(Comment, String)], Int, Int) = {
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
            cache.get[(User, Role)](lu) match {
              case Some((u, r)) =>
                val fromId = u.id
                val fromName = u.userName
                val comment = Comment(0, data.content, data.passageId,
                  new Timestamp(System.currentTimeMillis), fromId, fromName, toId = data.toId, toName = data.toName)
                //here create the comment
                commentDAO.insert(comment) onSuccess {
                  case r: Int =>
                    data.toName match {
                      case Some(to) =>
                        //here we update the receiver unreadCount if exists
                        resetUnreadCountInCache(to, 1)
                      case None =>
                        //here we update the authorF unreadCount if exists
                        val authorName = passageDAO.getAuthorNameWithPassageIdSync(data.passageId)
                        resetUnreadCountInCache(authorName, 1)
                    }
                }
                //here update the original comment status
                data.toCommentId match {
                  case Some(cId) =>
                    if (commentDAO.markAsSync(cId, CommentStatus.commented))
                      resetUnreadCountInCache(fromName, -1) //here we update the replier unreadCount
                  case None =>
                }

                Ok("Success")
              case None => Ok(Application.LOGIN_FIRST(data.passageId))
            }
          case None => Ok(Application.LOGIN_FIRST(data.passageId))
        }
      }
    )
  }

  private def resetUnreadCountInCache(user: String, markSize: Int): Unit = {
    val key = user + "-unreadMessage"
    val count: Int = cache.getOrElse(key)("0").toInt
    val result = count + markSize
    cache.set(key, if (result < 0) "0" else result.toString)
  }

  def markInMessageAs(markType: String, commentId: Int) = Action { implicit request =>
    val status = CommentStatus.getStatus(markType.toUpperCase)
    val result = commentDAO.markAsSync(commentId, status)
    val userName = Application.getLoginUserName(request.session)

    result match {
      case true =>
        resetUnreadCountInCache(userName, if (status.equals(CommentStatus.unread)) 1 else -1) //here we update the replier unreadCount
        Ok("Success")
      case false => Ok("Sorry that markAs" + markType + " failed, this issue has been logged, will be fixed later")
    }
  }

  @deprecated("i don't think this demand is highly considered")
  def markInMessagesAs(markType: String, commentIds: Any) = Action { implicit request =>
    val status = CommentStatus.getStatus(markType.toUpperCase)
    val userName = Application.getLoginUserName(request.session)

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
        val cSet = cList.map(c => c.toString.toInt).toSet
        val markSize = cSet.size

        commentDAO.marksAsSync(cSet, status) match {
          case true =>
            resetUnreadCountInCache(userName, if (status.equals(CommentStatus.unread)) markSize else -markSize) //here we update the replier unreadCount
            Ok("Success")
          case false =>
            Ok("Sorry that markAs" + status + " failed, this issue has been logged, will be fixed later")
        }

      case _ =>
        Logger.info("markAsRead with commentIdList error with: " + commentIds)
        Ok("markAsRead with commentIdList error with: " + commentIds)
    }
  }

  def viewComment(passageId: Int, commentId: Int) = Action { implicit request =>
    val userName = Application.getLoginUserName(request.session)
    if (commentDAO.markAsSync(commentId))
      resetUnreadCountInCache(userName, -1)

    val url = "/passage?id=" + passageId + "#" + commentId
    Redirect(url)
  }

  val commentForm = Form(
    mapping(
      "content" -> nonEmptyText,
      "passageId" -> number,
      "toId" -> optional(number),
      "toName" -> optional(text),
      "toCommentId" -> optional(number)
    )(CommentForm.apply)(CommentForm.unapply)
      verifying(Application.tooLong("content", 200), fields => fields.content.length < 200)
  )
}

case class CommentForm(content: String,
                       passageId: Int,
                       toId: Option[Int] = None,
                       toName: Option[String] = None,
                       toCommentId: Option[Int] = None)