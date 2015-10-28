package tables

import models.Role
import slick.driver.JdbcProfile

/**
 * Created by leo on 15-10-28.
 */
trait RoleTable {

  protected val driver: JdbcProfile

  import driver.api._

  class RoleTable(tag: Tag) extends Table[Role](tag, "T_ROLE") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def roleType = column[String]("roleType")

    def website = column[Option[String]]("website")

    override def * = (id, roleType, website) <>(Role.tupled, Role.unapply)
  }

}
