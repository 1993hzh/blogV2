package tables

import dao.{TagDAO, PassageDAO}
import models.Passage2Tag
import slick.driver.JdbcProfile

/**
  * Created by Leo.
  * 2015/11/1 21:03
  */
trait Passage2TagTable extends AbstractTable[Passage2Tag] {

  protected val driver: JdbcProfile

  import driver.api._

  class Passage2TagTable(tag: Tag) extends AbstractTable(tag, "t_passage_tag") {

    def passageId = column[Int]("passage_id")

    def passageFK = foreignKey("passage_fk", passageId, PassageDAO.passages)(_.id, onDelete = ForeignKeyAction.Cascade)

    def tagId = column[Int]("tag_id")

    def tagFK = foreignKey("tag_fk", tagId, TagDAO.tags)(_.id, onDelete = ForeignKeyAction.Cascade)

    def passage_tag_unique_Index = index("PASSAGE_TAG_UNIQUE_IDX", (passageId, tagId), unique = true)

    override def * = (id, passageId, tagId) <>(Passage2Tag.tupled, Passage2Tag.unapply)
  }

}
