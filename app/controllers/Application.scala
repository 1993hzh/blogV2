package controllers


import java.time.LocalDateTime

import models.{Role, User}
import play.api._
import play.api.i18n.{MessagesApi, I18nSupport, Messages}
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current
import play.api.i18n.Messages.Implicits._

object Application extends Controller {

  val KEY_PASSAGE_COUNT = "totalPassage"
  val KEY_PAGE_COUNT = "totalPage"
  val PAGE_SIZE = 5
  val LOGIN_CALL_BACK: String = "callback"

  def LOGIN_FIRST(passageId: Int) = "Please <a href=\"/loginWithCallback?callback=/passage?id=" +
    passageId + "&append=doComment\">login</a> first."

  def tooLong(name: String, length: Int) = name + "'s length too long, should less than " + length

  val ERROR_NAME_OR_PWD = "Wrong username or password!"

  //  def index = Action {
  //    val message = Messages("index.message")
  //
  //    Ok(views.html.index(message))
  //  }

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

  def now = LocalDateTime.now()
}
