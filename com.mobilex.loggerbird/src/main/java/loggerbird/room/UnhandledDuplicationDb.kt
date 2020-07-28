package loggerbird.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import loggerbird.models.room.UnhandledDuplication

@Database(entities = [UnhandledDuplication::class], version = 1)
internal abstract class UnhandledDuplicationDb : RoomDatabase() {
    abstract fun unhandledDuplicationDao(): UnhandledDuplicationDao

    companion object {
        var INSTANCE: UnhandledDuplicationDb? = null
        fun getUnhandledDuplicationDb(context: Context): UnhandledDuplicationDb? {
            if (INSTANCE == null) {
                synchronized(UnhandledDuplicationDb::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        UnhandledDuplicationDb::class.java,
                        "unhandled_duplication_db"
                    ).build()
                }
            }
            return INSTANCE
        }

        fun destroyDataBase() {
            INSTANCE = null
        }
    }
}