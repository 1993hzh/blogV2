package tables

import java.sql.Timestamp

import dao.RoleDAO
import models.User
import slick.driver.JdbcProfile

/**
 * Created by leo on 15-10-28.
 */
trait UserTable {

  protected val driver: JdbcProfile

  import driver.api._

  class UserTable(tag: Tag) extends Table[User](tag, "T_USER") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def userName = column[String]("userName")

    def password = column[String]("password")

    def mail = column[String]("mail")

    def lastLoginIp = column[Option[String]]("lastLoginIp")

    def lastLoginTime = column[Option[Timestamp]]("lastLoginTime")

    def roleId = column[Int]("role_id")

    def nameIndex = index("USERNAME_IDX", userName, unique = true)

    def mailIndex = index("MAIL_IDX", mail, unique = true)

    def roleFK = foreignKey("ROLE_FK", roleId, RoleDAO.roleModelQuery)(_.id)


    override def * = (id, userName, password, mail, roleId, lastLoginIp, lastLoginTime) <>(User.tupled, User.unapply)
  }

}
