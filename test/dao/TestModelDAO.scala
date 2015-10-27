package dao


import models.TestModel
import _root_.slick.lifted.TableQuery
import tables.TestModelTable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by leo on 15-10-27.
 */
class TestModelDAO extends AbstractDAO with TestModelTable {

  protected var modelQuery = TableQuery[TestModelTable]

  import driver.api._

  def create(model: TestModel): Future[Unit] = {
    try db.run(modelQuery += model).map(_ => ())
    finally db.close
  }

  def query(name: String): Future[TestModel] = {
    try db.run(modelQuery.filter(_.name === name).result.head)
    finally db.close
  }

  def all(): Future[List[TestModel]] = {
    try db.run(modelQuery.result).map(_.toList)
    finally db.close
  }

  def update(model: TestModel): Future[Unit] = {
    try db.run(modelQuery.filter(_.id === model.id).update(model)).map(_ => ())
    finally db.close
  }

  def delete(model: TestModel): Future[Int] = delete(model.id)

  def delete(id: Int): Future[Int] = {
    try db.run(modelQuery.filter(_.id === id).delete)
    finally db.close
  }

  def upsert(): Future[Unit] = ???

}