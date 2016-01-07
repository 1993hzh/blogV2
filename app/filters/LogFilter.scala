package filters

import play.api.Logger
import play.api.http.Status
import play.api.mvc.{RequestHeader, EssentialAction, EssentialFilter}
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Leo.
 * 2016/1/6 23:34
 */
object LogFilter extends EssentialFilter {
  private val log = Logger(this.getClass)

  def apply(nextFilter: EssentialAction) = new EssentialAction {
    def apply(requestHeader: RequestHeader) = {
      val startTime = System.currentTimeMillis
      nextFilter(requestHeader).map { result =>
        val endTime = System.currentTimeMillis
        val requestTime = endTime - startTime
        if (!requestHeader.uri.startsWith("/assets") && !requestHeader.uri.equals("/favicon.ico")) {
          val logger = s"${requestHeader.remoteAddress} ${requestHeader.method} ${requestHeader.uri}" +
            s" took ${requestTime}ms and returned ${result.header.status}"
          result.header.status match {
            case Status.OK => log.info(logger)
            case _ => log.error(logger)
          }
        }
        result
      }
    }
  }
}
