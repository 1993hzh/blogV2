package dao

import models.Role
import slick.lifted.TableQuery
import tables.RoleTable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by leo on 15-10-28.
 */
object RoleDAO extends AbstractDAO[Role] with RoleTable {

  val roleModelQuery = TableQuery[RoleTable]

  import driver.api._

  override def insert(model: Role): Future[Unit] = {
    db.run(roleModelQuery += model).map(_ => ())
  }

  override def update(model: Role): Future[Int] = {
    db.run(roleModelQuery.filter(_.id === model.id).update(model))
  }

  override def delete(model: Role): Future[Int] = {
    db.run(roleModelQuery.filter(_.id === model.id).delete)
  }

  override def upsert(model: Role): Future[Int] = {
    db.run(roleModelQuery.insertOrUpdate(model))
  }

  override def query(hql: String): Future[Seq[Role]] = ???
}