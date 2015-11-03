package tables

import dao.PassageDAO
import models.Keyword

/**
 * Created by Leo.
 * 2015/11/1 20:56
 */
trait KeywordTable extends AbstractTable[Keyword] {

  import driver.api._

  class KeywordTable(tag: Tag) extends AbstractTable(tag, "t_keyword") {

    def name = column[String]("name")

    def passageId = column[Int]("passage_id")

    def passageFK = foreignKey("passage_fk", passageId, PassageDAO.passages)(_.id, onDelete = ForeignKeyAction.Cascade)

    override def * = (id, name, passageId) <>(Keyword.tupled, Keyword.unapply)
  }

}