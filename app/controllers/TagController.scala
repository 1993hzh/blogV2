package controllers

import javax.inject.Inject

import dao.TagDAO
import play.api.Logger
import play.api.cache.CacheApi
import play.api.mvc.{Action, Controller}
import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

/**
 * Created by Leo.
 * 2015/11/14 23:07
 */
class TagController @Inject()(cache: CacheApi) extends Controller {

  lazy val tagDAO = TagDAO()

  def manage(page: Int) = Action { implicit request =>
    val pageSize = 10
    val totalPage = tagDAO.getTagCountSync()
    val pageNum: Int = Application.getPageNum(page, totalPage)
    val tags = tagDAO.getTagsSync(pageNum, pageSize)
    Ok(views.html.manageTag(tags, pageNum, totalPage))
  }

  def create = Action { implicit request =>
    //TODO
    Ok(views.html.createOrUpdatePassage())
  }

  def doCreateOrUpdate = Action { implicit request =>
    //TODO
    Ok("")
  }

  def update(id: Int) = Action { implicit request =>
    //    val tag = tagDAO.getTags(id)
    //TODO
    Ok("")
  }

  /**
   * I won't provide a function for batch deletion since it's unsafe
   * @param id
   * @return
   */
  def delete(id: Int) = Action { implicit request =>
    val user = Application.getLoginUserName(request.session)
    Logger.info(user + " delete tag: " + id + " from: " + request.remoteAddress + " at: " + Application.now)

    tagDAO.delete(id) onComplete {
      case Success(r) if (r == 1) =>
        Logger.info("tag: " + id + " delete succeed")
        Ok("Success")
      case Success(r) if (r != 1) =>
        Logger.info("tag: " + id + " delete failed, return value: " + r)
        Ok("Failure has been logged.")
      case Failure(f) =>
        Logger.error(f.getLocalizedMessage)
        Ok(f.getLocalizedMessage)
    }
    Ok("")
  }
}
