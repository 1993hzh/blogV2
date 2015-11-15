package dao

import java.sql.Timestamp
import javax.inject.Singleton

import controllers.Application
import models.{Role, RoleType, User}
import play.api.Logger
import slick.lifted.TableQuery
import tables.UserTable
import utils.Encryption
import scala.concurrent.{Await, Future}

/**
 * Created by leo on 15-10-28.
 */
@Singleton()
class UserDAO extends AbstractDAO[User] with UserTable {

  override protected val modelQuery = TableQuery[UserTable]

  override type T = UserTable

  private lazy val roleDAO = RoleDAO()

  private lazy val log = Logger

  import driver.api._

  def createCommonUser(userName: String, password: String, mail: String): Future[Int] = {
    val roleId = roleDAO.getRoleIdSync()
    val encodePassword = Encryption.encodeBySHA1(password)
    val user = User(0, userName, encodePassword, mail, roleId)
    log.info("User: " + user + " is going to be created.")
    super.insert(user)
  }

  def createCommonUserSync(userName: String, password: String, mail: String): Int = {
    Await.result(createCommonUser(userName, password, mail), waitTime)
  }

  def getUserCount(): Future[Int] = db.run(modelQuery.length.result)

  def getUserCountSync(): Int = Await.result(getUserCount, waitTime)

  def getUsersWithRole(pageNum: Int, pageSize: Int = Application.PAGE_SIZE): Future[Seq[(User, Role)]] = {
    val action = modelQuery.join(RoleDAO.roles).on(_.roleId === _.id)
      .sortBy(_._1.userName.desc).drop((pageNum - 1) * pageSize).take(pageSize).result
    db.run(action)
  }

  def getUsersWithRoleSync(pageNum: Int, pageSize: Int = Application.PAGE_SIZE): Seq[(User, Role)] = {
    Await.result(getUsersWithRole(pageNum, pageSize), waitTime)
  }

  def queryByUserName(userName: String): Future[Option[User]] = {
    db.run(modelQuery.filter(_.userName === userName).result.headOption)
  }

  def deleteByUserName(userName: String): Future[Int] = {
    db.run(modelQuery.filter(_.userName === userName).delete)
  }

  def login(userName: String, password: String): Option[(User, Role)] = {
    val user = Await.result(db.run(modelQuery.filter(_.userName === userName).result.headOption), waitTime)
    val pwd = Encryption.encodeBySHA1(password)
    user match {
      case Some(u) if u.password.equals(pwd) =>
        //pwd must equals
        var returnUser: Option[(User, Role)] = None
        Await.result(roleDAO.query(u.roleId), waitTime) match {
          //3rd party user cannot login use this function
          case Some(role) if !role.roleType.equals(RoleType.THIRD_PARTY) =>
            returnUser = Some((u, role))
          case _ =>
        }
        returnUser
      case _ => None
    }
  }

  def loginFromOtherSite(bindingId: String, website: String): Option[(User, Role)] = {
    val user = Await.result(db.run(modelQuery.filter(_.bindingId === bindingId).result.headOption), waitTime)
    user match {
      case Some(u) =>
        var returnUser: Option[(User, Role)] = None
        Await.result(roleDAO.query(u.roleId), waitTime) match {
          case Some(role) if role.roleType.equals(RoleType.COMMON) && role.webSite.equals(website) =>
            returnUser = Some((u, role))
          case _ =>
        }
        returnUser
      case None => None
    }
  }

  def updateLoginInfo(userName: String, lastLoginIp: String, lastLoginTime: Timestamp, lastLogoutTime: Timestamp): Unit = {
    Await.result(queryByUserName(userName), waitTime) match {
      case Some(u) => updateLogInfo(u.id, lastLoginIp, lastLoginTime, lastLogoutTime)
      case None => log.error("User: " + userName + " not found!")
    }
  }

  private def updateLogInfo(id: Int, lastLoginIp: String, lastLoginTime: Timestamp, lastLogoutTime: Timestamp): Future[Int] = {
    val action = modelQuery.filter(_.id === id)
      .map(u => (u.lastLoginIp, u.lastLoginTime, u.lastLogoutTime))
      .update((Some(lastLoginIp), Some(lastLoginTime), Some(lastLogoutTime)))

    db.run(action)
  }
}

object UserDAO {

  lazy val users = new UserDAO().modelQuery

  def apply() = {
    new UserDAO()
  }
}