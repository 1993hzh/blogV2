import actors.ViewCountActor
import akka.actor.Props
import controllers.Application
import filters.{HttpsFilter, LoginFilter, ManageFilter}
import play.api._
import play.api.libs.concurrent.Akka
import play.api.mvc._
import scala.concurrent.Future
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global

object Global extends WithFilters(HttpsFilter, LoginFilter, ManageFilter) with GlobalSettings {

  private val log = Logger(this.getClass)

  override def onStart(app: Application): Unit = {
    super.onStart(app)

    if (!app.mode.equals(Mode.Test)) {
      log.info("App starts.")

      Application.setPassageCount

      syncUpWithPassageViewCount(app)
    }
  }

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] = super.onError(request, ex)

  override def onBadRequest(request: RequestHeader, error: String): Future[Result] = super.onBadRequest(request, error)

  override def onHandlerNotFound(request: RequestHeader): Future[Result] = super.onHandlerNotFound(request)

  private def syncUpWithPassageViewCount(app: Application) = {
    val viewCountActor = Akka.system(app).actorOf(Props(new ViewCountActor()))
    Akka.system(app).scheduler.schedule(60 minutes, 60 minutes, viewCountActor, true)
  }

}

