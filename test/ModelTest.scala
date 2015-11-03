import models.{TestModel}
import org.junit.{After, Assert, Test, Before}
import play.Logger
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.util.{Success, Failure}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by leo on 15-10-20.
 */
class ModelTest extends AbstractTest {

  @Before
  def insert(): Unit = {
    val test1 = TestModel(0, "test1", "test")
    modelDAO.create(test1) onComplete {
      case Failure(f) => Assert.fail(f.getLocalizedMessage)
      case Success(result) =>
    }

    val test2 = TestModel(0, "test2", "test")
    modelDAO.create(test2) onComplete {
      case Failure(f) => Assert.fail(f.getLocalizedMessage)
      case Success(result) =>
    }
  }

  @Test
  def query() = {
    val result = Await.result(modelDAO.queryName("test1"), Duration.Inf)
    Assert.assertEquals("test1", result.name)
  }

  @Test
  def queryAll = {
    val result = Await.result(modelDAO.all, Duration.Inf)
    result.foreach(e => Logger.info(e.toString))
    Assert.assertEquals(2, result.size)
  }

  @Test
  def update() = {
    val result = Await.result(modelDAO.update("test2", "testUpdate"), Duration.Inf)
    Assert.assertEquals(1, result)
  }

  @After
  def delete(): Unit = {
    modelDAO.delete("test1") onComplete {
      case Success(result) => Assert.assertEquals(1, result)
      case Failure(f) => Assert.fail(f.getLocalizedMessage)
    }

    modelDAO.delete("test2") onComplete {
      case Success(result) => Assert.assertEquals(1, result)
      case Failure(f) => Assert.fail(f.getLocalizedMessage)
    }
  }

}

