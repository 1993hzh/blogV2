package dao

import javax.inject.Singleton
import controllers.Application
import models.{Tag => MyTag, Comment, Keyword, Passage}
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

  def queryPassages(num: Int, pageSize: Int = Application.PAGE_SIZE): Seq[(Passage, String)] = {
    //    val action = modelQuery.join(UserDAO.users).on(_.authorId === _.id)
    //      .sortBy(_._1.createTime.desc).map(f => (f._1, f._2.userName))
    //      .drop((num - 1) * pageSize).take(pageSize)
    //      .result
    val maxLength = 110
    val contentPreview = " ... [Click to see details]"
    val offSet = (num - 1) * pageSize + 1

    val action = sql"""
      select p.id, p.author_id, p.title,
      case when length(p.content) > $maxLength then substring(p.content for $maxLength) || $contentPreview
      else p.content || $contentPreview
      end
      , p.createtime, p.viewcount, u.username
      from t_passage p right join t_user u on p.author_id = u.id order by p.createtime desc limit $pageSize offset $offSet
      """.as[(Passage, String)]

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

  implicit val listPassagesResult = GetResult(
    r => (Passage(r.nextInt, r.nextInt, r.nextString, r.nextString, r.nextTimestamp, r.nextInt), r.nextString))

}

object PassageDAO {
  val passages = new PassageDAO().modelQuery

  def apply() = {
    new PassageDAO()
  }
}