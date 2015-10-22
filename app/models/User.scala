package models


import java.time.LocalDate
import java.util.Date
import javax.persistence._

import utils.Encryption

import scala.beans.BeanProperty

/**
 * Created by Leo.
 * 2015/10/22 21:12
 */
@Entity
@Table(name = "T_User")
class User extends AbstractModel {

  def this(userName: String, password: String, mail: String) {
    this()
    this.userName = userName
    this.password = password
    this.mail = mail
  }

  @BeanProperty
  @Column(unique = true)
  var userName: String = ""

  @BeanProperty
  var password: String = ""

  @BeanProperty
  var lastLoginIp: String = ""

  @BeanProperty
  var lastLoginTime: Date = new Date

  @BeanProperty
  @OneToOne
  var role: Role = null

  @BeanProperty
  @Column(unique = true)
  var mail: String = ""

}

object User {

  //for register user
  def apply(userName: String, password: String, mail: String) = {
    new User(userName, Encryption.encodeBySHA1(password), mail)
  }

  //for user login from other website such as: sina
  def apply(userName: String, mail: String) = {
    new User(userName, Encryption.encodeBySHA1(userName), mail)
  }
}

