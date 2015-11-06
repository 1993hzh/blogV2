package controllers

import javax.inject.Inject

import dao.PassageDAO
import models.Passage
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

  def showPassages(currentPage: Int) = Action { implicit request =>
    val totalPage = cache.getOrElse(Application.KEY_PAGE_COUNT)(0)
    val passages = listPassages(currentPage, totalPage)
    Ok(views.html.index(passages, currentPage, totalPage))
  }

  def listPassages(num: Any, totalPage: Int): List[(Passage,String)] = {
    val pageNo: Int = num match {
      case n: Int if (n <= 1) => 1
      case n: Int if (n >= totalPage) => totalPage
      case n: Int if (n > 1 && n < totalPage) => n
      case _ => Logger.info("Error page num fetched: " + num); 1
    }
    passageDAO.queryPassages(pageNo).toList
  }


}
