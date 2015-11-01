package tables

import models.{Tag => MyTag}

/**
 * Created by Leo.
 * 2015/11/1 21:07
 */
trait TagTable extends AbstractTable[MyTag] {

  import driver.api._

  class TagTable(tag: Tag) extends AbstractTable(tag, "t_tag") {

    def name = column[String]("name")

    def description = column[String]("description")

    override def * = (id, name, description) <>(MyTag.tupled, MyTag.unapply)
  }

}
