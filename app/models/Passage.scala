package models

import java.sql.Timestamp

/**
 * Created by Leo.
 * 2015/11/1 19:48
 */
case class Passage(id: Int,
                   authorId: Int,
                   title: String,
                   content: String,
                   createTime: Timestamp,
                   viewCount: Int
                    ) extends AbstractModel