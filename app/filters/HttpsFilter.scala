package filters

import play.api.{Play}
import play.api.mvc.{Filter, Results, Result, RequestHeader}
import play.api.Play.current
import scala.concurrent.Future

/**
 * Created by Leo.
 * 2016/1/1 14:43
 */
object HttpsFilter extends Filter {
  override def apply(f: (RequestHeader) => Future[Result])(request: RequestHeader): Future[Result] = {
    // force http to https in Prod
    if (Play.isProd && !request.secure && !request.uri.startsWith("/assets")) {
      Future.successful(Results.MovedPermanently("https://" + request.host + request.uri))
    } else {
      f(request)
    }
  }
}
