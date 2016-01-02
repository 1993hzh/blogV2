package models

import java.util.Date

/**
 * Created by Leo.
 * 2015/11/1 19:48
 */
case class Passage(id: Int,
                   authorId: Int,
                   authorName: String,
                   title: String,
                   content: String,
                   createTime: Date = new Date(),
                   viewCount: Int = 0
                    ) extends AbstractModel