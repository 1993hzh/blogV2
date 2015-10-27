import dao.TestModelDAO
import models.{TestModel}
import org.junit.{Assert, Test, Before}
import play.Logger
import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by leo on 15-10-20.
 */
class ModelTest extends AbstractTest {
  lazy val dao = new TestModelDAO()

  @Before
  def initSelf(): Unit = {
    truncateTable

    val test1 = TestModel(0, "test1")
    dao.create(test1) onComplete {
      case Failure(f) => Logger.error(f.getMessage); Assert.fail
      case _ =>
    }

    val test2 = TestModel(0, "test2")
    dao.create(test2) onComplete {
      case Failure(f) => Logger.error(f.getMessage); Assert.fail
      case _ =>
    }

  }

  def truncateTable() = {
    dao.delete("test1")
    dao.delete("test2")
  }

  @Test
  def query() = {
    val tm = dao.query("test1")
    tm onComplete {
      case Success(result) => Assert.assertEquals("test1", result.name)
      case Failure(f) => Logger.error(f.getMessage); Assert.fail
    }
  }

}

