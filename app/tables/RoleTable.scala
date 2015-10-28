package tables

import models.{WebSite, Role}
import slick.driver.JdbcProfile

/**
 * Created by leo on 15-10-28.
 */
trait RoleTable {

  protected val driver: JdbcProfile

  import driver.api._

  class RoleTable(tag: Tag) extends Table[Role](tag, "t_role") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def roleType = column[String]("roletype")

    def website = column[String]("website", O.Default(WebSite.MY))

    def roleType_website_Index = index("ROLETYPE_WEBSITE_IDX", (roleType, website), unique = true)

    override def * = (id, roleType, website) <>(Role.tupled, Role.unapply)
  }

}
