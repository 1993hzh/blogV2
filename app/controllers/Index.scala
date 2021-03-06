package controllers

import javax.inject.Inject

import dao.{CommentDAO, PassageDAO}
import models.{Tag, Passage}
import play.api.cache.CacheApi
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.{Action, Controller}

/**
 * Created by leo on 15-11-3.
 */
class Index @Inject()(cache: CacheApi, messages: MessagesApi) extends Controller with I18nSupport {
  override def messagesApi: MessagesApi = messages

  private lazy val passageDAO = PassageDAO()
  private lazy val commentDAO = CommentDAO()

  def index = {
    showPassages(1)
  }

  def about() = Action { implicit request =>
    Ok(views.html.about())
  }

  def showPassages(currentPage: Any, query: Option[String] = None) = Action { implicit request =>
    val result = getPassagesByConditions(currentPage, query)
    Ok(views.html.index(result._1._1, result._1._2, result._2, query))
  }

  def showPassagesToJSON(currentPage: Int, query: Option[String] = None) = Action { implicit request =>
    val totalPage = cache.getOrElse(Application.KEY_PAGE_COUNT)(0)
    val pageNo: Int = Application.getPageNum(currentPage, totalPage)
    val passages = passageDAO.queryPassages(pageNo, pageSize = 5, query = query)
    Ok(Json.obj("passages" -> Json.toJson(passages), "currentPage" -> currentPage, "totalPage" -> totalPage))
  }

  implicit val PassageWrites = Json.writes[Passage]

  def getPassagesByConditions(currentPage: Any, query: Option[String] = None): ((List[(Passage, List[Tag], Int)], Int), Int) = {
    var passages: (List[(Passage, List[Tag], Int)], Int) = (Nil, 0)
    var totalPage = 0
    query match {
      case Some(q) =>
        val count = passageDAO.queryTotalCountSync(query = query)
        totalPage = Application.getTotalPage(count)
        passages = listPassages(currentPage, totalPage, getQuery(q))
      case None =>
        totalPage = cache.getOrElse(Application.KEY_PAGE_COUNT)(0)
        passages = listPassages(currentPage, totalPage)
    }
    (passages, totalPage)
  }

  def listPassages(num: Any, totalPage: Int, query: Option[String] = None): (List[(Passage, List[Tag], Int)], Int) = {
    val pageNo: Int = Application.getPageNum(num, totalPage)
    val passageWithTagsAndCommentNum = passageDAO.queryPassages(pageNo, query = query)
      .map(p => (p, passageDAO.getTags(p.id), commentDAO.getCommentCountByPassageIdSync(p.id)))
    (passageWithTagsAndCommentNum.toList, pageNo)
  }

  private def getQuery(query: String) = {
    val result = query.length match {
      case l if l < 64 => query
      case l if l >= 64 => query.substring(0, 64)
      case _ => ""
    }
    Some(result)
  }

}
