import javax.persistence.{PersistenceContext, EntityManager}

import org.junit.Before
import play.db.jpa.JPA
import play.test.WithApplication

/**
 * Created by Leo.
 * 2015/10/22 22:55
 */
class AbstractTest extends WithApplication {
  val ENTITY_MANAGER = "blog"
  var em: EntityManager = null

  @Before
  def init() = {
    em = JPA.em(ENTITY_MANAGER)
  }
}
