package dao

import javax.inject.Singleton

import models.Keyword
import slick.lifted.TableQuery
import tables.KeywordTable

/**
 * Created by leo on 15-11-2.
 */
@Singleton()
class KeywordDAO extends AbstractDAO[Keyword] with KeywordTable {

  override type T = KeywordTable
  override protected val modelQuery: TableQuery[T] = TableQuery[KeywordTable]

  import driver.api._


}

object KeywordDAO {
  val keywords = new KeywordDAO().modelQuery

  def apply() = {
    new KeywordDAO()
  }
}
