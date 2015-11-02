package dao

import javax.inject.Singleton

import models.Comment
import slick.lifted.TableQuery
import tables.CommentTable

/**
 * Created by leo on 15-11-2.
 */
@Singleton()
class CommentDAO extends AbstractDAO[Comment] with CommentTable {


  override type T = CommentTable
  override protected val modelQuery = TableQuery[CommentTable]

  import driver.api._


}

object CommentDAO {
  val comments = new CommentDAO().modelQuery

  def apply() = {
    new CommentDAO()
  }
}