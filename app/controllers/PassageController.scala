package controllers

import java.sql.Timestamp
import javax.inject.Inject
import dao.{TagDAO, CommentDAO, PassageDAO}
import models.{Keyword, Passage}
import play.api.Logger
import play.api.cache.CacheApi
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Results, Action, Controller}
import scala.collection.mutable.{Set => Mset}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

/**
 * Created by Leo.
 * 2015/11/7 20:00
 */
class PassageController @Inject()(cache: CacheApi) extends Controller {

  private lazy val passageDAO = PassageDAO()
  private lazy val commentDAO = CommentDAO()
  private lazy val tagDAO = TagDAO()
  private lazy val log = Logger

  def passage(id: Int) = Action { implicit request =>
    val passageDetail = passageDAO.getDetail(id)
    passageDetail match {
      case Some(p) =>
        setViewCount(p._1.id, p._1.viewCount) //here set view count in cache, will get a schedule to sync up

        Ok(views.html.passage(p._1, p._2, p._3))
      case None => Redirect(routes.Index.index)
    }
  }

  private def setViewCount(passageId: Int, viewCount: Int) = {
    //first set
    var set = cache.getOrElse[Mset[Int]](Application.PASSAGE_BEEN_READ_LIST)(Mset.empty[Int])
    set += passageId
    cache.set(Application.PASSAGE_BEEN_READ_LIST, set)

    var currentViewCount = cache.getOrElse[Int](Application.PASSAGE_VIEW_COUNT_PREFIX + passageId)(0)
    if (currentViewCount == 0) //if the currentViewCount is 0, we should init the value with viewCount stored in db
      currentViewCount += viewCount
    currentViewCount += 1
    cache.set(Application.PASSAGE_VIEW_COUNT_PREFIX + passageId, currentViewCount)
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
    val createTime = new Timestamp(System.currentTimeMillis)
    passageForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(Ok(formWithErrors.errors.map(_.message).mkString(", ")))
      },
      data => {
        val passage = Passage(data.id.getOrElse(0), authorId, authorName, data.title, data.content, createTime)
        val result = data.id match {
          case Some(id) =>
            log.info(Application.now + ": " + authorName + " try to update passage: " + data.title + ", id: " + data.id)
            passageDAO.update(passage, data.keywords, data.tagIds)
          case None =>
            log.info(Application.now + ": " + authorName + " try to create passage: " + data.title)
            passageDAO.insert(passage, data.keywords, data.tagIds)
        }
        result.map {
          case r: Boolean if (r && !data.id.isEmpty) =>
            log.info(Application.now + ": " + authorName + " update passage: " + data.title + ", id: " + data.id + " succeed.")
            Redirect(routes.PassageController.passage(data.id.getOrElse(0)))
          case r: Boolean if !r =>
            log.info(Application.now + ": " + authorName + " update passage: " + data.title + ", id: " + data.id + " failed.")
            Ok("update passage: " + data.title + ", id: " + data.id + " failed.")
          case r: Int if r > 0 =>
            log.info(Application.now + ": " + authorName + " create passage: " + data.title + " succeed, new id: " + r)
            Redirect(routes.PassageController.passage(r))
          case r: Int if r <= 0 =>
            log.info(Application.now + ": " + authorName + " create passage: " + data.title + " failed, return value: " + r)
            Ok("create passage: " + data.title + " failed, return value: " + r)
          case _ =>
            log.info(Application.now + ": " + authorName + " upsert passage: " + data.title + " , id: " + data.id + " failed.")
            Ok("upsert passage: " + data.title + " , id: " + data.id + " failed.")
        }
      }
    )
  }

  def update(id: Int) = Action { implicit request =>
    val passage = passageDAO.getPassage(id)
    val tags = passageDAO.getTags(id)
    val keywords = passageDAO.getKeywords(id)
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
    log.info(user + " try to delete passage: " + id + " from: " + request.remoteAddress + " at: " + Application.now)

    passageDAO.delete(userId, id) match {
      case 1 =>
        log.info("passage: " + id + " delete succeed")
        Application.setPassageCount
        Ok("Success")
      case i if i != 1 =>
        log.info("passage: " + id + " delete failed, return value: " + i)
        Ok("Failure has been logged.")
    }
  }

  val passageForm = Form(
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