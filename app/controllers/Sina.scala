package controllers

import java.util.concurrent.TimeUnit
import javax.inject.Inject

import dao.{RoleDAO, UserDAO}
import models.{Role, RoleType, User, WebSite}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.JsValue
import play.api.{Logger}
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
class Sina @Inject()(ws: WSClient, cache: CacheApi, messages: MessagesApi) extends Controller with I18nSupport {
  override def messagesApi: MessagesApi = messages

  private lazy val log = Logger(this.getClass)
  private lazy val userDAO = UserDAO()
  private lazy val roleDAO = RoleDAO()

  private val authorize_url = "https://api.weibo.com/oauth2/authorize"
  private val access_token_url = "https://api.weibo.com/oauth2/access_token"
  private val get_uid_url = "https://api.weibo.com/2/account/get_uid.json"
  private val get_uinfo_url = "https://api.weibo.com/2/users/show.json"

  /* Need Config */
  private val REDIRECT_URI = Application.SINA_REDIRECT_URI
  private val CLIENT_ID = Application.SINA_CLIENT_ID
  private val CLIENT_SECRECT = Application.SINA_CLIENT_SECRECT
  private val INVALID_ACCESS_TOKEN = 21325

  private val WAIT_FOR_SINA_TIME = Duration(3, TimeUnit.SECONDS)

  private def accessTokenRequest(code: String) = ws.url(access_token_url).post(Map(
    "client_id" -> Seq(CLIENT_ID),
    "client_secret" -> Seq(CLIENT_SECRECT),
    "grant_type" -> Seq("authorization_code"),
    "redirect_uri" -> Seq(REDIRECT_URI),
    "code" -> Seq(code)
  ))

  def login() = Action.async { implicit request =>
    val queryString: Map[String, Seq[String]] = Map(
      "client_id" -> Seq(CLIENT_ID),
      "response_type" -> Seq("code"),
      "redirect_uri" -> Seq(REDIRECT_URI))
    Future.successful(Redirect(authorize_url, queryString))
  }

  def getToken() = Action.async { implicit request =>
    request.getQueryString("code") match {
      case Some(code) =>
        val json = Await.result(accessTokenRequest(code), WAIT_FOR_SINA_TIME)
        val accessToken = (json.json \ "access_token").asOpt[String]
        accessToken match {
          case Some(at) => doLogin(at)
          case None => handleErrorForSinaOpt("Get token failed", json.json)
        }
      case None =>
        log.error("Get token failed, no code returned, details: " + request.queryString.map(q => q._1 + "->" + q._2.mkString).mkString(", "))
        returnFutureResult("Get token failed, due to: " + request.getQueryString("error").getOrElse("Unknown reason."))
    }
  }

  private def doLogin(accessToken: String)(implicit request: Request[AnyContent]): Future[Result] = {
    val uIdJson = Await.result(ws.url(get_uid_url).withQueryString("access_token" -> accessToken).get(), WAIT_FOR_SINA_TIME)
    val uid = (uIdJson.json \ "uid").asOpt[Long]
    uid match {
      case Some(id) =>
        userDAO.loginFromOtherSite(id.toString, WebSite.SINA) match {
          case Some((u, r)) => loginWithExist(u, r)
          case None => loginWithNew(accessToken, id.toString)
        }
      case None => handleErrorForSinaOpt("Get uid failed", uIdJson.json)
    }
  }

  private def loginWithNew(accessToken: String, uid: String)(implicit request: Request[AnyContent]): Future[Result] = {
    val uInfoJson = Await.result(ws.url(get_uinfo_url).withQueryString("access_token" -> accessToken, "uid" -> uid).get(), WAIT_FOR_SINA_TIME)
    (uInfoJson.json \ "screen_name").asOpt[String] match {
      case Some(uname) =>
        val role = roleDAO.getRoleByWebsiteSync(WebSite.SINA, RoleType.THIRD_PARTY)
        role match {
          case Some(r) =>
            val user = User(0, uname, Encryption.encodeBySHA1(uname), uname + "@test.com", r.id, bindingId = Some(uid))
            log.info("Insert user from Sina: " + user)

            userDAO.createThirdPartyUser(user) match {
              case Some(u) => Application.doUserLogin(cache, u, r, request)
              case None =>
                val msg = "User create failed."
                log.error(msg)
                returnFutureResult(msg)
            }
          case None =>
            val msg = "Website: " + WebSite.SINA + ", with RoleType: " + RoleType.THIRD_PARTY + " not found!"
            log.error(msg)
            returnFutureResult(msg)
        }
      case None => handleErrorForSinaOpt("Get uname failed", uInfoJson.json)
    }
  }

  private def handleErrorForSinaOpt(leadMsg: String, jsonResult: JsValue)(implicit request: Request[AnyContent]): Future[Result] = {
    val errorCode = (jsonResult \ "error_code").asOpt[Int].getOrElse(-1)
    val error = (jsonResult \ "error").asOpt[String]
    errorCode match {
      case INVALID_ACCESS_TOKEN =>
        // here reget the token
        log.info("Get invalid token, redirect the user to re-get the access_token.")
        login().apply(request)
      case _ =>
        log.error(leadMsg + ", details: " + jsonResult)
        returnFutureResult(leadMsg + ", due to: " + error.getOrElse("Unknown reason."))
    }
  }

  private def loginWithExist(user: User, role: Role)(implicit request: Request[AnyContent]): Future[Result] = {
    Application.doUserLogin(cache, user, role, request)
  }

  private def returnFutureResult(msg: String)(implicit request: Request[AnyContent]) = {
    Future.successful(Ok(views.html.login(error = Some(msg))))
  }
}