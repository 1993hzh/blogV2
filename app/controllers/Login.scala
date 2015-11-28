package controllers

import java.sql.Timestamp
import javax.inject.Inject
import dao.UserDAO
import models.{Role, User}
import play.api.Logger
import play.api.cache._
import play.api.data._
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

import scala.concurrent.Future

/**
 * Created by leo on 15-11-4.
 */
class Login @Inject()(cache: CacheApi, messages: MessagesApi) extends Controller with I18nSupport {
  override def messagesApi: MessagesApi = messages

  private lazy val log = Logger(this.getClass)
  private lazy val userDAO = UserDAO()

  def index = Action { implicit request =>
    val callback = request.getQueryString(Application.LOGIN_CALL_BACK)
    Ok(views.html.login(callback))
  }

  // do not support callback now
  def login = Action.async { implicit request =>
    loginForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(Ok(views.html.login(error = Some(formWithErrors.errors.map(_.message).mkString(", ")))))
      },
      data => {
        userDAO.login(data.name, data.password) match {
          case Some((u, r)) =>
            Application.doUserLogin(cache, u, r, request, data.callback)
          case None =>
            Future.successful(Ok(views.html.login(error = Some(Application.ERROR_NAME_OR_PWD))))
        }
      }
    )
  }

  def logout = Action { implicit request =>
    val loginToken = request.session.get("loginUser")

    val loginUser = cache.get[(User, Role)](loginToken.getOrElse(""))

    loginUser match {
      case Some((u, r)) =>
        val logoutTime = new Timestamp(System.currentTimeMillis())
        userDAO.updateLogoutInfo(u.userName, logoutTime)
        log.info("User: " + u.userName + " logout at: " + logoutTime)

        loginToken match {
          case Some(lt) =>
            cache.remove(lt) // remove loginToken from public cache
            cache.remove(u.userName + "-unreadMessage") // remove unreadMessage from public cache
          case None => log.info("Session has no `loginUser` ")
        }
      case None =>
        log.info("Didn't find " + loginToken)
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
