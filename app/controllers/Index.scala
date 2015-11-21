package controllers

import javax.inject.Inject

import dao.{CommentDAO, PassageDAO}
import models.{Tag, Passage}
import play.api.Logger
import play.api.cache.CacheApi
import play.api.mvc.{Action, Controller}

/**
 * Created by leo on 15-11-3.
 */
class Index @Inject()(cache: CacheApi) extends Controller {

  private lazy val passageDAO = PassageDAO()
  private lazy val commentDAO = CommentDAO()

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

  def listPassages(num: Any, totalPage: Int): (List[(Passage, List[Tag], Int)], Int) = {
    val pageNo: Int = Application.getPageNum(num, totalPage)
    val passageWithTagsAndCommentNum = passageDAO.queryPassages(pageNo)
      .map(p => (p, passageDAO.getTags(p.id), commentDAO.getCommentCountByPassageIdSync(p.id)))
    (passageWithTagsAndCommentNum.toList, pageNo)
  }

}
