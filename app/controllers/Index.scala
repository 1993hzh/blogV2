package controllers

import javax.inject.Inject

import dao.PassageDAO
import models.{Tag, Passage}
import play.api.Logger
import play.api.cache.CacheApi
import play.api.mvc.{Action, Controller}

/**
 * Created by leo on 15-11-3.
 */
class Index @Inject()(cache: CacheApi) extends Controller {

  private val passageDAO = PassageDAO()

  def index = {
    showPassages(1)
  }

  def showPassages(currentPage: Any) = Action { implicit request =>
    val totalPage = cache.getOrElse(Application.KEY_PAGE_COUNT)(0)
    val passages = listPassages(currentPage, totalPage)
    Ok(views.html.index(passages._1, passages._2, totalPage))
  }

  def about() = Action { implicit request =>
    Ok(views.html.about())
  }

  def listPassages(num: Any, totalPage: Int): (List[(Passage, List[Tag])], Int) = {
    val pageNo: Int = num match {
      case n: Int if (n <= 1) => 1
      case n: Int if (n >= totalPage) => totalPage
      case n: Int if (n > 1 && n < totalPage) => n
      case s: String if s.isEmpty => 1
      case _ => Logger.info("Error page num fetched: " + num); 1
    }
    val passageWithTags = passageDAO.queryPassages(pageNo).map(p => (p, passageDAO.getTags(p.id)))
    (passageWithTags.toList, pageNo)
  }

}
