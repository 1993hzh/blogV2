package dao

import javax.inject.Singleton

import models.File
import slick.lifted.TableQuery
import tables.FileTable

import scala.concurrent.{Await, Future}

/**
 * Created by Leo.
 * 2016/1/1 2:50
 */
@Singleton()
class FileDAO extends AbstractDAO[File] with FileTable {
  override type T = FileTable
  override protected val modelQuery: TableQuery[T] = TableQuery[FileTable]

  import driver.api._

  def getFilesByPassageId(passageId: Int): Future[Seq[File]] = {
    db.run(modelQuery.filter(_.passageId === passageId).sortBy(_.createTime.desc).result)
  }

  def getFilesByPassageIdSync(passageId: Int): Seq[File] = Await.result(getFilesByPassageId(passageId), waitTime)

}

object FileDAO {
  val files = new FileDAO().modelQuery

  def apply() = {
    new FileDAO()
  }
}
