package models

import java.sql.Timestamp

/**
 * Created by Leo.
 * 2015/11/1 20:06
 */
case class Comment(id: Int,
                   content: String,
                   fromId: Int,
                   toId: Int,
                   time: Timestamp,
                   passageId: Int) extends AbstractModel
