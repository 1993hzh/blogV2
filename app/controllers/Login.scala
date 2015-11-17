package controllers

import java.sql.Timestamp
import javax.inject.Inject
import dao.UserDAO
import dao.CommentDAO
import models.{Role, User}
import play.api.Logger
import play.api.cache._
import utils.Encryption
import play.api.data._
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}

/**
  * Created by leo on 15-11-4.
  */
class Login @Inject()(cache: CacheApi) extends Controller {

  private val userDAO = UserDAO()
  private val commentDAO = CommentDAO()
  private var loginTime: Timestamp = null
  private var logoutTime: Timestamp = null

  def index = Action { implicit request =>
    val callback = request.getQueryString(Application.LOGIN_CALL_BACK)
    Ok(views.html.login(callback))
  }

  def login = Action { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => {
        Application.sendJsonResult(false, formWithErrors.errors.map(_.message).mkString(", "))
      },
      data => {
        val loginUser = userDAO.login(data.name, data.password)

        loginUser match {
          case Some((u, r)) =>
            val current = System.currentTimeMillis()
            loginTime = new Timestamp(current)
            Logger.info("User: " + u.userName + " on behalf as: " + r.roleType +
              ", login from: " + request.remoteAddress + " at: " + loginTime)

            val loginToken = Encryption.encodeBySHA1(u.userName + current)
            val loginUserSession = request.session + ("loginUser" -> loginToken) // add loginToken into session

            cache.set(loginToken, (u, r)) // add loginToken into public cache
            cache.set(u.userName + "-unreadMessage", getUnreadInMessage(u.id)) //add unreadMessage into public cache

            data.callback match {
              case Some(str) =>
                Application.sendJsonResult(true, str) withSession (loginUserSession) //callback exists
              case None =>
                Application.sendJsonResult(true, routes.Index.index().url) withSession (loginUserSession) //callback exists
            }

          case None =>
            Application.sendJsonResult(false, Application.ERROR_NAME_OR_PWD)
        }
      }
    )
  }

  private def getUnreadInMessage(userId: Int): String = {
    val count = commentDAO.getUnreadInMessagesCountSync(userId)
    count.toString
  }

  def logout = Action { implicit request =>
    val loginToken = request.session.get("loginUser")

    val loginUser = cache.get[(User, Role)](loginToken.getOrElse(""))

    loginUser match {
      case Some((u, r)) =>
        logoutTime = new Timestamp(System.currentTimeMillis())
        userDAO.updateLoginInfo(u.userName, request.remoteAddress, loginTime, logoutTime)
        Logger.info("User: " + u.userName + " logout at: " + logoutTime)

        loginToken match {
          case Some(lt) =>
            cache.remove(lt) // remove loginToken from public cache
            cache.remove(u.userName + "-unreadMessage") // remove unreadMessage from public cache
          case None => Logger.info("Session has no `loginUser` ")
        }
      case None =>
        Logger.info("Didn't find " + loginToken)
    }
    Redirect(routes.Index.index()).withNewSession
  }

  val loginForm = Form(
    mapping(
      "callback" -> optional(text),
      "name" -> nonEmptyText,
      "password" -> nonEmptyText
    )(LoginForm.apply)(LoginForm.unapply)
      verifying(Application.tooLong("callback", 100), fields => fields.callback.getOrElse("").length < 100)
      verifying(Application.tooLong("name", 64), fields => fields.name.length < 64)
      verifying(Application.tooLong("password", 255), fields => fields.password.length < 255)
  )
}

case class LoginForm(callback: Option[String], name: String, password: String)
