package filters

import controllers.{routes, Application}
import play.api.Logger
import play.api.mvc.{Results, Result, RequestHeader, Filter}

import scala.concurrent.Future

/**
 * Created by Leo.
 * 2015/11/15 13:07
 */
class LoginFilter extends Filter {

  private lazy val log = Logger(this.getClass)

  override def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    if (rh.path.contains("logged") || rh.path.contains("manage")) {

      val isAjax = rh.headers.get("X-Requested-With") match {
        case Some(x) => true
        case None => false
      }

      Application.getLoginUser(rh.session) match {
        case None if (isAjax) =>
          //          log.info(rh.remoteAddress + " is requesting " + rh.uri + " without login, send ajax.")
          log.info(rh.remoteAddress + " didn't login, send ajax.")
          Future.successful(Application.sendJsonResult(false, Application.loginAjax("")(rh)))
        case None if (!isAjax) =>
          //          log.info(rh.remoteAddress + " is requesting " + rh.uri + " without login, redirected.")
          log.info(rh.remoteAddress + " didn't login, redirected.")
          Future.successful(Results.Redirect(routes.Login.index() + "?callback=" + rh.uri))
        case Some((u, r)) => f(rh)
      }
    } else {
      f(rh)
    }
  }
}