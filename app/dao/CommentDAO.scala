package dao

import javax.inject.Singleton

import controllers.Application
import models.{CommentStatus, Comment}
import play.api.Logger
import slick.lifted.TableQuery
import tables.CommentTable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{Await, Future}

/**
 * Created by leo on 15-11-2.
 */
@Singleton()
class CommentDAO extends AbstractDAO[Comment] with CommentTable {


  override type T = CommentTable
  override protected val modelQuery = TableQuery[CommentTable]

  import driver.api._

  private def getInMessageQuery(userId: Int) = {
    val replies = modelQuery.filter(_.toId === userId).join(PassageDAO.passages).on(_.passageId === _.id)
    val commentsToPassage = modelQuery.join(PassageDAO.passages).on(_.passageId === _.id)
      .filter(r => {
        r._2.authorId === userId && r._1.toId.isEmpty && r._1.toName.isEmpty
      })
    replies ++ commentsToPassage
  }

  /**
   *
   * @param num
   * @param pageSize
   * @param userId
   * @return
   */
  private def getInMessagesByLoginUser(num: Int, pageSize: Int = Application.PAGE_SIZE, userId: Int): Future[Seq[(Comment, String)]] = {
    val query = getInMessageQuery(userId)
    val action = query.map(f => (f._1, f._2.title)).sortBy(_._1.createTime.desc)
      .drop((num - 1) * pageSize).take(pageSize).result

    db.run(action)
  }

  def getInMessagesByLoginUserSync(num: Int, pageSize: Int = Application.PAGE_SIZE, userId: Int): List[(Comment, String)] = {
    val result = getInMessagesByLoginUser(num, pageSize, userId)
    Await.result(result, waitTime).toList
  }

  private def getInMessagesCount(userId: Int): Future[Int] = db.run(getInMessageQuery(userId).length.result)

  def getInMessagesCountSync(userId: Int): Int = Await.result(getInMessagesCount(userId), waitTime)

  private def getUnreadInMessagesCount(userId: Int): Future[Int] = {
    db.run(getInMessageQuery(userId).map(_._1).filter(f => f.status === CommentStatus.unread).length.result)
  }

  def getUnreadInMessagesCountSync(userId: Int): Int = Await.result(getUnreadInMessagesCount(userId), waitTime)

  private def markAs(commentId: Int, status: String = CommentStatus.read): Future[Int] = {
    val action = modelQuery.filter(c => c.id === commentId).map(_.status).update(status)
    db.run(action)
  }

  private def marksAs(commentId: Set[Int], status: String = CommentStatus.read): Future[Int] = {
    val action = modelQuery.filter(c => c.id inSet commentId).map(_.status).update(status)
    db.run(action)
  }

  def markAsSync(commentId: Int, status: String = CommentStatus.read): Boolean = {
    Await.result(markAs(commentId, status), waitTime) match {
      case r: Int if r == 1 => true
      case _ => Logger.info("Comment: " + commentId + " mark as " + status + " failed"); false
    }
  }

  def marksAsSync(commentIds: Set[Int], status: String = CommentStatus.read): Boolean = {
    Await.result(marksAs(commentIds, status), waitTime) match {
      case r: Int if r == 1 => true
      case _ => Logger.info("Comment: " + commentIds + " mark as " + status + " failed"); false
    }
  }

  private def markAllAsRead(userId: Int): Future[Int] = {
    //I think it's a bug
    //    val action = getInMessageQuery(userId).map(_._1).filter(_.status === CommentStatus.unread)
    //      .map(_.status).update(CommentStatus.read)
    val action =
      sql"""
       update t_comment set status = 'READ' where id in
       (select c1.id from t_comment c1 where c1.to_id = $userId and c1.status = 'UNREAD'
       union
       select c2.id from t_comment c2 join t_passage p on c2.passage_id = p.id
       where c2.to_id is null and c2.to_name is null and c2.status = 'UNREAD' and p.author_id = $userId);
      """.asUpdate
    db.run(action)
  }

  def markAllAsReadSync(userId: Int): Int = {
    Await.result(markAllAsRead(userId), waitTime)
  }

}

object CommentDAO {
  val comments = new CommentDAO().modelQuery

  def apply() = {
    new CommentDAO()
  }
}