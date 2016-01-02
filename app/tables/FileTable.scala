package tables


import java.util.Date
import dao.PassageDAO
import models.File

/**
 * Created by Leo.
 * 2016/1/1 2:43
 */
trait FileTable extends AbstractTable[File] {

  import driver.api._

  class FileTable(tag: Tag) extends AbstractTable(tag, "t_file") {

    def name = column[String]("name")

    def storeId = column[String]("store_id")

    def storeName = column[String]("storename")

    def createTime = column[Date]("createtime")

    def fileType = column[String]("filetype")

    def passageId = column[Int]("passage_id")

    def passageFK = foreignKey("passage_fk", passageId, PassageDAO.passages)(_.id, onDelete = ForeignKeyAction.Cascade)

    override def * = (id, name, storeId, storeName, createTime, fileType, passageId) <>(File.tupled, File.unapply)
  }

}
