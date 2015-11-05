package tables

import java.sql.Timestamp

import dao.RoleDAO
import models.User

/**
  * Created by leo on 15-10-28.
  */
trait UserTable extends AbstractTable[User] {

  import driver.api._

  class UserTable(tag: Tag) extends AbstractTable(tag, "t_user") {

    def userName = column[String]("username")

    def password = column[String]("password")

    def mail = column[String]("mail")

    def lastLoginIp = column[Option[String]]("lastloginip")

    def lastLoginTime = column[Option[Timestamp]]("lastlogintime")

    def lastLogoutTime = column[Option[Timestamp]]("lastlogouttime")

    def bindingId = column[Option[String]]("binding_id")

    def roleId = column[Int]("role_id")

    def nameIndex = index("USERNAME_IDX", userName, unique = true)

    def mailIndex = index("MAIL_IDX", mail, unique = true)

    def bindingIdIndex = index("BINDING_ID_IDX", bindingId, unique = true)

    def roleFK = foreignKey("ROLE_FK", roleId, RoleDAO.roles)(_.id)

    override def * = (id, userName, password, mail, roleId, lastLoginIp, lastLoginTime, lastLogoutTime, bindingId) <>(User.tupled, User.unapply)
  }

}
