package controllers


import java.sql.Timestamp
import java.time.LocalDateTime

import dao.{UserDAO, CommentDAO, PassageDAO}
import models.{Role, User}
import play.api._
import play.api.i18n.{Messages}
import play.api.libs.json.Json
import play.api.mvc._
import play.api.cache.{CacheApi, Cache}
import play.api.Play.current
import utils.Encryption
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.i18n.Messages.Implicits._

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Application extends Controller {
  private lazy val log = Logger(this.getClass)

  private lazy val passageDAO = PassageDAO()
  private lazy val commentDAO = CommentDAO()
  private lazy val userDAO = UserDAO()

  val ERROR_NAME_OR_PWD = Messages("login.error")
  val KEY_PASSAGE_COUNT = "totalPassage"
  val KEY_PAGE_COUNT = "totalPageOfPassage"
  val PAGE_SIZE = 5
  val LOGIN_CALL_BACK: String = "callback"
  val PASSAGE_VIEW_COUNT_PREFIX = "passage-id-"
  val PASSAGE_BEEN_READ_LIST = "passage_has_been_read"

  def loginAjax(callback: String) = Messages("login.ajax.redirect", routes.Login.index(), callback)

  def tooLong(name: String, length: Int) = Messages("error.toolong", name, length)

  def getPageNum(num: Any, totalPage: Int) = {
    num match {
      case n: Int if (n <= 1) => 1
      case n: Int if (n >= totalPage) => totalPage
      case n: Int if (n > 1 && n < totalPage) => n
      case s: String if s.isEmpty => 1
      case _ => log.info("Error page num fetched: " + num); 1
    }
  }

  def getTotalPage(count: Int, pageSize: Int = PAGE_SIZE): Int = count / pageSize + (if (count % pageSize != 0) 1 else 0)

  def getLoginUserName(session: Session): String = {
    val loginToken = session.get("loginUser").getOrElse("")
    Cache.getAs[(User, Role)](loginToken) match {
      case Some((u, r)) => u.userName
      case None => ""
    }
  }

  def getLoginUserId(session: Session): Int = {
    val loginToken = session.get("loginUser").getOrElse("")
    Cache.getAs[(User, Role)](loginToken) match {
      case Some((u, r)) => u.id
      case None => -1
    }
  }

  def getLoginUser(session: Session): Option[(User, Role)] = {
    val loginToken = session.get("loginUser").getOrElse("")
    Cache.getAs[(User, Role)](loginToken)
  }

  def setPassageCount = {
    passageDAO.queryTotalCount() onComplete {
      case Success(result) =>
        Cache.set(KEY_PASSAGE_COUNT, result)
        setTotalPage(result)
      case Failure(f) =>
        log.error("App get passage count failed due to: " + f.getLocalizedMessage)
    }
  }

  private def setTotalPage(passageCount: Int) = {
    val totalPage = getTotalPage(passageCount)
    Cache.set(KEY_PAGE_COUNT, totalPage)
    log.info("App get total page num: " + totalPage)
  }

  def now = LocalDateTime.now()

  def sendJsonResult(isSuccess: Boolean, detail: String) = {
    Ok(Json.obj("isSuccess" -> isSuccess, "detail" -> detail))
  }

  def getContent(content: String, maxLength: Int) = {
    content.length match {
      case length if length > maxLength => content.substring(0, maxLength).concat(" ...")
      case _ => content
    }
  }

  def doUserLogin(cache: CacheApi, u: User, r: Role, request: Request[AnyContent], callback: Option[String] = None): Future[Result] = {
    val loginTime = new Timestamp(System.currentTimeMillis())
    val ip = request.remoteAddress

    userDAO.updateLoginInfo(u.userName, ip, loginTime)
    log.info("User: " + u.userName + " on behalf of: " + r.roleType + ", login from: " + ip)

    val loginToken = Encryption.encodeBySHA1(u.userName + current)
    val loginUserSession = request.session + ("loginUser" -> loginToken) // add loginToken into session

    cache.set(loginToken, (u, r)) // add loginToken into public cache
    cache.set(u.userName + "-unreadMessage", getUnreadInMessage(u.id)) //add unreadMessage into public cache

    callback match {
      case Some(str) =>
        Future.successful(Redirect(str) withSession (loginUserSession)) //callback exists
      case None =>
        Future.successful(Redirect(routes.Index.index()) withSession (loginUserSession)) //callback exists
    }
  }

  private def getUnreadInMessage(userId: Int): String = {
    val count = commentDAO.getUnreadInMessagesCountSync(userId)
    count.toString
  }
}
