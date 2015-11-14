package filters

import java.time.LocalDateTime

import controllers.{routes, Application}
import dao.RoleDAO
import models.RoleType
import play.api.Logger
import play.api.mvc.{Results, Result, RequestHeader, Filter}

import scala.concurrent.Future

/**
 * Created by Leo.
 * 2015/11/14 21:24
 */
object ManageFilter extends Filter {

  lazy val roleDAO = RoleDAO()
  val log = Logger

  override def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    if (rh.path.toLowerCase.contains("manage")) {
      log.info(rh.remoteAddress + " is requesting management: '" + rh.uri + "' at: " + LocalDateTime.now)

      Application.getLoginUserId(rh.session) match {
        case -1 =>
          log.info("user not login at: " + rh.remoteAddress + ", redirected.")
          Future.successful(Results.Redirect(routes.Login.index))
        case i =>
          // first defence with role
          roleDAO.getRoleByLoginUserSync(i) match {
            case RoleType.OWNER => f(rh)
            case _ =>
              log.warn("user: " + i + " not authorized: " + rh.remoteAddress + ", redirected.")
              Future.successful(Results.Redirect(routes.Index.index))
          }
        case _ => f(rh)
      }
    } else {
      f(rh)
    }
  }
}
