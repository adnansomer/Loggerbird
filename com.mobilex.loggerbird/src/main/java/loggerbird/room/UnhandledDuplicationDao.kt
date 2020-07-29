package loggerbird.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import loggerbird.models.room.UnhandledDuplication

@Dao
internal interface UnhandledDuplicationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertUnhandledDuplication(unhandledDuplication: UnhandledDuplication)
    @Query("SELECT * FROM UnhandledDuplication")
    fun getUnhandledDuplication():List<UnhandledDuplication>
}