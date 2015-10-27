package dao

import play.api.Play
import play.api.db.slick.{HasDatabaseConfig, DatabaseConfigProvider}
import _root_.slick.driver.JdbcProfile

/**
 * Created by leo on 15-10-27.
 */
trait AbstractDAO extends HasDatabaseConfig[JdbcProfile] {

  override protected val dbConfig = DatabaseConfigProvider.get[JdbcProfile]("blog")(Play.current)

}
