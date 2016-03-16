package controllers

import javax.inject.Inject

import play.api.Logger
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{Lang, I18nSupport, MessagesApi}
import play.api.mvc.{Results, Action, Controller}

import scala.concurrent.Future

/**
  * Created by leo on 16-3-16.
  */
class ResumeController @Inject()(messages: MessagesApi) extends Controller with I18nSupport {
  override def messagesApi: MessagesApi = messages

  private lazy val log = Logger(this.getClass)

  def view() = Action.async { implicit request =>
    Application.RESUME_URL match {
      case Some(url) =>
        Future.successful(Ok(views.html.resume()))
      case None =>
        log.error("Requesting resume, but no resume external url found!")
        Future.successful(Results.Redirect(routes.Index.index()))
    }
  }

  def check() = Action.async { implicit request =>
    resumeForm.bindFromRequest.fold(
      formWithErrors => {
        Future.successful(Ok(views.html.resume(error = Some(formWithErrors.errors.map(_.message).mkString(", ")))))
      },
      data => {
        Application.RESUME_PASSWORD match {
          case Some(pwd) if pwd.equals(data.password) =>
            Future.successful(Results.Redirect(Application.RESUME_URL.getOrElse(routes.Index.index().url)))
          case _ =>
            log.error("Requesting resume, but password error!")
            Future.successful(Ok(views.html.resume(error = Some("Error Password."))))
        }
      }
    )
  }

  def resumeForm(implicit lang: Lang) = Form(
    mapping(
      "password" -> nonEmptyText
    )(ResumeForm.apply)(ResumeForm.unapply)
      verifying(Application.tooLong("password", 255), fields => fields.password.length < 255)
  )

  case class ResumeForm(password: String)

}

