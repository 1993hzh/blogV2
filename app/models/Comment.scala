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
                   toId: Option[Int] = None,
                   toName: Option[String] = None) extends AbstractModel
