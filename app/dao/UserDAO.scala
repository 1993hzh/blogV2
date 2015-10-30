package dao

import java.util.concurrent.TimeUnit
import javax.inject.Singleton

import models.{RoleType, User}
import slick.lifted.TableQuery
import tables.UserTable
import utils.Encryption
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by leo on 15-10-28.
 */
@Singleton()
class UserDAO extends AbstractDAO[User] with UserTable {

  val userModelQuery = TableQuery[UserTable]

  lazy val roleDAO = RoleDAO()

  import driver.api._

  override def insert(model: User): Future[Int] = {
    db.run(userModelQuery returning userModelQuery.map(_.id) += model)
  }

  override def update(model: User): Future[Int] = {
    db.run(userModelQuery.filter(_.id === model.id).update(model))
  }

  override def delete(model: User): Future[Int] = {
    db.run(userModelQuery.filter(_.id === model.id).delete)
  }

  override def upsert(model: User): Future[Int] = {
    db.run(userModelQuery.insertOrUpdate(model))
  }

  override def query(id: Int): Future[Option[User]] = ???

  def queryByUserName(userName: String): Future[Option[User]] = {
    db.run(userModelQuery.filter(_.userName === userName).result.headOption)
  }

  def deleteByUserName(userName: String): Future[Int] = {
    db.run(userModelQuery.filter(_.userName === userName).delete)
  }

  def login(userName: String, password: String): Option[User] = {
    // query time should never larger than 1'
    val waitTime = Duration(1, TimeUnit.SECONDS)

    val user = Await.result(db.run(userModelQuery.filter(_.userName === userName).result.headOption), waitTime)
    val pwd = Encryption.encodeBySHA1(password)
    user match {
      case Some(u) if (u.password.equals(pwd)) => {
        //pwd must equals
        var returnUser: Option[User] = None
        Await.result(roleDAO.query(u.roleId), waitTime) match {
          case Some(role) if (role.roleType.equals(RoleType.OWNER)) =>
            returnUser = Some(u)
          case _ =>
        }
        returnUser
      }
      case _ => None
    }
  }
}

object UserDAO {

  lazy val userModelQuery = new UserDAO().userModelQuery

  def apply() = {
    new UserDAO()
  }
}