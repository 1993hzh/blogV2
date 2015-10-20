import javax.persistence.{EntityManager}
import javax.transaction.Transactional

import models.test.TestModel
import org.junit.{Test, Before}
import play.db.jpa.{JPA}
import play.test.WithApplication


/**
 * Created by leo on 15-10-20.
 */
class ModelTest extends WithApplication {

  val ENTITY_MANAGER = "blog"
  var em: EntityManager = null

  @Before
  def init() = {
    em = JPA.em(ENTITY_MANAGER)
    insert
  }

  def insert() = {
    val tm: TestModel = TestModel()
    tm.name = "test"
    em.getTransaction.begin
    em.persist(tm)
    em.getTransaction.commit
  }

  @Test
  def query() = {
    val tm: TestModel = em.createQuery("from TestModel").getSingleResult.asInstanceOf[TestModel]
    println(tm.name)
  }
}