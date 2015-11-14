package controllers

import javax.inject.Inject
import dao.{CommentDAO, PassageDAO}
import play.api.Logger
import play.api.cache.CacheApi
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}
import scala.collection.mutable.{Set => Mset}

/**
 * Created by Leo.
 * 2015/11/7 20:00
 */
class PassageController @Inject()(cache: CacheApi) extends Controller {

  lazy val passageDAO = PassageDAO()
  lazy val commentDAO = CommentDAO()

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
    Ok(views.html.createOrUpdatePassage())
  }

  def doCreateOrUpdate = Action { implicit request =>
    passageForm.bindFromRequest.fold(
      formWithErrors => {
        Ok(formWithErrors.errors.map(_.message).mkString(", "))
      },
      data => {
        //TODO
        Application.setPassageCount
        Ok("")
      }
    )
  }

  def update(id: Int) = Action { implicit request =>
    val passage = passageDAO.getPassage(id)
    val tags = passageDAO.getTags(id)
    val keywords = passageDAO.getKeywords(id)
    Ok(views.html.createOrUpdatePassage(passage, tags, keywords))
  }

  /**
   * I won't provide a function for batch deletion since it's unsafe
   * @param id
   * @return
   */
  def delete(id: Int) = Action { implicit request =>
    val user = Application.getLoginUserName(request.session)
    val userId = Application.getLoginUserId(request.session)
    Logger.info(user + " try to delete passage: " + id + " from: " + request.remoteAddress + " at: " + Application.now)

    passageDAO.delete(userId, id) match {
      case 1 =>
        Logger.info("passage: " + id + " delete succeed")
        Application.setPassageCount
        Ok("Success")
      case i if i != 1 =>
        Logger.info("passage: " + id + " delete failed, return value: " + i)
        Ok("Failure has been logged.")
    }
  }

  val passageForm = Form(
    mapping(
      "title" -> nonEmptyText,
      "content" -> nonEmptyText,
      "keywords" -> list(nonEmptyText),
      "tagIds" -> list(number)
    )(PassageForm.apply)(PassageForm.unapply)
      verifying(Application.tooLong("title", 200), fields => fields.content.length < 200)
  )
}

case class PassageForm(title: String,
                       content: String,
                       keywords: List[String],
                       tagIds: List[Int])