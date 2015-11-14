package dao

import javax.inject.Singleton

import controllers.Application
import models.{Tag => MyTag}
import slick.lifted.TableQuery
import tables.TagTable

import scala.concurrent.{Await, Future}

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

  private def getTags(num: Int, pageSize: Int = Application.PAGE_SIZE): Future[Seq[MyTag]] = {
    db.run(modelQuery.sortBy(_.id.desc).drop((num - 1) * pageSize).take(pageSize).result)
  }

  def getTagsSync(num: Int, pageSize: Int = Application.PAGE_SIZE): Seq[MyTag] = {
    Await.result(getTags(num, pageSize), waitTime)
  }

  private def getTagCount(): Future[Int] = db.run(modelQuery.length.result)

  def getTagCountSync(): Int = Await.result(getTagCount(), waitTime)

}

object TagDAO {
  val tags = new TagDAO().modelQuery

  def apply() = {
    new TagDAO()
  }
}
