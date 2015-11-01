package dao

import javax.inject.Singleton

import models.Role
import slick.lifted.TableQuery
import tables.RoleTable
import scala.concurrent.Future

/**
 * Created by leo on 15-10-28.
 */
@Singleton()
class RoleDAO extends AbstractDAO[Role] with RoleTable {

  private val modelQuery = TableQuery[RoleTable]

  import driver.api._

  override def insert(model: Role): Future[Int] = {
    db.run(modelQuery returning modelQuery.map(_.id) += model)
  }

  override def update(model: Role): Future[Int] = {
    db.run(modelQuery.filter(_.id === model.id).update(model))
  }

  override def delete(model: Role): Future[Int] = {
    db.run(modelQuery.filter(_.id === model.id).delete)
  }

  override def upsert(model: Role): Future[Int] = {
    db.run(modelQuery.insertOrUpdate(model))
  }

  override def query(id: Int): Future[Option[Role]] = {
    db.run(modelQuery.filter(_.id === id).result.headOption)
  }

  def queryByRoleType(roleType: String): Future[Option[Role]] = {
    db.run(modelQuery.filter(_.roleType === roleType).result.headOption)
  }

  def deleteByRoleTypeAndWebsite(roleType: String, website: String): Future[Int] = {
    db.run(modelQuery.filter(r => (r.roleType === roleType && r.website === website)).delete)
  }
}

object RoleDAO {

  val roles = new RoleDAO().modelQuery

  def apply() = {
    new RoleDAO()
  }
}