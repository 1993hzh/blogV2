import models.{WebSite, RoleType, Role, User}
import org.junit.{After, Before, Assert, Test}
import play.Logger
import utils.Encryption
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Created by Leo.
 * 2015/10/22 22:55
 */
class UserTest extends AbstractTest {

  @Before
  def insertUserLikeRegister(): Unit = {
    val adminRole = Role(0, RoleType.OWNER)
    val roleId = Await.result(roleDAO.insert(adminRole), Duration.Inf)

    val admin = User(0, "admin", Encryption.encodeBySHA1("admin"), "admin@test.com", roleId)
    Await.result(userDAO.insert(admin), Duration.Inf)
  }

  @Before
  def insertUserLikeBinding(): Unit = {
    val sinaRole = Role(0, RoleType.COMMON, WebSite.SINA)
    val roleId = Await.result(roleDAO.insert(sinaRole), Duration.Inf)

    val sinaUser = User(0, "sina", Encryption.encodeBySHA1("sina111"), "sina@test.com", roleId, bindingId = Some("sinaId"))
    Await.result(userDAO.insert(sinaUser), Duration.Inf)
  }

  @Test
  def loginUserLikeRegisterFail() = {
    //username or pwd wrong
    Assert.assertEquals(None, loginRegister("asd", "admin123"))
    Assert.assertEquals(None, loginRegister("admin", "admin123"))
    //3rd party users should not login like register user
    Assert.assertEquals(None, loginRegister("sina", "sina"))
  }

  def loginRegister(name: String, pwd: String): Option[(User, Role)] = {
    userDAO.login(name, pwd)
  }

  @Test
  def loginUserLikeRegisterSuccess() = {
    loginRegister("admin", "admin") match {
      case Some((user, role)) => {
        Logger.info("Login user: " + user)
        assertUser("admin", user, role)
      }
      case None => Assert.fail("admin login fail")
    }
  }

  def assertUser(name: String, user: User, role: Role) = {
    Assert.assertNotNull(role)
    Assert.assertEquals(name, user.userName)
    if (role.roleType.eq(RoleType.OWNER)) {
      Assert.assertEquals(Encryption.encodeBySHA1(name), user.password)
    }
    Assert.assertEquals(name + "@test.com", user.mail)
    Assert.assertEquals(role.id, user.roleId)
  }

  @Test
  def queryUserLikeBindingSuccess() = {
    loginBinding("sinaId", WebSite.SINA) match {
      case Some((user, role)) => {
        Logger.info("Login user: " + user)
        assertUser("sina", user, role)
      }
      case None => Assert.fail("sinaId login fail")
    }
  }

  @Test
  def queryUserLikeBindingFail() = {
    Assert.assertEquals(None, loginBinding("errorId", "errorSite"))
    Assert.assertEquals(None, loginBinding("admin", "admin"))
    Assert.assertEquals(None, loginBinding("sinaIdError", WebSite.SINA))
  }

  def loginBinding(bindingId: String, website: String): Option[(User, Role)] = {
    userDAO.loginFromOtherSite(bindingId, website)
  }

  @After
  def deleteUserAndRole(): Unit = {
    Await.result(userDAO.deleteByUserName("admin"), Duration.Inf)
    Await.result(userDAO.deleteByUserName("sina"), Duration.Inf)

    Await.result(roleDAO.deleteByRoleTypeAndWebsite(RoleType.OWNER, WebSite.MY), Duration.Inf)
    Await.result(roleDAO.deleteByRoleTypeAndWebsite(RoleType.COMMON, WebSite.SINA), Duration.Inf)
  }
}
