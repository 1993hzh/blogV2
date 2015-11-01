package tables

import dao.{TagDAO, PassageDAO}
import models.Passage2Tag
import slick.driver.JdbcProfile

/**
 * Created by Leo.
 * 2015/11/1 21:03
 *
 * since the `t_passage_tag` is a intermedia table, I decide not to treat it as a model
 * all intermedia tables will not extends AbstractTable
 */
trait Passage2TagTable {

  protected val driver: JdbcProfile

  import driver.api._

  class Passage2TagTable(tag: Tag) extends Table[Passage2Tag](tag, "t_passage_tag") {

    def passageId = column[Int]("passage_id")

    def passageFK = foreignKey("passage_fk", passageId, PassageDAO.passages)(_.id)

    def tagId = column[Int]("tag_id")

    def tagFK = foreignKey("tag_fk", tagId, TagDAO.tags)(_.id)


    override def * = (passageId, tagId) <>(Passage2Tag.tupled, Passage2Tag.unapply)
  }

}
