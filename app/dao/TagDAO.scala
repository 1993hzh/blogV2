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

  protected val modelQuery = TableQuery[TagTable]

  import driver.api._

  override def insert(model: MyTag): Future[Int] = {
    db.run(modelQuery returning modelQuery.map(_.id) += model)
  }

  override def update(model: MyTag): Future[Int] = {
    db.run(modelQuery.filter(_.id === model.id).update(model))
  }

  override def delete(model: MyTag): Future[Int] = {
    db.run(modelQuery.filter(_.id === model.id).delete)
  }

  override def upsert(model: MyTag): Future[Int] = {
    db.run(modelQuery.insertOrUpdate(model))
  }

  override def query(id: Int): Future[Option[MyTag]] = {
    db.run(modelQuery.filter(_.id === id).result.headOption)
  }

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
