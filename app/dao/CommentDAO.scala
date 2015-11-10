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
    val action = modelQuery.filter(_.toId === userId)
      .join(PassageDAO.passages).on(_.passageId === _.id)
      .map(f => (f._1, f._2.title))
      .drop((num - 1) * pageSize).take(pageSize)
      .sortBy(_._1.createTime.desc).result

    db.run(action)
  }

  def getInMessagesByLoginUserSync(num: Int, pageSize: Int = Application.PAGE_SIZE, userId: Int): List[(Comment, String)] = {
    val result = getInMessagesByLoginUser(num, pageSize, userId)
    Await.result(result, waitTime).toList
  }

  private def getInMessagesCount(userId: Int): Future[Int] = db.run(modelQuery.filter(_.toId === userId).length.result)

  def getInMessagesCountSync(userId: Int): Int = Await.result(getInMessagesCount(userId), waitTime)

}

object CommentDAO {
  val comments = new CommentDAO().modelQuery

  def apply() = {
    new CommentDAO()
  }
}