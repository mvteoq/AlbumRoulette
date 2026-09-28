package com.mvteo.albumroulette.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.mvteo.albumroulette.model.AlbumStatus
import com.mvteo.albumroulette.model.SavedAlbum
class AlbumStatusConverters {
    @TypeConverter
    fun fromStatus(status: AlbumStatus): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(value: String): AlbumStatus {
        return AlbumStatus.valueOf(value)
    }
}
@Database(
    entities = [SavedAlbum::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(AlbumStatusConverters::class)
abstract class AlbumDatabase : RoomDatabase() {
    abstract fun dao(): SavedAlbumDao

    companion object {
        const val DATABASE_NAME = "album_roulette.db"

        @Volatile
        private var INSTANCE: AlbumDatabase? = null

        fun getDatabase(context: Context): AlbumDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AlbumDatabase::class.java,
                    DATABASE_NAME
                )
                    .build()
                    .also { INSTANCE = it }
            }
        }
    }
}
