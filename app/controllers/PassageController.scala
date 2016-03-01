package controllers

import java.util.Date
import javax.inject.Inject
import dao.{FileDAO, TagDAO, PassageDAO}
import models.{Passage}
import play.api.Logger
import play.api.cache.CacheApi
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, Controller}
import scala.collection.mutable.{Set => Mset}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by Leo.
 * 2015/11/7 20:00
 */
class PassageController @Inject()(cache: CacheApi, messages: MessagesApi) extends Controller with I18nSupport {
  override def messagesApi: MessagesApi = messages

  private lazy val passageDAO = PassageDAO()
  private lazy val fileDAO = FileDAO()
  private lazy val tagDAO = TagDAO()
  private lazy val log = Logger(this.getClass)

  def passage(id: Int) = Action { implicit request =>
    val passageDetail = passageDAO.getDetail(id)
    passageDetail match {
      case Some(p) =>
        setViewCount(p._1.id, p._1.viewCount) //here set view count in cache, will get a schedule to sync up

        Ok(views.html.passage(p._1, p._2, p._3))
      case None => Redirect(routes.Index.index)
    }
  }

  def passageForApp(id: Int) = Action { implicit request =>
    val passageDetail = passageDAO.getDetail(id)
    passageDetail match {
      case Some(p) =>
        setViewCount(p._1.id, p._1.viewCount) //here set view count in cache, will get a schedule to sync up

        Ok(views.html.passageForApp(p._1, p._2, p._3))
      case None => Redirect(routes.Index.index)
    }
  }

  private def setViewCount(passageId: Int, viewCount: Int) = {
    //first set
    val set = cache.getOrElse[Mset[Int]](Application.PASSAGE_BEEN_READ_LIST)(Mset.empty[Int])
    if (!set.contains(passageId))
      cache.set(Application.PASSAGE_BEEN_READ_LIST, set + passageId)

    val currentViewCount = cache.getOrElse[Int](Application.PASSAGE_VIEW_COUNT_PREFIX + passageId)(viewCount)
    cache.set(Application.PASSAGE_VIEW_COUNT_PREFIX + passageId, currentViewCount + 1)
  }

  def manage(page: Int) = Action { implicit request =>
    val pageSize = 10
    val userId = Application.getLoginUserId(request.session)
    val totalPassageCount = passageDAO.queryTotalCountSync(Some(userId))
    val totalPage = Application.getTotalPage(totalPassageCount, pageSize)
    val pageNum: Int = Application.getPageNum(page, totalPage)

    val passageList = passageDAO.queryPassages(pageNum, pageSize, 20, Some(userId))
    Ok(views.html.managePassage(passageList, pageNum, totalPage))
  }

  def create = Action { implicit request =>
    Ok(views.html.createOrUpdatePassage(allTag = tagDAO.getAllTagSync.toList))
  }

  def doCreateOrUpdate = Action.async { implicit request =>
    val authorId = Application.getLoginUserId(request.session)
    val authorName = Application.getLoginUserName(request.session)
    passageForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(Application.sendJsonResult(false, formWithErrors.errors.map(_.message).mkString(", ")))
      },
      data => {
        val passage = Passage(data.id.getOrElse(0), authorId, authorName, data.title, data.content, new Date())
        val result = data.id match {
          case Some(id) if id > 0 =>
            log.info(authorName + " try to update passage: " + data.title + ", id: " + data.id)
            passageDAO.update(passage, data.keywords, data.tagIds)
          case _ =>
            log.info(authorName + " try to create passage: " + data.title)
            passageDAO.insert(passage, data.keywords, data.tagIds)
        }
        result.map {
          case r: Boolean if (r && !data.id.isEmpty) =>
            log.info(authorName + " update passage: " + data.title + ", id: " + data.id + " succeed.")
            Application.sendJsonResult(true, routes.PassageController.passage(data.id.getOrElse[Int](0)).url)
          case r: Boolean if !r =>
            val error = "update passage: " + data.title + ", id: " + data.id + " failed."
            log.warn(authorName + " " + error)
            Application.sendJsonResult(false, error)
          case r: Int if r > 0 =>
            Application.setPassageCount
            log.info(authorName + " create passage: " + data.title + " succeed, new id: " + r)
            Application.sendJsonResult(true, routes.PassageController.passage(r).url)
          case r: Int if r <= 0 =>
            Application.setPassageCount
            val error = "create passage: " + data.title + " failed, return value: " + r
            log.warn(authorName + " " + error)
            Application.sendJsonResult(false, error)
          case _ =>
            val error = "upsert passage: " + data.title + " , id: " + data.id + " failed."
            log.warn(authorName + " " + error)
            Application.sendJsonResult(false, error)
        }
      }
    )
  }

  def update(id: Int) = Action { implicit request =>
    val passage = passageDAO.getPassage(id)
    val tags = passageDAO.getTags(id).map(_.id)
    val keywords = passageDAO.getKeywords(id).map(_.name)
    Ok(views.html.createOrUpdatePassage(passage, tags, tagDAO.getAllTagSync.toList, keywords))
  }

  /**
   * I won't provide a function for batch deletion since it's unsafe
   * @param id
   * @return
   */
  def delete(id: Int) = Action { implicit request =>
    val user = Application.getLoginUserName(request.session)
    val userId = Application.getLoginUserId(request.session)
    log.info(user + " try to delete passage: " + id + " from: " + request.remoteAddress)

    passageDAO.delete(userId, id) match {
      case 1 =>
        log.info("passage: " + id + " delete succeed")
        Application.setPassageCount
        Application.sendJsonResult(true, "")
      case i if i != 1 =>
        log.info("passage: " + id + " delete failed, return value: " + i)
        Application.sendJsonResult(false, "Failure has been logged.")
    }
  }

  def passageForm(implicit lang: play.api.i18n.Lang) = Form(
    mapping(
      "id" -> optional(number),
      "title" -> nonEmptyText,
      "content" -> nonEmptyText,
      "keywords" -> list(nonEmptyText),
      "tagIds" -> list(number)
    )(PassageForm.apply)(PassageForm.unapply)
      verifying(Application.tooLong("title", 200), fields => fields.title.length < 200)
  )
}

case class PassageForm(id: Option[Int],
                       title: String,
                       content: String,
                       keywords: List[String],
                       tagIds: List[Int])