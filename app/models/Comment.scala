package models

import java.sql.Timestamp

/**
  * Created by Leo.
  * 2015/11/1 20:06
  */
case class Comment(id: Int,
                   content: String,
                   passageId: Int,
                   createTime: Timestamp,
                   fromId: Int,
                   fromName: String,
                   status: String = CommentStatus.unread,
                   toId: Option[Int] = None,
                   toName: Option[String] = None) extends AbstractModel

object CommentStatus {
  val unread = "UNREAD"
  val read = "READ"
  val commented = "COMMENTED"
  val deleted = "DELETED"

  def getStatus(status: String) = {
    status match {
      case CommentStatus.unread => unread
      case CommentStatus.read => read
      case CommentStatus.commented => commented
      case CommentStatus.deleted => deleted
      case _ => unread
    }
  }
}
