package controllers

import java.sql.Timestamp
import javax.inject.Inject
import dao.{TagDAO, CommentDAO, PassageDAO}
import models.Passage
import play.api.Logger
import play.api.cache.CacheApi
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}
import scala.collection.mutable.{Set => Mset}
import scala.concurrent.ExecutionContext.Implicits.global

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
    var passageId: Option[Int] = None
    var keywords: List[String] = Nil
    var tagIds: List[Int] = Nil
    var title = ""
    var content = ""
    passageForm.bindFromRequest.fold(
      formWithErrors => {
        Ok(formWithErrors.errors.map(_.message).mkString(", "))
      },
      data => {
        passageId = data.id
        keywords = data.keywords
        tagIds = data.tagIds
        title = data.title
        content = data.content
      }
    )
    val authorId = Application.getLoginUserId(request.session)
    val authorName = Application.getLoginUserName(request.session)
    val createTime = new Timestamp(System.currentTimeMillis)
    val passage = Passage(passageId.getOrElse(0), authorId, authorName, title, content, createTime)
    val result = passageId match {
      case Some(id) =>
        passageDAO.update(passage, keywords, tagIds)
      case None =>
        passageDAO.insert(passage, keywords, tagIds)
    }

    result.map {
      case r: Boolean if r =>
        passageId match {
          case Some(id) => Redirect(routes.PassageController.passage(id))
          case None =>
            log.info(Application.now + ": " + authorName + " update passage: " + title
              + " succeed, however, id not found, it should never happened.")
            Ok("update passage: " + title + " succeed, however, id not found, it should never happened.")
        }
      case r: Boolean if !r =>
        log.info(Application.now + ": " + authorName + " update passage: " + title + ", id: " + passageId + " failed.")
        Ok("update passage: " + title + ", id: " + passageId + " failed.")
      case r: Int if r > 0 => Redirect(routes.PassageController.passage(r))
      case r: Int if r <= 0 =>
        log.info(Application.now + ": " + authorName + " create passage: " + title + " failed, return value: " + r)
        Ok("create passage: " + title + " failed, return value: " + r)
      case _ =>
        log.info(Application.now + ": " + authorName + " upsert passage: " + title + " , id: " + passageId + " failed.")
        Ok("upsert passage: " + title + " , id: " + passageId + " failed.")
    }
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
      verifying(Application.tooLong("title", 200), fields => fields.content.length < 200)
  )
}

case class PassageForm(id: Option[Int],
                       title: String,
                       content: String,
                       keywords: List[String],
                       tagIds: List[Int])