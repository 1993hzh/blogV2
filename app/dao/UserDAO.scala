package dao

import java.util.concurrent.TimeUnit

import models.{RoleType, User}
import slick.lifted.TableQuery
import tables.UserTable
import utils.Encryption
import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
 * Created by leo on 15-10-28.
 */
object UserDAO extends AbstractDAO[User] with UserTable {

  val userModelQuery = TableQuery[UserTable]

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

  override def query(id: Int): Future[User] = ???

  def queryByUserName(userName: String): Future[User] = {
    db.run(userModelQuery.filter(_.userName === userName).result.head)
  }

  def deleteByUserName(userName: String): Future[Int] = {
    db.run(userModelQuery.filter(_.userName === userName).delete)
  }

  def login(userName: String, password: String): Option[User] = {
    // query time should never larger than 1'
    val user = Await.result(db.run(userModelQuery.filter(_.userName === userName).result.headOption), Duration(1, TimeUnit.SECONDS))
    val pwd = Encryption.encodeBySHA1(password)
    user match {
      case Some(u) => {
        if (u.password.equals(pwd)) {
          //pwd must equals
          var returnUser: Option[User] = None
          RoleDAO.query(u.roleId) onComplete {
            //role must be OWNER
            case Success(result) => {
              if (result.roleType.equals(RoleType.OWNER))
                returnUser = Some(u)
            }
            case Failure(f) => returnUser = None
          }
          returnUser
        } else
          None
      }
      case None => None
    }
  }
}
