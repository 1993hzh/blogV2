package dao

import java.sql.Timestamp
import javax.inject.Singleton

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

  lazy val roleDAO = RoleDAO()

  import driver.api._

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
          case Some(role) if role.roleType.equals(RoleType.OWNER) =>
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
      case None => Logger.error("User: " + userName + " not found!")
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