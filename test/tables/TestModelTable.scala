package tables

import models.TestModel
import slick.driver.JdbcProfile

/**
 * Created by leo on 15-10-26.
 */
trait TestModelTable {
  protected val driver: JdbcProfile

  import driver.api._

  class TestModelTable(tag: Tag) extends Table[TestModel](tag, "testmodel") {

    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)

    def name = column[String]("name")

    def nameIndex = index("NAME_IDX", name, unique = true)

    override def * = (id, name) <>(TestModel.tupled, TestModel.unapply)
  }

}