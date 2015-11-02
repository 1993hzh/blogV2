package dao

import models.Passage2Tag
import slick.lifted.TableQuery
import tables.Passage2TagTable

/**
 * Created by leo on 15-11-2.
 */
class Passage2TagDAO extends AbstractDAO[Passage2Tag] with Passage2TagTable {
  override type T = Passage2TagTable
  override protected val modelQuery: TableQuery[T] = TableQuery[Passage2TagTable]

  import driver.api._

}

object Passage2TagDAO {
  val passage2Tags = new Passage2TagDAO().modelQuery

  def apply() = {
    new Passage2TagDAO()
  }
}