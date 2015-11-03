import java.sql.Timestamp

import dao._
import models._
import org.junit.{Assert, After, Test, Before}
import utils.Encryption
import play.Logger
import scala.collection.mutable.ArrayBuffer
import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by leo on 15-11-2.
  */
class PassageTest extends AbstractTest {


  lazy val tagDAO = TagDAO()
  lazy val roleDAO = RoleDAO()
  lazy val userDAO = UserDAO()
  lazy val passageDAO = PassageDAO()
  lazy val keywordDAO = KeywordDAO()
  lazy val commentDAO = CommentDAO()
  lazy val passage2TagDAO = Passage2TagDAO()

  val PASSAGE_SIZE = 22
  var tagIds: List[Int] = null
  var adminId = 0
  var userId = 0
  var passageIds = Array.emptyIntArray

  @Before
  def initSelf(): Unit = {
    tagIds = initTag
    adminId = initAdmin
    userId = initUser
    passageIds = initPassage(adminId)

    passageIds.foreach(p => {
      tagIds.foreach(e => initPassage2Tag(p, e))
      initKeyword(p)
      initComment(userId, adminId, p)
    })
  }

  @Test
  def queryByUserId(): Unit = {
    queryForPagination(1)
    queryForPagination(2)
    queryForPagination(3)
  }

  @Test
  def queryKeywordsByPassageId(): Unit = {
    passageIds.foreach(p => {
      val keywords: Seq[Keyword] = Await.result(passageDAO.queryKeywordsByPassageId(p), Duration.Inf)
      Logger.info(keywords.toString)
      assertKeyword(keywords, p)
    })
  }

  @Test
  def queryTagsByPassageId(): Unit = {
    passageIds.foreach(p => {
      val tags: Seq[Tag] = Await.result(passageDAO.queryTagsByPassageId(p), Duration.Inf)
      Logger.info(tags.toString)
      assertTag(tags, p)
    })
  }

  @Test
  def queryCommentsByPassageId(): Unit = {
    passageIds.foreach(p => {
      val comments: Seq[Comment] = Await.result(passageDAO.queryCommentsByPassageId(p), Duration.Inf)
      Logger.info(comments.toString)
      assertComment(comments, p)
    })
  }

  @After
  def destorySelf(): Unit = {
    tagIds.foreach(e => Await.result(tagDAO.delete(e), Duration.Inf))
    passageIds.foreach(p => Await.result(passageDAO.delete(p), Duration.Inf))

    Await.result(userDAO.delete(adminId), Duration.Inf)
    Await.result(userDAO.delete(userId), Duration.Inf)

    Await.result(roleDAO.deleteByRoleTypeAndWebsite(RoleType.OWNER, WebSite.MY), Duration.Inf)
    Await.result(roleDAO.deleteByRoleTypeAndWebsite(RoleType.COMMON, WebSite.SINA), Duration.Inf)
  }

  def queryForPagination(num: Int) = {
    val result = Await.result(passageDAO.queryByUserId(adminId, num), Duration.Inf)

    val expectCount = if (num <= PASSAGE_SIZE / 10) 10 else PASSAGE_SIZE % 10
    Assert.assertEquals(expectCount, result.size)

    var i = PASSAGE_SIZE - (num - 1) * 10
    result.foreach(e => {
      assertPassage(e, i)
      i -= 1
    })

    result.foreach(r => Logger.info(r.toString))
  }

  def assertKeyword(kws: Seq[Keyword], p: Int) = {
    Assert.assertEquals(List("kw2", "kw1"), kws.map(_.name).toList)
    Assert.assertEquals(p, kws.head.passageId)
  }

  def assertTag(tags: Seq[Tag], p: Int) = {
    Assert.assertEquals("tag2", tags.head.name)
    Assert.assertEquals("tag2", tags.head.description)

    Assert.assertEquals("tag1", tags.tail.head.name)
    Assert.assertEquals("tag1", tags.tail.head.description)
  }

  def assertComment(comments: Seq[Comment], p: Int) = {
    Assert.assertEquals("comment1", comments.head.content)
    Assert.assertEquals(userId, comments.head.fromId)
    Assert.assertEquals(None, comments.head.toId)
    Assert.assertEquals(p, comments.head.passageId)

    Assert.assertEquals("reply2comment1", comments.tail.head.content)
    Assert.assertEquals(adminId, comments.tail.head.fromId)
    Assert.assertEquals(Some(userId), comments.tail.head.toId)
    Assert.assertEquals(p, comments.tail.head.passageId)
  }

  def assertPassage(p: Passage, index: Int) = {
    Assert.assertEquals(adminId, p.authorId)
    Assert.assertEquals("title" + index, p.title)
    Assert.assertEquals("content" + index, p.content)
  }

  def initTag() = {
    val tag1 = Tag(0, "tag1", "tag1")
    val id1 = Await.result(tagDAO.insert(tag1), Duration.Inf)

    val tag2 = Tag(0, "tag2", "tag2")
    val id2 = Await.result(tagDAO.insert(tag2), Duration.Inf)

    List(id1, id2)
  }

  def initAdmin(): Int = {
    val adminRole = Role(0, RoleType.OWNER)
    val roleId = Await.result(roleDAO.insert(adminRole), Duration.Inf)

    val admin = User(0, "admin", Encryption.encodeBySHA1("admin_admin"), "admin@test.com", roleId)
    Await.result(userDAO.insert(admin), Duration.Inf)
  }

  def initUser(): Int = {
    val commonUserRole = Role(0, RoleType.COMMON, WebSite.SINA)
    val roleId = Await.result(roleDAO.insert(commonUserRole), Duration.Inf)

    val commonUser = User(0, "commonUser", Encryption.encodeBySHA1("commonUser"), "commonUser@test.com", roleId, bindingId = Some("sinaId"))
    Await.result(userDAO.insert(commonUser), Duration.Inf)
  }

  def initPassage(authorId: Int): Array[Int] = {
    val ids = ArrayBuffer[Int]()
    for (i <- 1 to PASSAGE_SIZE) {
      val passage = Passage(0, authorId, "title" + i, "content" + i, new Timestamp(System.currentTimeMillis()))
      ids += Await.result(passageDAO.insert(passage), Duration.Inf)
    }
    ids.toArray
  }

  def initPassage2Tag(passageId: Int, tagId: Int) = {
    val p2t = Passage2Tag(0, passageId = passageId, tagId = tagId)
    Await.result(passage2TagDAO.insert(p2t), Duration.Inf)
  }

  def initKeyword(passageId: Int) = {
    val keyword1 = Keyword(0, "kw1", passageId)
    val id1 = Await.result(keywordDAO.insert(keyword1), Duration.Inf)

    val keyword2 = Keyword(0, "kw2", passageId)
    val id2 = Await.result(keywordDAO.insert(keyword2), Duration.Inf)

    List(id1, id2)
  }

  def initComment(userId: Int, adminId: Int, passageId: Int) = {
    val comment1 = Comment(0, "comment1", passageId, new Timestamp(System.currentTimeMillis()), userId)
    val id1 = Await.result(commentDAO.insert(comment1), Duration.Inf)

    val comment2 = Comment(0, "reply2comment1", passageId, new Timestamp(System.currentTimeMillis()), adminId, Some(userId))
    val id2 = Await.result(commentDAO.insert(comment2), Duration.Inf)

    List(id1, id2)
  }

}
