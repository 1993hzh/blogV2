package controllers

import javax.inject.Inject

import dao.UserDAO
import play.api.Logger
import play.api.cache.CacheApi
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}

import play.api.libs.concurrent.Execution.Implicits.defaultContext

/**
 * Created by Leo.
 * 2015/11/15 10:15
 */
class UserController @Inject()(cache: CacheApi) extends Controller {

  private lazy val userDAO = UserDAO()

  private lazy val log = Logger

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
        Ok(formWithErrors.errors.map(_.message).mkString(", "))
      },
      data => {
        val result = userDAO.createCommonUserSync(data.userName, data.password, data.mail)
        result match {
          case result: Int if result > 0 =>
            log.info("User: " + data.userName + " is created, new id: " + result)
            Ok("Success")
          case result: Int if result <= 0 =>
            log.info("User: " + data.userName + " create failed, return value: " + result)
            Ok("User: " + data.userName + " create failed, return value: " + result)
        }
      }
    )
  }

  def delete(id: Int) = Action.async { implicit request =>
    userDAO.delete(id) map {
      case result: Int if result == 1 =>
        log.info("user: " + id + " delete succeed")
        Ok("Success")
      case result: Int if result != 1 =>
        log.info("user: " + id + " delete failed, return value: " + result)
        Ok("user: " + id + " delete failed, return value: " + result)
    }
  }

  val userForm = Form(
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