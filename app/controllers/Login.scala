package controllers

import java.time.LocalTime

import dao.UserDAO
import models.User
import play.Logger
import play.api.cache._
import scala.concurrent.duration._
import play.api.data._
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}
import play.api.Play.current

/**
  * Created by leo on 15-11-4.
  */
object Login extends Controller {

  val userDAO = UserDAO()

  def index = Action { implicit request =>
    Ok(views.html.login())
  }

  def login = Action(parse.form(loginForm)) { implicit request =>
    val data = request.body

    val loginUser = userDAO.login(data.name, data.password)

    loginUser match {
      case Some(u) =>
        Logger.info("User: " + u.userName + " login from: " + u.lastLoginIp)

        request.session.+("loginUser", u.userName)
        Cache.set(u.userName, u, 30.minutes)

        Ok("Success")
      case None =>
        Ok("Wrong username or password!")
    }
  }

  def logout = Action { implicit request =>
    val userName = request.session.get("loginUser")

    val loginUser = Cache.getAs[User](userName.getOrElse(""))

    loginUser match {
      case Some(u) =>
        Logger.info("User: " + userName + " logout at: " + LocalTime.now)

        request.session.-("loginUser")
        userName match {
          case Some(un) => Cache.remove(un)
          case None => Logger.error("Session has no `loginUser` ")
        }
      case None =>
        Logger.error("Didn't find " + userName)
    }
    Ok(views.html.index()).withNewSession
  }


  val loginForm = Form(
    mapping(
      "name" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginForm.apply)(LoginForm.unapply)
  )
}

case class LoginForm(name: String, password: String)
