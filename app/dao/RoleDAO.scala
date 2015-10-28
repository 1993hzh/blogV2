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

  override def insert(model: Role): Future[Int] = {
    db.run(roleModelQuery returning roleModelQuery.map(_.id) += model)
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

  override def query(id: Int): Future[Role] = {
    db.run(roleModelQuery.filter(_.id === id).result.head)
  }

  def queryByRoleType(roleType: String): Future[Role] = {
    db.run(roleModelQuery.filter(_.roleType === roleType).result.head)
  }

  def deleteByRoleTypeAndWebsite(roleType: String, website: String): Future[Int] = {
    db.run(roleModelQuery.filter(r => (r.roleType === roleType && r.website === website)).delete)
  }
}
