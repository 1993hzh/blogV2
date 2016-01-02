package models

import java.util.Date

/**
 * Created by Leo.
 * 2016/1/1 2:40
 */
case class File(id: Int,
                name: String,
                storeId: String,
                storeName: String,
                createTime: Date,
                fileType: String,
                passageId: Int) extends AbstractModel
