package dao


import javax.inject.Singleton

import models.TestModel
import _root_.slick.lifted.TableQuery
import tables.TestModelTable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by leo on 15-10-27.
 */
@Singleton()
class TestModelDAO extends AbstractDAO[TestModel] with TestModelTable {

  var modelQuery = TableQuery[TestModelTable]

  import driver.api._

  def create(model: TestModel): Future[Unit] = {
    db.run(modelQuery += model).map(_ => ())
  }

  def queryName(name: String): Future[TestModel] = {
    db.run(modelQuery.filter(_.name === name).result.head)
  }

  def all(): Future[List[TestModel]] = {
    db.run(modelQuery.result).map(_.toList)
  }

  def update(model: TestModel): Future[Int] = {
    val action = modelQuery.filter(_.id === model.id).update(model)
    db.run(action)
  }

  def update(name: String, description: String): Future[Int] = {
    val action = modelQuery.filter(_.name === name).map(_.description).update(description)
    db.run(action)
  }

  def delete(model: TestModel): Future[Int] = delete(model.id)

  def delete(id: Int): Future[Int] = {
    db.run(modelQuery.filter(_.id === id).delete)
  }

  def delete(name: String): Future[Int] = {
    db.run(modelQuery.filter(_.name === name).delete)
  }

  override def insert(model: TestModel): Future[Unit] = ???

  override def upsert(model: TestModel): Future[Int] = ???

  override def query(id: Int): Future[TestModel] = ???
}

object TestModelDAO {
  def apply() = {
    new TestModelDAO()
  }
}