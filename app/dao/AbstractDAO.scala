package dao

import java.util.concurrent.TimeUnit

import models.AbstractModel
import play.api.Play
import play.api.db.slick.{HasDatabaseConfig, DatabaseConfigProvider}
import _root_.slick.driver.JdbcProfile
import tables.AbstractTable

import scala.concurrent.Future
import scala.concurrent.duration.Duration

/**
  * Created by leo on 15-10-27.
  */

trait AbstractDAO[M <: AbstractModel] extends HasDatabaseConfig[JdbcProfile] with AbstractTable[M] {

  import driver.api._

  override protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile]("blog")(Play.current)

  type T <: AbstractTable

  protected val modelQuery: slick.lifted.TableQuery[T]

  // query time should never larger than 2'
  val waitTime = Duration(2, TimeUnit.SECONDS)

  def insert(model: M): Future[Int] = db.run(modelQuery returning modelQuery.map(_.id) += model)

  def update(model: M): Future[Int] = db.run(modelQuery.filter(_.id === model.id).update(model))

  def query(id: Int): Future[Option[M]] = db.run(modelQuery.filter(_.id === id).result.headOption)

  def delete(model: M): Future[Int] = delete(model.id)

  def delete(id: Int): Future[Int] = db.run(modelQuery.filter(_.id === id).delete)

  def upsert(model: M): Future[Int] = db.run(modelQuery.insertOrUpdate(model))

}
