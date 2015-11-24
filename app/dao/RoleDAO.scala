package dao

import javax.inject.Singleton

import models.{RoleType, Role}
import slick.lifted.TableQuery
import tables.RoleTable
import scala.concurrent.{Await, Future}

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

  private def getRoleByLoginUser(userId: Int): Future[String] = {
    val action = UserDAO.users.filter(_.id === userId).join(modelQuery).on(_.roleId === _.id).map(_._2.roleType).result.head
    db.run(action)
  }

  def getRoleByLoginUserSync(userId: Int): String = Await.result(getRoleByLoginUser(userId), waitTime)

  def getRoleId(roleType: String = RoleType.COMMON): Future[Int] = {
    db.run(modelQuery.filter(_.roleType === roleType).map(_.id).result.head)
  }

  def getRoleIdSync(roleType: String = RoleType.COMMON): Int = Await.result(getRoleId(roleType), waitTime)

  def getRoleByWebsiteSync(webSite: String, roleType: String = RoleType.THIRD_PARTY): Option[Role] = {
    Await.result(getRoleByWebsite(webSite, roleType), waitTime)
  }

  def getRoleByWebsite(webSite: String, roleType: String = RoleType.THIRD_PARTY): Future[Option[Role]] = {
    db.run(modelQuery.filter(r => r.website === webSite && r.roleType === roleType).result.headOption)
  }

}

object RoleDAO {

  val roles = new RoleDAO().modelQuery

  def apply() = {
    new RoleDAO()
  }
}