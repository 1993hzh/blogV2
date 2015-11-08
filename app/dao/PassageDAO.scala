package dao

import javax.inject.Singleton
import controllers.Application
import models.{Tag => MyTag, Comment, Keyword, Passage}
import play.api.Logger
import slick.jdbc.GetResult
import slick.lifted.TableQuery
import scala.concurrent.{Await, Future}
import tables.PassageTable

/**
 * Created by Leo.
 * 2015/11/1 20:50
 */
@Singleton()
class PassageDAO extends AbstractDAO[Passage] with PassageTable {

  override protected val modelQuery = TableQuery[PassageTable]

  override type T = PassageTable

  import driver.api._

  def queryTotalCount(): Future[Int] = {
    db.run(modelQuery.length.result)
  }

  def queryByUserId(userId: Int, num: Int, pageSize: Int = Application.PAGE_SIZE): Seq[Passage] = {
    Await.result(db.run(modelQuery.filter(_.authorId === userId).sortBy(_.createTime.desc)
      .drop((num - 1) * pageSize).take(pageSize).result), waitTime)
  }

  def queryPassages(num: Int, pageSize: Int = Application.PAGE_SIZE): Seq[Passage] = {
    //    val action = modelQuery.join(UserDAO.users).on(_.authorId === _.id)
    //      .sortBy(_._1.createTime.desc).map(f => (f._1, f._2.userName))
    //      .drop((num - 1) * pageSize).take(pageSize)
    //      .result
    val maxLength = 110
    val contentPreview = " ... [Click to see details]"
    val offSet = (num - 1) * pageSize

    val action = sql"""
      select p.id, p.author_id, p.author_name, p.title,
      case when length(p.content) > $maxLength then substring(p.content for $maxLength) || $contentPreview
      else p.content || $contentPreview end
      , p.createtime, p.viewcount
      from t_passage p order by p.createtime desc limit $pageSize offset $offSet
      """.as[Passage]

    Await.result(db.run(action), waitTime)
  }

  def queryKeywordsByPassageId(passageId: Int): Future[Seq[Keyword]] = {
    db.run(modelQuery.filter(_.id === passageId)
      .join(KeywordDAO.keywords).on(_.id === _.passageId).sortBy(_._2.name.desc).map(_._2).result)
  }

  def queryTagsByPassageId(passageId: Int): Future[Seq[MyTag]] = {
    db.run(modelQuery.filter(_.id === passageId)
      .join(Passage2TagDAO.passage2Tags).on(_.id === _.passageId)
      .join(TagDAO.tags).on(_._2.tagId === _.id).sortBy(_._2.name.desc).map(_._2).result)
  }

  def queryCommentsByPassageId(passageId: Int): Future[Seq[Comment]] = {
    db.run(modelQuery.filter(_.id === passageId).
      join(CommentDAO.comments).on(_.id === _.passageId).sortBy(_._2.createTime.asc).map(_._2).result)
  }

  def getKeywords(passageId: Int): List[Keyword] = {
    Await.result(queryKeywordsByPassageId(passageId), waitTime).toList
  }

  def getComments(passageId: Int): List[Comment] = {
    Await.result(queryCommentsByPassageId(passageId), waitTime).toList
  }

  def getDetail(id: Int): Option[(Passage, List[Keyword], List[Comment])] = {
    Await.result(query(id), waitTime) match {
      case Some(passage) => Some(passage, getKeywords(id), getComments(id))
      case None =>
        Logger.info("Passage: " + id + " not found!")
        None
    }
  }

  def getTags(passageId: Int): List[MyTag] = {
    Await.result(queryTagsByPassageId(passageId), waitTime).toList
  }

  implicit val listPassagesResult = GetResult(
    r => (Passage(r.nextInt, r.nextInt, r.nextString, r.nextString, r.nextString, r.nextTimestamp, r.nextInt))
  )
}

object PassageDAO {
  val passages = new PassageDAO().modelQuery

  def apply() = {
    new PassageDAO()
  }
}