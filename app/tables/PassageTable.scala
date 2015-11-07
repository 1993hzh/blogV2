package tables

import java.sql.Timestamp

import dao.UserDAO
import models.Passage
import slick.lifted.ProvenShape

/**
 * Created by Leo.
 * 2015/11/1 20:35
 */
trait PassageTable extends AbstractTable[Passage] {

  import driver.api._

  class PassageTable(tag: Tag) extends AbstractTable(tag, "t_passage") {

    def authorId = column[Int]("author_id")

    def authorName = column[String]("author_name")

    def title = column[String]("title")

    def content = column[String]("content")

    def createTime = column[Timestamp]("createtime")

    def viewCount = column[Int]("viewcount")

    def authorFK = foreignKey("author_fk", authorId, UserDAO.users)(_.id)

    override def * : ProvenShape[Passage] = (id, authorId, authorName, title, content, createTime, viewCount) <>
      (Passage.tupled, Passage.unapply)
  }

}
