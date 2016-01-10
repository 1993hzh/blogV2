package controllers

import javax.inject.Inject

import dao.UserDAO
import play.api.Logger
import play.api.cache.CacheApi
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{Lang, I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}

import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Future

/**
 * Created by Leo.
 * 2015/11/15 10:15
 */
class UserController @Inject()(cache: CacheApi, messages: MessagesApi) extends Controller with I18nSupport {
  override def messagesApi: MessagesApi = messages

  private lazy val userDAO = UserDAO()

  private lazy val log = Logger(this.getClass)

  def manage(page: Int) = Action { implicit request =>
    val pageSize = 10
    val totalUserCount = userDAO.getUserCountSync()
    val totalPage = Application.getTotalPage(totalUserCount, pageSize)
    val pageNum: Int = Application.getPageNum(page, totalPage)

    val usersWithRole = userDAO.getUsersWithRoleSync(pageNum, pageSize)
    Ok(views.html.manageUser(usersWithRole, pageNum, totalPage))
  }

  def create = Action { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => {
        Application.sendJsonResult(false, formWithErrors.errors.map(_.message).mkString(", "))
      },
      data => {
        val result = userDAO.createCommonUserSync(data.userName, data.password, data.mail)
        result match {
          case result: Int if result > 0 =>
            log.info("User: " + data.userName + " is created, new id: " + result)
            Application.sendJsonResult(true, "")
          case result: Int if result <= 0 =>
            log.info("User: " + data.userName + " create failed, return value: " + result)
            Application.sendJsonResult(false, "User: " + data.userName + " create failed, return value: " + result)
        }
      }
    )
  }

  def delete(id: Int) = Action.async { implicit request =>
    userDAO.delete(id) map {
      case result: Int if result == 1 =>
        log.info("user: " + id + " delete succeed")
        Application.sendJsonResult(true, "")
      case result: Int if result != 1 =>
        log.info("user: " + id + " delete failed, return value: " + result)
        Application.sendJsonResult(false, "user: " + id + " delete failed, return value: " + result)
    }
  }

  def switchLang(lang: String) = Action.async { implicit request =>
    Lang.get(lang) match {
      case Some(l) if (Application.SUPPORTED_LANGUAGES.contains(lang)) =>
        Future.successful(Application.sendJsonResult(true, "").withLang(l))
      case _ => Future.successful(Application.sendJsonResult(false, messages("language.notfound", lang)))
    }
  }

  def switchEffect(effect: String) = Action.async { implicit request =>
    val newEffect = request.session - ("effects") + ("effects" -> effect)
    Future.successful(Application.sendJsonResult(true, "").withSession(newEffect))
  }

  def profile() = Action.async { implicit request =>
    val user = Application.getLoginUser(request.session)
    user match {
      case Some((u, r)) =>
        Future.successful(Ok(views.html.profile((u, r), Application.SUPPORTED_LANGUAGES)))
      case None => Future.successful(Redirect(routes.Index.index()))
    }
  }

  /*def update = Action { implicit request =>
    userForm.bindFromRequest.fold(
      formWithErrors => {
        Application.sendJsonResult(false, formWithErrors.errors.map(_.message).mkString(", "))
      },
      data => {
        val userName = Application.getLoginUserName(request.session)
        if (!userName.equals(data.userName)) {
          log.info("User: " + userName + " try to update User: " + data.userName)
          Application.sendJsonResult(false, "")
        }
        val userId = Application.getLoginUserId(request.session)
        val result = userDAO.updateUserBySelf(userId, data.password, data.mail)
        result match {
          case result: Int if result > 0 =>
            log.info("User: " + data.userName + " is updated, new id: " + result)
            Application.sendJsonResult(true, "")
          case result: Int if result <= 0 =>
            log.info("User: " + data.userName + " update failed, return value: " + result)
            Application.sendJsonResult(false, "User: " + data.userName + " update failed, return value: " + result)
        }
      }
    )
  }*/

  def userForm(implicit lang: Lang) = Form(
    mapping(
      "userName" -> nonEmptyText,
      "password" -> nonEmptyText,
      "mail" -> email
    )(UserForm.apply)(UserForm.unapply)
      verifying(Application.tooLong("userName", 32), fields => fields.userName.length < 32)
      verifying("UserName cannot created with all space", fields => fields.userName.trim != 0)
      verifying("Password cannot created with all space", fields => fields.password.trim != 0)
  )

}

case class UserForm(userName: String,
                    password: String,
                    mail: String)