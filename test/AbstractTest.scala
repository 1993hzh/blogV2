import dao._
import org.junit.Before
import play.test.WithApplication

/**
 * Created by Leo.
 * 2015/10/22 22:55
 */
class AbstractTest extends WithApplication {

  // remember that lazy should be added to prevent the DAO eager fetch
  // eager fetch will result in the unit case fail with `Play.current` empty, represented as `no application start`
  lazy val modelDAO = TestModelDAO()
  lazy val tagDAO = TagDAO()
  lazy val roleDAO = RoleDAO()
  lazy val userDAO = UserDAO()
  lazy val passageDAO = PassageDAO()
  lazy val keywordDAO = KeywordDAO()
  lazy val commentDAO = CommentDAO()
  lazy val passage2TagDAO = Passage2TagDAO()

  @Before
  def init() = {

  }
}
