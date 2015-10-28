package models

import java.sql.Timestamp


/**
 * Created by Leo.
 * 2015/10/22 21:12
 */
case class User(id: Int, userName: String, password: String, mail: String, roleId: Int,
                lastLoginIp: Option[String] = None, lastLoginTime: Option[Timestamp] = None) extends AbstractModel
