import dao.{UserDAO, RoleDAO}
import models.{WebSite, RoleType, Role, User}
import org.junit.{After, Before, Assert, Test}
import play.Logger
import utils.Encryption
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.Duration
import scala.util.{Failure, Success}

/**
 * Created by Leo.
 * 2015/10/22 22:55
 */
class UserTest extends AbstractTest {

  lazy val roleDAO = RoleDAO()

  lazy val userDAO = UserDAO()

  @Before
  def insertUserLikeRegister() = {
    val adminRole = Role(0, RoleType.OWNER)
    roleDAO.insert(adminRole) onComplete {
      case Failure(f) => Assert.fail(f.getLocalizedMessage)
      case Success(result) => {
        val admin = User(0, "admin", Encryption.encodeBySHA1("admin"), "admin@test.com", result)
        userDAO.insert(admin) onComplete {
          case Failure(f) => Assert.fail(f.getLocalizedMessage)
          case Success(result) =>
        }
      }
    }
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

  def loginRegister(name: String, pwd: String): Option[User] = {
    userDAO.login(name, pwd)
  }

  @Test
  def loginUserLikeRegisterSuccess() = {
    loginRegister("admin", "admin") match {
      case Some(user) => {
        Logger.info("Login user: " + user)
        val adminRole = Await.result(roleDAO.query(user.roleId), Duration.Inf).getOrElse(null)
        assertUser("admin", user, adminRole)
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
      case Some(user) => {
        Logger.info("Login user: " + user)
        val adminRole = Await.result(roleDAO.query(user.roleId), Duration.Inf).getOrElse(null)
        assertUser("sina", user, adminRole)
      }
      case None => Assert.fail("sinaId login fail")
    }
  }

  @Test
  def queryUserLikeBindingFail() = {
    //username or pwd wrong
    Assert.assertEquals(None, loginBinding("errorId", "errorSite"))
    Assert.assertEquals(None, loginBinding("admin", "admin"))
    //3rd party users should not login like register user
    Assert.assertEquals(None, loginBinding("sinaIdError", WebSite.SINA))
  }

  def loginBinding(bindingId: String, website: String): Option[User] = {
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
