package controllers

import javax.inject.Inject

import play.api.cache.CacheApi
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.libs.ws.WSClient
import play.api.mvc.{Action, Controller}
import utils.Encryption

import scala.concurrent.Future

/**
  * Created by leo on 15-12-1.
  */
class ImageUpload @Inject()(ws: WSClient, cache: CacheApi) extends Controller {

  private val RETURN_BODY_JSON = "{\"name\": $(fname),\"size\": $(fsize),\"w\": $(imageInfo.width),\"h\": $(imageInfo.height),\"hash\": $(etag)}"


  private lazy val ACCESS_KEY = Application.QINIU_ACCESS_KEY
  private lazy val SECRET_KEY = Application.QINIU_SECRET_KEY
  private lazy val BUCKET_NAME = Application.QINIU_BUCKET_NAME

  implicit val uploadPolicyWrites: Writes[PutPolicy] = (
    (JsPath \ "scope").write[String] and
      (JsPath \ "deadLine").write[Long] and
      (JsPath \ "returnBody").write[String]
    ) (unlift(PutPolicy.unapply))

  private def getPutPolicy(imageName: String = ""): String = {
    val scope = imageName.isEmpty match {
      case true => BUCKET_NAME
      case false => BUCKET_NAME + ":" + imageName
    }
    val deadline = (System.currentTimeMillis) / 1000 + 3600
    val putPolicy = PutPolicy(scope, deadline, RETURN_BODY_JSON)
    Json.toJson(putPolicy).toString
  }

  private def getPutPolicyEncode(imageName: String = "") = Encryption.urlSafe(Encryption.encodeByBase64(getPutPolicy(imageName)))

  private def sign(putPolicyEncode: String) = Encryption.urlSafe(Encryption.encodeByHmacSha1(putPolicyEncode, SECRET_KEY))

  def getUploadToken(imageName: String = "") = Action.async {
    val putPolicyEncode = getPutPolicyEncode(imageName)
    val token = ACCESS_KEY + ":" + sign(putPolicyEncode) + ":" + putPolicyEncode
    Future.successful(Ok(generateToken(token)))
  }

  private def generateToken(token: String) = {
    "{\"uptoken\": \"" + token + "\"}"
  }

}

case class PutPolicy(scope: String,
                     deadLine: Long,
                     returnBody: String)