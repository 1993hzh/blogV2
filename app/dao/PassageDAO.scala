package dao

import javax.inject.Singleton

import models.Passage
import slick.lifted.TableQuery
import tables.PassageTable

/**
 * Created by Leo.
 * 2015/11/1 20:50
 */
@Singleton()
class PassageDAO extends AbstractDAO[Passage] with PassageTable {

  override protected val modelQuery = TableQuery[PassageTable]

  override type T = PassageTable

  import driver.api._

}

object PassageDAO {
  val passages = new PassageDAO().modelQuery

  def apply() = {
    new PassageDAO()
  }
}