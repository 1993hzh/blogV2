import dao.{UserDAO, RoleDAO}
import models.{WebSite, RoleType, Role, User}
import org.junit.{After, Before, Assert, Test}
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


  @Before
  def insertUserLikeRegister() = {
    val adminRole = Role(0, RoleType.OWNER)
    RoleDAO.insert(adminRole) onComplete {
      case Failure(f) => Assert.fail(f.getLocalizedMessage)
      case Success(result) => {
        val admin = User(0, "admin", Encryption.encodeBySHA1("admin"), "admin@test.com", result)
        UserDAO.insert(admin) onComplete {
          case Failure(f) => Assert.fail(f.getLocalizedMessage)
          case Success(result) =>
        }
      }
    }
  }

  @Before
  def insertUserLikeBinding(): Unit = {
    val sinaRole = Role(0, RoleType.COMMON, WebSite.SINA)
    val roleId = Await.result(RoleDAO.insert(sinaRole), Duration.Inf)

    val sinaUser = User(0, "sina", Encryption.encodeBySHA1("sina"), "sina@test.com", roleId)
    Await.result(UserDAO.insert(sinaUser), Duration.Inf)
  }

  @Test
  def queryUserLikeRegister() = {
    val admin = Await.result(UserDAO.queryByUserName("admin"), Duration.Inf)

    val adminRole = Await.result(RoleDAO.query(admin.roleId), Duration.Inf)

    assertUser(admin, adminRole)
  }

  def assertUser(user: User, role: Role) = {
    Assert.assertEquals("admin", user.userName)
    Assert.assertEquals(Encryption.encodeBySHA1("admin"), user.password)
    Assert.assertEquals("admin@test.com", user.mail)
    Assert.assertEquals(role.id, user.roleId)
  }

  @Test
  def queryUserLikeBinding() = {

  }

  @After
  def deleteUserAndRole(): Unit = {
    Await.result(UserDAO.deleteByUserName("admin"), Duration.Inf)
    Await.result(UserDAO.deleteByUserName("sina"), Duration.Inf)

    Await.result(RoleDAO.deleteByRoleTypeAndWebsite(RoleType.OWNER, WebSite.MY), Duration.Inf)
    Await.result(RoleDAO.deleteByRoleTypeAndWebsite(RoleType.COMMON, WebSite.SINA), Duration.Inf)
  }
}
