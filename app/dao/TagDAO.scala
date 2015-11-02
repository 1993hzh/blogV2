package dao

import javax.inject.Singleton

import models.{Tag => MyTag}
import slick.lifted.TableQuery
import tables.TagTable

import scala.concurrent.Future

/**
 * Created by Leo.
 * 2015/11/1 21:06
 */
@Singleton()
class TagDAO extends AbstractDAO[MyTag] with TagTable {

  override type T = TagTable

  override protected val modelQuery = TableQuery[TagTable]

  import driver.api._

  def queryByName(name: String): Future[Option[MyTag]] = {
    db.run(modelQuery.filter(_.name === name).result.headOption)
  }

}

object TagDAO {
  val tags = new TagDAO().modelQuery

  def apply() = {
    new TagDAO()
  }
}
