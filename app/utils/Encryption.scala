package utils

import java.security.MessageDigest
import org.apache.commons.codec.binary.Base64
import play.api.Logger

/**
 * Created by Leo.
 * 2015/10/22 22:07
 */
object Encryption {

  def encodeBySHA1(str: String): String = {
    if (null == str) {
      return null
    }
    try {
      val md: MessageDigest = MessageDigest.getInstance("SHA-1")
      return Base64.encodeBase64String(md.digest(str.getBytes("utf-8"))).trim()
    } catch {
      case e: Exception =>
        Logger.error(e.getMessage + "=> cause " + str + " encryption failed.")
    }
    return null
  }

  def main(args: Array[String]) {
    println(encodeBySHA1("123"))
  }
}
