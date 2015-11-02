package tables

import models.AbstractModel
import slick.driver.JdbcProfile

/**
 * Created by Leo.
 * 2015/11/1 21:41
 */
trait AbstractTable[M <: AbstractModel] {

  protected val driver: JdbcProfile

  import driver.api._

  abstract class AbstractTable(tag: Tag, tableName: String) extends Table[M](tag, tableName) {
    def id = column[Int]("id", O.PrimaryKey, O.AutoInc)
  }

}
