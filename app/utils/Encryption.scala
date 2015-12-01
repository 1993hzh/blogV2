package utils

import java.security.MessageDigest
import javax.crypto.Mac
import javax.crypto.spec.SecretKeySpec
import org.apache.commons.codec.binary.Base64

/**
  * Created by Leo.
  * 2015/10/22 22:07
  */
object Encryption {

  def encodeBySHA1(str: String): String = {
    val md: MessageDigest = MessageDigest.getInstance("SHA-1")
    Base64.encodeBase64String(md.digest(str.getBytes("utf-8"))).trim()
  }

  def encodeByBase64(str: String): String = Base64.encodeBase64String(str.getBytes("utf-8"))

  def encodeByHmacSha1(data: String, key: String) = {
    val signingKey = new SecretKeySpec(key.getBytes(), "HmacSHA1")
    val mac = Mac.getInstance("HmacSHA1")
    mac.init(signingKey)
    val rawHmac = mac.doFinal(data.getBytes())
    Base64.encodeBase64String(rawHmac)
  }

  def urlSafe(str: String) = {
    str.replaceAll("\\+", "-").replaceAll("\\/", "_")
  }

  def main(args: Array[String]) {
    println(encodeBySHA1("123"))
  }
}
