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
    passageDetail match {
      case Some(p) => Ok(views.html.passage(p._1, p._2, p._3))
      case None => Redirect(routes.Index.index)
    }

  }

}
