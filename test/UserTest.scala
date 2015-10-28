import dao.{UserDAO, RoleDAO}
import models.{WebSite, RoleType, Role, User}
import org.junit.{After, Before, Assert, Test}
import utils.Encryption
import scala.concurrent.ExecutionContext.Implicits.global
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
        val pwd = Encryption.encodeBySHA1("admin")
        val admin = User(0, "admin", pwd, "admin@test.com", result)
        UserDAO.insert(admin) onComplete {
          case Failure(f) => Assert.fail(f.getLocalizedMessage)
          case Success(result) =>
        }
      }
    }
  }

  @Test
  def queryUserLikeRegister() = {
    UserDAO.queryByUserName("admin") onComplete {
      case Success(result) =>
        val admin = result
        RoleDAO.query(result.roleId) onComplete {
          case Success(result) => assertUser(admin, result)
          case Failure(f) => Assert.fail(f.getLocalizedMessage)
        }
      case Failure(f) => Assert.fail(f.getLocalizedMessage)
    }
  }

  def assertUser(user: User, role: Role) = {
    Assert.assertEquals("admin", user.userName)
    Assert.assertEquals(Encryption.encodeBySHA1("admin"), user.password)
    Assert.assertEquals("admin@test.com", user.mail)
    Assert.assertEquals(role.id, user.roleId)
  }

  //  @Test
  def insertUserLikeBinding() = {

  }

  @After
  def deleteUserAndRole() = {
    UserDAO.deleteByUserName("admin") onComplete {
      case Failure(f) => Assert.fail(f.getLocalizedMessage)
      case Success(result) =>
    }

    RoleDAO.deleteByRoleTypeAndWebsite(RoleType.OWNER, WebSite.MY) onComplete {
      case Failure(f) => Assert.fail(f.getLocalizedMessage)
      case Success(result) =>
    }
  }
}
