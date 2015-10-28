package dao

import models.User
import slick.lifted.TableQuery
import tables.UserTable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

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
}
