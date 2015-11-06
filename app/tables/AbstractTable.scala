package tables

import java.sql.Timestamp
import java.util.Date

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

  implicit def dateTimeMapper = MappedColumnType.base[Date, Timestamp](
    { date => new Timestamp(date.getTime) }, { timestamp => new Date(timestamp.getTime) }
  )
}
