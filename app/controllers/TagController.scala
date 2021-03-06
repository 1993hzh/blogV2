package controllers

import javax.inject.Inject

import dao.TagDAO
import models.Tag
import play.api.Logger
import play.api.cache.CacheApi
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Leo.
 * 2015/11/14 23:07
 */
class TagController @Inject()(cache: CacheApi, messages: MessagesApi) extends Controller with I18nSupport {
  override def messagesApi: MessagesApi = messages

  private lazy val tagDAO = TagDAO()

  private lazy val log = Logger(this.getClass)

  def manage(page: Int) = Action { implicit request =>
    val pageSize = 10
    val totalTagCount = tagDAO.getTagCountSync()
    val totalPage = Application.getTotalPage(totalTagCount, pageSize)
    val pageNum: Int = Application.getPageNum(page, totalPage)

    val tags = tagDAO.getTagsSync(pageNum, pageSize)
    Ok(views.html.manageTag(tags, pageNum, totalPage))
  }

  def upsert = Action { implicit request =>
    tagForm.bindFromRequest.fold(
      formWithErrors => {
        Application.sendJsonResult(false, formWithErrors.errors.map(_.message).mkString(", "))
      },
      data => {
        val tagId = data.id.getOrElse(0)

        val result = tagDAO.upsertSync(Tag(tagId, data.name, data.description))
        result match {
          case result: Int if result == 0 =>
            log.info("Tag: " + data.name + " createOrUpdate failed, return value: " + result)
            Application.sendJsonResult(false, "Tag: " + data.name + " createOrUpdate failed, return value: " + result)
          case result: Int if result != 0 && tagId != 0 =>
            log.info("Tag: " + data.name + " is updated, id: " + tagId)
            Application.sendJsonResult(true, "")
          case result: Int if result != 0 && tagId == 0 =>
            log.info("Tag: " + data.name + " is created.")
            Application.sendJsonResult(true, "")
        }
      }
    )
  }

  /**
   * I won't provide a function for batch deletion since it's unsafe
   * @param id
   * @return
   */
  def delete(id: Int) = Action.async { implicit request =>
    tagDAO.delete(id) map {
      case result: Int if (result == 1) =>
        log.info("tag: " + id + " delete succeed")
        Application.sendJsonResult(true, "")
      case result: Int if (result != 1) =>
        log.info("tag: " + id + " delete failed, return value: " + result)
        Application.sendJsonResult(false, "Failure has been logged.")
    }
  }

  def tagForm(implicit lang: play.api.i18n.Lang) = Form(
    mapping(
      "id" -> optional(number),
      "name" -> nonEmptyText,
      "description" -> text
    )(TagForm.apply)(TagForm.unapply)
      verifying(Application.tooLong("name", 32), fields => fields.name.length < 32)
      verifying(Application.tooLong("description", 255), fields => fields.description.length < 255)
  )
}

case class TagForm(id: Option[Int],
                   name: String,
                   description: String)