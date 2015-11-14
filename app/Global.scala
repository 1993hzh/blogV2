import java.time.LocalDateTime
import controllers.Application
import filters.ManageFilter
import play.api._
import play.api.mvc._
import scala.concurrent.Future

object Global extends WithFilters(ManageFilter) with GlobalSettings {

  override def onStart(app: Application): Unit = {
    super.onStart(app)

    Logger.info("App starts on: " + Application.now)

    Application.setPassageCount
  }

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] = super.onError(request, ex)

  override def onBadRequest(request: RequestHeader, error: String): Future[Result] = super.onBadRequest(request, error)

  override def onHandlerNotFound(request: RequestHeader): Future[Result] = super.onHandlerNotFound(request)

}

