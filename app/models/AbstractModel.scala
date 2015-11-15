package models

/**
 * Created by leo on 15-10-28.
 */
trait AbstractModel {
  def id: Int //here is a design mistake, should make it as Option
}
