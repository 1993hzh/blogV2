package filters

import java.time.LocalDateTime

import controllers.{routes, Application}
import play.api.Logger
import play.api.mvc.{Results, Result, RequestHeader, Filter}

import scala.concurrent.Future

/**
 * Created by Leo.
 * 2015/11/15 13:07
 */
object LoginFilter extends Filter {

  private lazy val log = Logger

  override def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    if (rh.path.contains("logged")) {
      log.info(rh.remoteAddress + " is requesting logged-permission-url: '" + rh.uri + "' at: " + LocalDateTime.now)

      val isAjax = rh.headers.get("X-Requested-With") match {
        case Some(x) => true
        case None => false
      }

      Application.getLoginUser(rh.session) match {
        case None if (isAjax) =>
          log.info("user not login at: " + rh.remoteAddress + ", send a ajax with login info.")
          Future.successful(Results.Ok(Application.loginAjax()))
        case None if (!isAjax) =>
          log.info("user not login at: " + rh.remoteAddress + ", redirected.")
          Future.successful(Results.Redirect(routes.Login.index))
        case Some((u, r)) => f(rh)
      }
    } else {
      f(rh)
    }
  }
}