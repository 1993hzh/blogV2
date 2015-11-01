package tables

import models.{WebSite, Role}

/**
 * Created by leo on 15-10-28.
 */
trait RoleTable extends AbstractTable[Role] {

  import driver.api._

  class RoleTable(tag: Tag) extends AbstractTable(tag, "t_role") {

    def roleType = column[String]("roletype")

    def website = column[String]("website", O.Default(WebSite.MY))

    def roleType_website_Index = index("ROLETYPE_WEBSITE_IDX", (roleType, website), unique = true)

    override def * = (id, roleType, website) <>(Role.tupled, Role.unapply)
  }

}
