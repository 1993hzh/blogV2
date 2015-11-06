import java.time.LocalDateTime

import dao.PassageDAO
import play.api._
import play.api.cache.Cache
import play.api.mvc._
import controllers.Application
import scala.concurrent.Future
import scala.util.{Failure, Success}
import play.api.Play.current
import scala.concurrent.ExecutionContext.Implicits.global

object Global extends WithFilters() with GlobalSettings {

  override def onStart(app: Application): Unit = {
    super.onStart(app)

    Logger.info("App starts on: " + now)

    setPassageCount
  }

  override def onError(request: RequestHeader, ex: Throwable): Future[Result] = super.onError(request, ex)

  override def onBadRequest(request: RequestHeader, error: String): Future[Result] = super.onBadRequest(request, error)

  override def onHandlerNotFound(request: RequestHeader): Future[Result] = super.onHandlerNotFound(request)

  private def now = LocalDateTime.now

  def setPassageCount = {
    PassageDAO().queryTotalCount onComplete {
      case Success(result) =>
        Cache.set(Application.KEY_PASSAGE_COUNT, result)

        setTotalPage(result)
      case Failure(f) =>
        Logger.error("App get passage count failed due to: " + f.getLocalizedMessage)
    }
  }

  private def setTotalPage(passageCount: Int) = {
    val totalPage = passageCount / Application.PAGE_SIZE + (if (passageCount % Application.PAGE_SIZE != 0) 1 else 0)
    Cache.set(Application.KEY_PASSAGE_COUNT, totalPage)
    Logger.info("App get total page num: " + totalPage)
  }
}
