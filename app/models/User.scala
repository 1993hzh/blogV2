package models

import java.sql.Timestamp


/**
 * Created by Leo.
 * 2015/10/22 21:12
 */
case class User(id: Int,
                userName: String,
                password: String,
                mail: String,
                roleId: Int,
                lastLoginIp: Option[String] = None,
                lastLoginTime: Option[Timestamp] = None,
                //Decided not to use a single table to persist user_id & binding_id,
                //as I think no one will bind one more 3rd party platform..
                //ok, I admit, the real cause is that I don't wanna maintain one more table
                bindingId: Option[String] = None) extends AbstractModel
