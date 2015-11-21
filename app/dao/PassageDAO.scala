package dao

import javax.inject.Singleton
import controllers.Application
import models.{Tag => MyTag, Passage2Tag, Comment, Keyword, Passage}
import play.api.Logger
import slick.jdbc.GetResult
import slick.lifted.TableQuery
import scala.concurrent.{Await, Future}
import tables.PassageTable
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Leo.
 * 2015/11/1 20:50
 */
@Singleton()
class PassageDAO extends AbstractDAO[Passage] with PassageTable {

  private lazy val log = Logger

  private lazy val passage2TagDAO = Passage2TagDAO()

  private lazy val keywordDAO = KeywordDAO()

  override protected val modelQuery = TableQuery[PassageTable]

  override type T = PassageTable

  import driver.api._

  def queryTotalCount(userId: Option[Int] = None): Future[Int] = {
    userId match {
      case Some(u) => db.run(modelQuery.filter(_.authorId === u).length.result)
      case None => db.run(modelQuery.length.result)
    }
  }

  def queryTotalCountSync(userId: Option[Int] = None): Int = Await.result(queryTotalCount(userId), waitTime)

  def queryByUserId(userId: Int, num: Int, pageSize: Int = Application.PAGE_SIZE): Seq[Passage] = {
    Await.result(db.run(modelQuery.filter(_.authorId === userId).sortBy(_.createTime.desc)
      .drop((num - 1) * pageSize).take(pageSize).result), waitTime)
  }

  def queryPassages(num: Int, pageSize: Int = Application.PAGE_SIZE,
                    contentMaxLength: Int = 150, userId: Option[Int] = None): Seq[Passage] = {
    val contentPreview = " ..."
    val offSet = (num - 1) * pageSize

    val action = userId match {
      case Some(u) => sql"""
          select p.id, p.author_id, p.author_name, p.title,
          case when length(p.content) > $contentMaxLength then substring(p.content for $contentMaxLength) || $contentPreview
          else p.content || $contentPreview end
          , p.createtime, p.viewcount
          from t_passage p where p.author_id = $userId order by p.createtime desc limit $pageSize offset $offSet
        """.as[Passage]
      case None => sql"""
          select p.id, p.author_id, p.author_name, p.title,
          case when length(p.content) > $contentMaxLength then substring(p.content for $contentMaxLength) || $contentPreview
          else p.content || $contentPreview end
          , p.createtime, p.viewcount
          from t_passage p order by p.createtime desc limit $pageSize offset $offSet
        """.as[Passage]
    }

    Await.result(db.run(action), waitTime)
  }

  def queryKeywordsByPassageId(passageId: Int): Future[Seq[Keyword]] = {
    db.run(modelQuery.filter(_.id === passageId)
      .join(KeywordDAO.keywords).on(_.id === _.passageId).sortBy(_._2.name.desc).map(_._2).result)
  }

  def queryTagsByPassageId(passageId: Int): Future[Seq[MyTag]] = {
    db.run(modelQuery.filter(_.id === passageId)
      .join(Passage2TagDAO.passage2Tags).on(_.id === _.passageId)
      .join(TagDAO.tags).on(_._2.tagId === _.id).sortBy(_._2.name.asc).map(_._2).result)
  }

  def queryCommentsByPassageId(passageId: Int): Future[Seq[Comment]] = {
    db.run(modelQuery.filter(_.id === passageId).
      join(CommentDAO.comments).on(_.id === _.passageId).sortBy(_._2.createTime.asc).map(_._2).result)
  }

  private def getAuthorNameWithPassageId(passageId: Int): Future[Option[String]] = {
    db.run(modelQuery.filter(_.id === passageId)
      .join(UserDAO.users).on(_.authorId === _.id).map(_._2.userName).result.headOption)
  }

  def getAuthorNameWithPassageIdSync(passageId: Int): String = {
    Await.result(getAuthorNameWithPassageId(passageId), waitTime).getOrElse("")
  }

  def getKeywords(passageId: Int): List[Keyword] = Await.result(queryKeywordsByPassageId(passageId), waitTime).toList

  def getComments(passageId: Int): List[Comment] = Await.result(queryCommentsByPassageId(passageId), waitTime).toList

  def getDetail(id: Int): Option[(Passage, List[Keyword], List[Comment])] = {
    Await.result(query(id), waitTime) match {
      case Some(passage) => Some(passage, getKeywords(id), getComments(id))
      case None =>
        log.info("Passage: " + id + " not found!")
        None
    }
  }

  def getTags(passageId: Int): List[MyTag] = Await.result(queryTagsByPassageId(passageId), waitTime).toList

  def getPassage(passageId: Int): Option[Passage] = Await.result(query(passageId), waitTime)

  /**
   * make sure the delete request is legal
   * @param id
   * @return
   */
  def delete(userId: Int, id: Int): Int = {
    if (isAuthorized(userId, id))
      Await.result(super.delete(id), waitTime)
    else {
      log.warn("user: " + userId + " try to delete passage: " + id)
      0
    }
  }

  def isAuthorized(userId: Int, id: Int): Boolean = {
    getPassage(id) match {
      case Some(p) =>
        p.authorId.equals(userId)
      case None => false
    }
  }

  def updateViewCount(id: Int, viewCount: Int): Future[Int] = {
    log.info(Application.now + ": passage: " + id + " start to sync up view count:" + viewCount)

    val action = modelQuery.filter(_.id === id).map(_.viewCount).update(viewCount)
    db.run(action)
  }

  def insert(model: Passage, keywords: List[String], tagIds: List[Int]): Future[Int] = {
    super.insert(model) map {
      case r: Int if r > 0 =>
        keywords.foreach(kw => keywordDAO.insert(Keyword(0, kw, r)))
        tagIds.foreach(tid => passage2TagDAO.insert(Passage2Tag(0, r, tid)))
        r
      case r: Int if r <= 0 => r
    }
  }

  def update(model: Passage, keywords: List[String], tagIds: List[Int]): Future[Boolean] = {
    this.update(model) map {
      case r: Int if r > 0 =>
        val originKeywords = getKeywords(model.id)
        val originTags = getTags(model.id)

        originKeywords.foreach(okw => {
          if (!keywords.contains(okw.name))
            keywordDAO.delete(okw) map {
              case r: Int if r <= 0 => log.warn("Delete keyword: " + okw + " failed.")
            }
        })
        keywords.foreach(kw => {
          if (!originKeywords.map(_.name).contains(kw))
            keywordDAO.insert(Keyword(0, kw, model.id)) map {
              case r: Int if r <= 0 => log.warn("Create keyword: " + kw + " failed.")
            }
        })

        originTags.foreach(ot => {
          if (!tagIds.contains(ot.id))
            passage2TagDAO.remove(model.id, ot.id) map {
              case r: Int if r <= 0 => log.warn("Delete Passage2Tag relationship: (" + model.id + ", " + ot.id + ") failed.")
            }
        })
        tagIds.foreach(t => {
          if (!originTags.map(_.id).contains(t))
            passage2TagDAO.insert(Passage2Tag(0, model.id, t)) map {
              case r: Int if r <= 0 => log.warn("Create Passage2Tag relationship: (" + model.id + ", " + t + ") failed.")
            }
        })

        true
      case r: Int if r <= 0 => false
    }
  }

  override def update(model: Passage): Future[Int] = {
    val action = modelQuery.filter(_.id === model.id).map(p => (p.title, p.content)).update((model.title, model.content))
    db.run(action)
  }

  @deprecated("passage upsert should not be block", "2015-15-17 (no version num)")
  def getUpsertResultSync(passageId: Option[Int], passage: Passage, keywords: List[String], tagIds: List[Int]): AnyVal = {
    val result = passageId match {
      case Some(id) =>
        update(passage, keywords, tagIds)
      case None =>
        insert(passage, keywords, tagIds)
    }
    Await.result(result, waitTime)
  }

  implicit val listPassagesResult = GetResult(
    r => (
      Passage(r.nextInt, r.nextInt, r.nextString, r.nextString, r.nextString, r.nextTimestamp, r.nextInt)
      )
  )
}

object PassageDAO {
  val passages = new PassageDAO().modelQuery

  def apply() = {
    new PassageDAO()
  }
}