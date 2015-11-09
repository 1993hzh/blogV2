package controllers


import play.api._
import play.api.i18n.{MessagesApi, I18nSupport, Messages}
import play.api.mvc._
import play.api.cache.Cache
import play.api.Play.current

import play.api.db._
import play.api.i18n.Messages.Implicits._

object Application extends Controller {

  val KEY_PASSAGE_COUNT = "totalPassage"
  val KEY_PAGE_COUNT = "totalPage"
  val PAGE_SIZE = 5
  val LOGIN_CALL_BACK: String = "callback"

  def LOGIN_FIRST(passageId: Int) = "Please <a href=\"/loginWithCallback?callback=/passage?id=" +
    passageId + "&append=doComment\">login</a> first."

  val ERROR_NAME_OR_PWD = "Wrong username or password!"

  //  def index = Action {
  //    val message = Messages("index.message")
  //
  //    Ok(views.html.index(message))
  //  }


}
