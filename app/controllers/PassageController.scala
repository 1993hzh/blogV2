package controllers

import javax.inject.Inject
import dao.{CommentDAO, PassageDAO}
import play.api.Logger
import play.api.cache.CacheApi
import play.api.data.Form
import play.api.data.Forms._
import play.api.mvc.{Action, Controller}

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
      case Some(p) => Ok(views.html.passage(p._1, p._2, p._3))
      case None => Redirect(routes.Index.index)
    }
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