package models


/**
 * Created by Leo.
 * 2015/10/22 21:22
 */
case class Role(id: Int, roleType: String, webSite: String = WebSite.MY) extends AbstractModel

object RoleType {
  val OWNER = "owner"
  val COMMON = "common"
}

object WebSite {
  val MY = "www.huzhonghua.cn"
  val SINA = "weibo.com"
}