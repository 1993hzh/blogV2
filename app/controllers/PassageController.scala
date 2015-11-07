package controllers

import javax.inject.Inject
import dao.PassageDAO
import play.api.cache.CacheApi
import play.api.mvc.{Action, Controller}

/**
 * Created by Leo.
 * 2015/11/7 20:00
 */
class PassageController @Inject()(cache: CacheApi) extends Controller {

  lazy val passageDAO = PassageDAO()

  def passage(id: Int) = Action { implicit request =>
    val passageDetail = passageDAO.getDetail(id)
    Ok(views.html.passage(passageDetail._1, passageDetail._2, passageDetail._3))
  }

}
