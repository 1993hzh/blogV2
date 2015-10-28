package tables

import models.TestModel
import slick.driver.JdbcProfile

/**
 * Created by leo on 15-10-26.
 */
trait TestModelTable {
  protected val driver: JdbcProfile

  import driver.api._

  class TestModelTable(tag: Tag) extends Table[TestModel](tag, "t_testmodel") {
    //remember that the `t_testmodel` should be in lower case

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def nameIndex = index("NAME_IDX", name, unique = true)

    def description = column[String]("description")

    override def * = (id, name, description) <>(TestModel.tupled, TestModel.unapply)
  }

}