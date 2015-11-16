package dao

import models.Passage2Tag
import slick.lifted.TableQuery
import tables.Passage2TagTable

import scala.concurrent.Future

/**
 * Created by leo on 15-11-2.
 */
class Passage2TagDAO extends AbstractDAO[Passage2Tag] with Passage2TagTable {
  override type T = Passage2TagTable
  override protected val modelQuery: TableQuery[T] = TableQuery[Passage2TagTable]

  import driver.api._

  def remove(passageId: Int, tagId: Int): Future[Int] = {
    val action = modelQuery.filter(pt => pt.passageId === passageId && pt.tagId === tagId).delete
    db.run(action)
  }

}

object Passage2TagDAO {
  val passage2Tags = new Passage2TagDAO().modelQuery

  def apply() = {
    new Passage2TagDAO()
  }
}