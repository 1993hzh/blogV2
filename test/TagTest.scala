import models.Tag
import org.junit.{Assert, After, Before, Test}
import play.Logger

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Created by Leo.
 * 2015/11/1 21:27
 */
class TagTest extends AbstractTest {

  private var tagId = 0

  @Before
  def create(): Unit = {
    val tag = Tag(0, "tagTest", "tagTest")
    val id = Await.result(tagDAO.insert(tag), Duration.Inf)
    tagId = id
  }

  @Test
  def update(): Unit = {
    val tagName = "tagTest"
    val tag = Await.result(tagDAO.queryByName(tagName), Duration.Inf)
    tag match {
      case Some(t) => {
        val newTag = Tag(t.id, "tagTest_updated", "tagTest_updated")
        val i = Await.result(tagDAO.update(newTag), Duration.Inf)
        Logger.trace(i + ": i don't know what is this")
      }
      case None => Assert.fail(tagName + " not found")
    }
  }

  @Test
  def query(): Unit = {
    val tag = Await.result(tagDAO.query(tagId), Duration.Inf)
    tag match {
      case Some(t) => assertTag(t)
      case None => Assert.fail(tagId + " not found")
    }
  }

  @After
  def delete(): Unit = {
    val tag = Await.result(tagDAO.query(tagId), Duration.Inf)
    tag match {
      case Some(t) => {
        val i = Await.result(tagDAO.delete(t), Duration.Inf)
        Assert.assertEquals(1, i)
      }
      case None => Assert.fail(tagId + " not found")
    }
  }

  def assertTag(tag: Tag) = {
    Assert.assertEquals(tagId, tag.id)
    Assert.assertEquals("tagTest", tag.name)
    Assert.assertEquals("tagTest", tag.description)
  }
}
