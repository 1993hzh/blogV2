package controllers

import java.sql.Timestamp
import javax.inject.Inject

import dao.{PassageDAO, CommentDAO}
import models.{CommentStatus, Comment}
import play.api.Logger
import play.api.cache.CacheApi
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Leo.
 * 2015/11/10 22:24
 */
class CommentController @Inject()(cache: CacheApi, messages: MessagesApi) extends Controller with I18nSupport {
  override def messagesApi: MessagesApi = messages

  private lazy val log = Logger(this.getClass)
  private lazy val commentDAO = CommentDAO()
  private lazy val passageDAO = PassageDAO()

  def getComments(page: Int) = Action { implicit request =>
    val loginUserId = Application.getLoginUserId(request.session)
    val result = getInMessage(loginUserId, page)
    Ok(views.html.message(result._1, result._2, result._3))
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
        Application.sendJsonResult(false, formWithErrors.errors.map(_.message).mkString(", "))
      },
      data => {
        val loginUserId = Application.getLoginUserId(request.session)
        val loginUserName = Application.getLoginUserName(request.session)
        val fromId = loginUserId
        val fromName = loginUserName
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
          //User A replied to Comment1 which is sent to User B
          //in this case, Comment1 should not mark as read or commented
          case Some(cId) =>
            commentDAO.markAsSync(cId, CommentStatus.commented, fromId) match {
              case (true, true) => resetUnreadCountInCache(fromName, -1) //here we update the replier unreadCount
              case _ =>
            }
          case _ =>
        }
        Application.sendJsonResult(true, routes.PassageController.passage(data.passageId).url)
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
    val loginUserId = Application.getLoginUserId(request.session)

    val status = CommentStatus.getStatus(markType.toUpperCase)
    val result = commentDAO.markAsSync(commentId, status, loginUserId)
    val userName = Application.getLoginUserName(request.session)

    result match {
      case (true, true) =>
        resetUnreadCountInCache(userName, if (status.equals(CommentStatus.unread)) 1 else -1) //here we update the replier unreadCount
        Application.sendJsonResult(true, "")
      case (true, false) =>
        resetUnreadCountInCache(userName, if (status.equals(CommentStatus.unread)) 1 else -1) //here we update the replier unreadCount
        Application.sendJsonResult(true, "")
      case _ =>
        Application.sendJsonResult(false, "Sorry that markAs" + markType + " failed, this issue has been logged, will be fixed later")
    }
  }

  @deprecated("i don't think this demand is seldom considered(actually only me)", "since 2015/11/13(no version num)")
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
              case ne: NumberFormatException => log.info("Try to format " + c + " to Int failed: " + ne.getLocalizedMessage)
            }
        }
        val cSet = cList.map(c => c.toString.toInt).toSet
        val markSize = cSet.size

        commentDAO.marksAsSync(cSet, status) match {
          case true =>
            resetUnreadCountInCache(userName, if (status.equals(CommentStatus.unread)) markSize else -markSize) //here we update the replier unreadCount
            Application.sendJsonResult(true, "")
          case false =>
            Application.sendJsonResult(false, "Sorry that markAs" + status + " failed, this issue has been logged, will be fixed later")
        }

      case _ =>
        log.info("markAsRead with commentIdList error with: " + commentIds)
        Application.sendJsonResult(false, "markAsRead with commentIdList error with: " + commentIds)
    }
  }

  def markInMessagesAsRead = Action { implicit request =>
    val userId = Application.getLoginUserId(request.session)
    val userName = Application.getLoginUserName(request.session)

    val result = commentDAO.markAllAsReadSync(userId)
    resetUnreadCountInCache(userName, -result)

    Application.sendJsonResult(true, "")
  }

  def viewComment(passageId: Int, commentId: Int) = Action { implicit request =>
    val loginUserId = Application.getLoginUserId(request.session)
    val userName = Application.getLoginUserName(request.session)
    commentDAO.markAsSync(commentId, markerId = loginUserId) match {
      case (true, true) => resetUnreadCountInCache(userName, -1)
      case _ =>
    }

    Redirect(routes.PassageController.passage(passageId).url + "#" + commentId)
  }

  def commentForm(implicit lang: play.api.i18n.Lang) = Form(
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
