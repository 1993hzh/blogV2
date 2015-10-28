import java.sql.Date

import dao.{UserDAO, RoleDAO}
import models.{RoleType, Role, User}
import org.junit.{Before, Assert, Test}
import utils.Encryption

/**
 * Created by Leo.
 * 2015/10/22 22:55
 */
class UserTest extends AbstractTest {


  @Test
  def insertUserLikeRegister() = {
    val adminRole = Role(0, RoleType.OWNER)
    RoleDAO.insert(adminRole)

    val admin = User(0, "admin", Encryption.encodeBySHA1("admin"), "admin@test.com", adminRole.id)
    UserDAO.insert(admin)

    assertUser(admin, adminRole)
  }

  @Test
  def queryUserLikeRegister() = {
  }

  def assertUser(user: User, role: Role) = {
    Assert.assertEquals("Test", user.userName)
    Assert.assertEquals(Encryption.encodeBySHA1("Test"), user.password)
    Assert.assertEquals("test@test.com", user.mail)
    Assert.assertEquals(role.id, user.roleId)
  }

  @Test
  def insertUserLikeBinding() = {

  }
}
