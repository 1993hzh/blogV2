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

    def passageId = column[Int]("passage_id")

    def createTime = column[Timestamp]("createtime")

    def fromId = column[Int]("from_id")

    //can be null if the commenter is replying to the passage
    def toId = column[Option[Int]]("to_id")

    def fromFK = foreignKey("from_fk", fromId, UserDAO.users)(_.id)

    def toFK = foreignKey("to_fk", toId, UserDAO.users)(_.id)

    def passageFK = foreignKey("passage_fk", passageId, PassageDAO.passages)(_.id, onDelete = ForeignKeyAction.Cascade)

    override def * = (id, content, passageId, createTime, fromId, toId) <>(Comment.tupled, Comment.unapply)
  }

}