package filters

import java.time.LocalDateTime

import controllers.{routes, Application}
import play.api.Logger
import play.api.mvc.{Results, Result, RequestHeader, Filter}

import scala.concurrent.Future

/**
 * Created by Leo.
 * 2015/11/14 21:24
 */
object ManageFilter extends Filter {

  override def apply(f: (RequestHeader) => Future[Result])(rh: RequestHeader): Future[Result] = {
    if (rh.uri.toLowerCase.contains("manage")) {
      Logger.info(rh.remoteAddress + " is requesting management: '" + rh.uri + "' at: " + LocalDateTime.now)

      Application.getLoginUserId(rh.session) match {
        case -1 => Future.successful(Results.Redirect(routes.Login.index))
        case _ => f(rh)
      }
    } else {
      f(rh)
    }
  }
}
