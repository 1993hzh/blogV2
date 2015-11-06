package controllers

import java.sql.Timestamp
import javax.inject.Inject

import dao.UserDAO
import models.User
import play.api.Logger
import play.api.cache._
import utils.Encryption
import scala.concurrent.duration._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}

/**
  * Created by leo on 15-11-4.
  */
class Login @Inject()(cache: CacheApi) extends Controller {

  private val userDAO = UserDAO()
  private var loginTime: Timestamp = null
  private var logoutTime: Timestamp = null

  def index = Action { implicit request =>
    Ok(views.html.login())
  }

  def login = Action(parse.form(loginForm)) { implicit request =>
    val data = request.body

    val loginUser = userDAO.login(data.name, data.password)

    loginUser match {
      case Some(u) =>
        val current = System.currentTimeMillis()
        loginTime = new Timestamp(current)
        Logger.info("User: " + u.userName + " login from: " + request.remoteAddress + " at: " + loginTime)

        val loginToken = Encryption.encodeBySHA1(u.userName + current)
        val loginUser = request.session + ("loginUser" -> loginToken)
        cache.set(loginToken, u, 30.minutes)

        Ok("Success") withSession (loginUser)
      case None =>
        Ok("Wrong username or password!")
    }
  }

  def logout = Action { implicit request =>
    val userName = request.session.get("loginUser")

    val loginUser = cache.get[User](userName.getOrElse(""))

    loginUser match {
      case Some(u) =>
        logoutTime = new Timestamp(System.currentTimeMillis())
        userDAO.updateLoginInfo(u.userName, request.remoteAddress, loginTime, logoutTime)
        Logger.info("User: " + u.userName + " logout at: " + logoutTime)

        request.session - ("loginUser")
        userName match {
          case Some(un) => cache.remove(un)
          case None => Logger.error("Session has no `loginUser` ")
        }
      case None =>
        Logger.error("Didn't find " + userName)
    }
    Redirect(routes.Index.index()).withNewSession
  }


  val loginForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginForm.apply)(LoginForm.unapply)
  )
}

case class LoginForm(name: String, password: String)
