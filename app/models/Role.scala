//package models
//
//import javax.persistence.{OneToOne, Table, Entity}
//
//import scala.beans.BeanProperty
//
///**
// * Created by Leo.
// * 2015/10/22 21:22
// */
//@Entity
//@Table(name = "T_Role")
//class Role extends AbstractModel {
//
//  def this(roleType: String) {
//    this()
//    this.roleType = roleType
//  }
//
//  def this(roleType: String, user: User) {
//    this(roleType)
//    this.user = user
//  }
//
//  def this(roleType: String, user: User, webSite: String) {
//    this(roleType, user)
//    this.webSite = webSite
//  }
//
//  @BeanProperty
//  var roleType: String = ""
//
//  @OneToOne
//  @BeanProperty
//  var user: User = null
//
//  @BeanProperty
//  var webSite: String = ""
//
//}
//
//object Role {
//
//  //for register User
//  def apply(user: User) = {
//    new Role(RoleType.COMMON.toString, user)
//  }
//
//  def apply(user: User, webSite: String) = {
//    new Role(RoleType.COMMON.toString, user, webSite)
//  }
//}
//
//object RoleType extends Enumeration {
//  val OWNER = Value("owner")
//  val COMMON = Value("common")
//}