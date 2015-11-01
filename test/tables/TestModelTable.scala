package tables

import models.TestModel
import slick.lifted

/**
 * Created by leo on 15-10-26.
 */
trait TestModelTable extends AbstractTable[TestModel] {

  import driver.api._

  class TestModelTable(tag: lifted.Tag) extends AbstractTable(tag, "t_testmodel") {
    //remember that the `t_testmodel` should be in lower case

    def name = column[String]("name")

    def nameIndex = index("NAME_IDX", name, unique = true)

    def description = column[String]("description")

    override def * = (id, name, description) <>(TestModel.tupled, TestModel.unapply)
  }

}