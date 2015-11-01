package dao

import javax.inject.Singleton

import models.Passage
import slick.lifted.TableQuery
import tables.PassageTable

import scala.concurrent.Future

/**
 * Created by Leo.
 * 2015/11/1 20:50
 */
@Singleton()
class PassageDAO extends AbstractDAO[Passage] with PassageTable {
  private val modelQuery = TableQuery[PassageTable]

  import driver.api._

  override def insert(model: Passage): Future[Int] = ???

  override def update(model: Passage): Future[Int] = ???

  override def delete(model: Passage): Future[Int] = ???

  override def upsert(model: Passage): Future[Int] = ???

  override def query(id: Int): Future[Option[Passage]] = ???
}

object PassageDAO {
  val passages = new PassageDAO().modelQuery

  def apply() = {
    new PassageDAO()
  }
}