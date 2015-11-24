package controllers

import javax.inject.Inject

import dao.{RoleDAO, UserDAO}
import models.{Role, RoleType, User, WebSite}
import play.api.{Play, Logger}
import play.api.cache.CacheApi
import play.api.libs.ws.{WSClient}
import play.api.mvc._
import utils.Encryption

import scala.concurrent.{Future, Await}
import scala.concurrent.duration.Duration

/**
 * Created by Leo.
 * 2015/11/22 21:41
 */
class Sina @Inject()(ws: WSClient, cache: CacheApi) extends Controller {

  private lazy val log = Logger
  private lazy val userDAO = UserDAO()
  private lazy val roleDAO = RoleDAO()

  private val authorize_url = "https://api.weibo.com/oauth2/authorize"
  private val access_token_url = "https://api.weibo.com/oauth2/access_token"
  private val get_uid_url = "https://api.weibo.com/2/account/get_uid.json"
  private val get_uinfo_url = "https://api.weibo.com/2/users/show.json"

  /* Need Config */
  private val redirect_uri = Play.current.configuration.getString("sina.redirect_uri").getOrElse("")
  private val CLIENT_ID = Play.current.configuration.getString("sina.app_id").getOrElse("")
  private val CLIENT_SECRECT = Play.current.configuration.getString("sina.app_secret").getOrElse("")

  private def accessTokenRequest(code: String) = ws.url(access_token_url).post(Map(
    "client_id" -> Seq(CLIENT_ID),
    "client_secret" -> Seq(CLIENT_SECRECT),
    "grant_type" -> Seq("authorization_code"),
    "redirect_uri" -> Seq(redirect_uri),
    "code" -> Seq(code)
  ))

  def login() = Action.async { implicit request =>
    val queryString: Map[String, Seq[String]] = Map(
      "client_id" -> Seq(CLIENT_ID),
      "response_type" -> Seq("code"),
      "redirect_uri" -> Seq(redirect_uri))
    Future.successful(Redirect(authorize_url, queryString))
  }

  def getToken() = Action.async { implicit request =>
    val code = request.getQueryString("code").getOrElse("")

    val json = Await.result(accessTokenRequest(code), Duration.Inf)
    (json.json \ "access_token").asOpt[String] match {
      case Some(at) => doLogin(at)
      case None =>
        val msg = "Get token failed, return value: " + json.json
        log.error(msg)
        returnFutureResult(msg)
    }
  }

  private def doLogin(accessToken: String)(implicit request: Request[AnyContent]): Future[Result] = {
    val uIdJson = Await.result(ws.url(get_uid_url).withQueryString("access_token" -> accessToken).get(), Duration.Inf)
    val uid = (uIdJson.json \ "uid").asOpt[Long]
    uid match {
      case Some(id) =>
        userDAO.loginFromOtherSite(id.toString, WebSite.SINA) match {
          case Some((u, r)) => loginWithExist(u, r)
          case None => loginWithNew(accessToken, id.toString)
        }
      case None =>
        val msg = "Get uid failed, return value: " + uIdJson.json
        log.error(msg)
        returnFutureResult(msg)
    }
  }

  private def loginWithNew(accessToken: String, uid: String)(implicit request: Request[AnyContent]): Future[Result] = {
    val uInfoJson = Await.result(ws.url(get_uinfo_url).withQueryString("access_token" -> accessToken, "uid" -> uid).get(), Duration.Inf)
    (uInfoJson.json \ "screen_name").asOpt[String] match {
      case Some(uname) =>
        val role = roleDAO.getRoleByWebsiteSync(WebSite.SINA, RoleType.THIRD_PARTY)
        role match {
          case Some(r) =>
            val user = User(0, uname, Encryption.encodeBySHA1(uname), uname + "@test.com", r.id, bindingId = Some(uid))
            log.info("Insert user from Sina: " + user)
            userDAO.insert(user)
            Application.doUserLogin(cache, user, r, request)
          case None =>
            val msg = "Website: " + WebSite.SINA + ", with RoleType: " + RoleType.THIRD_PARTY + " not found!"
            log.error(msg)
            returnFutureResult(msg)
        }
      case None =>
        val msg = "Get uname failed, return value: " + uInfoJson.json
        log.error(msg)
        returnFutureResult(msg)
    }
  }

  private def loginWithExist(user: User, role: Role)(implicit request: Request[AnyContent]): Future[Result] = {
    Application.doUserLogin(cache, user, role, request)
  }

  private def returnFutureResult(msg: String)(implicit request: Request[AnyContent]) = {
    Future.successful(Ok(views.html.login(error = Some(msg))))
  }
}