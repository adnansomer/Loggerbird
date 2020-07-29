package loggerbird.models.room

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
internal data class UnhandledDuplication(
    @PrimaryKey(autoGenerate = true)
    val pid: Int? = null,
    val className: String? = null,
    val methodName:String? = null,
    val lineName:String? = null,
    val fieldName:String? = null)