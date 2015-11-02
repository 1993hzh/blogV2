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

  override protected val modelQuery = TableQuery[RoleTable]

  override type T = RoleTable

  import driver.api._

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