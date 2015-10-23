import javax.persistence.{EntityManager}
import javax.transaction.Transactional

import models.test.TestModel
import org.junit.{Assert, Test, Before}
import play.db.jpa.{JPA}
import play.test.WithApplication


/**
 * Created by leo on 15-10-20.
 */
class ModelTest extends AbstractTest {

  @Before
  def insert() = {
    val tm: TestModel = TestModel()
    tm.name = "test"
    em.getTransaction.begin
    em.persist(tm)
    em.getTransaction.commit
  }

  @Test
  def query() = {
    val tm: TestModel = em.createQuery("from TestModel").getResultList.get(0).asInstanceOf[TestModel]
    Assert.assertEquals("test", tm.name)
  }
}