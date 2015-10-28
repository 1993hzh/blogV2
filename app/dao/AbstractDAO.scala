package dao

import models.AbstractModel
import play.api.Play
import play.api.db.slick.{HasDatabaseConfig, DatabaseConfigProvider}
import _root_.slick.driver.JdbcProfile

import scala.concurrent.Future

/**
 * Created by leo on 15-10-27.
 */

trait AbstractDAO[M <: AbstractModel] extends HasDatabaseConfig[JdbcProfile] {

  override protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile]("blog")(Play.current)

  def insert(model: M): Future[Unit]

  def update(model: M): Future[Int]

  def query(hql: String): Future[Seq[M]]

  def delete(model: M): Future[Int]

  def upsert(model: M): Future[Int]

}
