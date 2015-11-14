package controllers


import java.time.LocalDateTime

import dao.PassageDAO
import models.{Role, User}
import play.api._
import play.api.i18n.{MessagesApi, I18nSupport, Messages}
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global
import play.api.i18n.Messages.Implicits._

import scala.util.{Failure, Success}

object Application extends Controller {

  private lazy val passageDAO = PassageDAO()

  val ERROR_NAME_OR_PWD = "Wrong username or password!"
  val KEY_PASSAGE_COUNT = "totalPassage"
  val KEY_PAGE_COUNT = "totalPageOfPassage"
  val PAGE_SIZE = 5
  val LOGIN_CALL_BACK: String = "callback"
  val PASSAGE_VIEW_COUNT_PREFIX = "passage-id-"
  val PASSAGE_BEEN_READ_LIST = "passage_has_been_read"

  def LOGIN_FIRST(passageId: Int) = "Please <a href=\"/loginWithCallback?callback=/passage?id=" +
    passageId + "&append=doComment\">login</a> first."

  def tooLong(name: String, length: Int) = name + "'s length too long, should less than " + length

  def getPageNum(num: Any, totalPage: Int) = {
    num match {
      case n: Int if (n <= 1) => 1
      case n: Int if (n >= totalPage) => totalPage
      case n: Int if (n > 1 && n < totalPage) => n
      case s: String if s.isEmpty => 1
      case _ => Logger.info("Error page num fetched: " + num); 1
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

  def setPassageCount = {
    passageDAO.queryTotalCount() onComplete {
      case Success(result) =>
        Cache.set(Application.KEY_PASSAGE_COUNT, result)
        setTotalPage(result)
      case Failure(f) =>
        Logger.error("App get passage count failed due to: " + f.getLocalizedMessage)
    }
  }

  private def setTotalPage(passageCount: Int) = {
    val totalPage = getTotalPage(passageCount)
    Cache.set(Application.KEY_PAGE_COUNT, totalPage)
    Logger.info("App get total page num: " + totalPage)
  }

  def now = LocalDateTime.now()
}
