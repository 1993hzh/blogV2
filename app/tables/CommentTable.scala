package tables

import java.sql.Timestamp

import dao.{PassageDAO, UserDAO}
import models.Comment
import slick.lifted

/**
 * Created by Leo.
 * 2015/11/1 20:44
 */
trait CommentTable extends AbstractTable[Comment] {

  import driver.api._

  class CommentTable(tag: lifted.Tag) extends AbstractTable(tag, "t_comment") {

    def content = column[String]("content")

    def fromId = column[Int]("from_id")

    //can be null if the commenter is replying to the passage
    def toId = column[Int]("to_id")

    def time = column[Timestamp]("time")

    def passageId = column[Int]("passage_id")

    def fromFK = foreignKey("from_fk", fromId, UserDAO.users)(_.id)

    def toFK = foreignKey("to_fk", toId, UserDAO.users)(_.id)

    def passageFK = foreignKey("passage_fk", passageId, PassageDAO.passages)(_.id)

    override def * = (id, content, fromId, toId, time, passageId) <>(Comment.tupled, Comment.unapply)
  }

}