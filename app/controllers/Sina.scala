package controllers

import javax.inject.Inject

import play.api.libs.ws.{WSRequest, WSClient}
import play.api.mvc.Controller

import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent.ExecutionContext.Implicits.global

/**
 * Created by Leo.
 * 2015/11/22 21:41
 */
class Sina @Inject()(ws: WSClient) extends Controller {

  private val authorize_url = "https://api.weibo.com/oauth2/authorize"
  private val access_token_url = "https://api.weibo.com/oauth2/access_token"
  private val get_uid_url = "https://api.weibo.com/2/account/get_uid.json"
  private val redirect_uri = ""

  private val CLIENT_ID = "$"
  private val CLIENT_SECRECT = "$"

  val authorizeRequest: WSRequest = ws.url(authorize_url).withQueryString(
    "client_id" -> CLIENT_ID,
    "response_type" -> "code",
    "redirect_uri" -> redirect_uri
  )

  authorizeRequest.execute()

  val accessTokenRequest = ws.url(access_token_url).withHeaders(
    "client_id" -> CLIENT_ID,
    "client_secret" -> CLIENT_SECRECT
  ).withQueryString(
    "grant_type" -> "authorization_code",
    "redirect_uri" -> redirect_uri,
    "code" -> "CODE"
  )

  val accessTokenJson = accessTokenRequest.get().map(response =>
    (response.json \ "access_token").as[String]
  )

  val accessToken = Await.result(accessTokenJson, Duration.Inf)

  val getUIdJson = ws.url(get_uid_url).withHeaders("access_token" -> accessToken).get().map(response =>
    (response.json \ "uid").as[String]
  )

  val bindingId = Await.result(getUIdJson, Duration.Inf)

}