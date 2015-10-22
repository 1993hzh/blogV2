import models.{RoleType, Role, User}
import org.junit.{Before, Assert, Test}
import utils.Encryption

/**
 * Created by Leo.
 * 2015/10/22 22:55
 */
class UserTest extends AbstractTest {

  @Before
  def insertUserLikeRegister() = {
    val user = User("Test", "Test", "test@test.com")
    em.getTransaction.begin
    em.persist(user)

    val role = Role(user)
    user.setRole(role)
    em.persist(role)
    em.getTransaction.commit

    assertUser(user)
    assertRole(user.getRole, user)
  }

  @Test
  def queryUserLikeRegister() = {
    val user = em.createQuery("from User where userName='Test'").getSingleResult.asInstanceOf[User]
    assertUser(user)
    assertRole(user.getRole, user)
  }

  def assertUser(user: User) = {
    Assert.assertEquals("Test", user.getUserName)
    Assert.assertEquals(Encryption.encodeBySHA1("Test"), user.getPassword)
    Assert.assertEquals("test@test.com", user.getMail)
  }

  def assertRole(role: Role, user: User) = {
    Assert.assertEquals(RoleType.COMMON.toString, role.getRoleType)
    Assert.assertEquals("", role.getWebSite)
    Assert.assertEquals(user, role.getUser)
  }

  @Test
  def insertUserLikeBinding() = {

  }
}
