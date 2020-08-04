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
    @Query("SELECT COUNT(*) FROM UnhandledDuplication")
    fun getUnhandledDuplicationCount():Int
    @Query("DELETE FROM UnhandledDuplication WHERE pId in (SELECT pId FROM UnhandledDuplication limit 100)")
    fun deleteUnhandledDuplication()
}