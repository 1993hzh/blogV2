package models.test

import javax.persistence.Entity

import models.AbstractModel

import scala.beans.BeanProperty

/**
 * Created by leo on 15-10-20.
 */
@Entity
class TestModel extends AbstractModel {

  @BeanProperty
  var name: String = ""
}

object TestModel {
  def apply() = {
    new TestModel
  }
}