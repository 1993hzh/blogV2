package models

import javax.persistence.{GeneratedValue, Id, MappedSuperclass}

import scala.beans.BeanProperty

/**
 * Created by leo on 15-10-20.
 */
@MappedSuperclass
abstract class AbstractModel {
  @Id
  @GeneratedValue
  @BeanProperty
  var id: Int = 0


  def canEqual(other: Any): Boolean = other.isInstanceOf[AbstractModel]

  override def equals(other: Any): Boolean = other match {
    case that: AbstractModel =>
      (that canEqual this) &&
        id == that.id
    case _ => false
  }

  override def hashCode(): Int = {
    val state = Seq(id)
    state.map(_.hashCode()).foldLeft(0)((a, b) => 31 * a + b)
  }

}
