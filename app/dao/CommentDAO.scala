package dao

import javax.inject.Singleton

import controllers.Application
import models.Comment
import slick.lifted.TableQuery
import tables.CommentTable

import scala.concurrent.{Await, Future}

/**
  * Created by leo on 15-11-2.
  */
@Singleton()
class CommentDAO extends AbstractDAO[Comment] with CommentTable {


  override type T = CommentTable
  override protected val modelQuery = TableQuery[CommentTable]

  import driver.api._

  /**
    *
    * @param num
    * @param pageSize
    * @param userId
    * @return
    */
  private def getInMessagesByLoginUser(num: Int, pageSize: Int = Application.PAGE_SIZE, userId: Int): Future[Seq[(Comment, String)]] = {
    val query = getInMessageQuery(userId)
    val action = (query).map(f => (f._1, f._2.title)).sortBy(_._1.createTime.desc)
      .drop((num - 1) * pageSize).take(pageSize).result

    db.run(action)
  }

  def getInMessagesByLoginUserSync(num: Int, pageSize: Int = Application.PAGE_SIZE, userId: Int): List[(Comment, String)] = {
    val result = getInMessagesByLoginUser(num, pageSize, userId)
    Await.result(result, waitTime).toList
  }

  private def getInMessageQuery(userId: Int) = {
    val replies = modelQuery.filter(_.toId === userId).join(PassageDAO.passages).on(_.passageId === _.id)
    val commentsToPassage = modelQuery.join(PassageDAO.passages).on(_.passageId === _.id)
      .filter(r => {
        r._2.authorId === userId && r._1.toId.isEmpty && r._1.toName.isEmpty
      })
    replies ++ commentsToPassage
  }

  private def getInMessagesCount(userId: Int): Future[Int] = db.run(getInMessageQuery(userId).length.result)

  def getInMessagesCountSync(userId: Int): Int = Await.result(getInMessagesCount(userId), waitTime)

}

object CommentDAO {
  val comments = new CommentDAO().modelQuery

  def apply() = {
    new CommentDAO()
  }
}