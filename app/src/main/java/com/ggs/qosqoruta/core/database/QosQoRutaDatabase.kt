package com.ggs.qosqoruta.core.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.ggs.qosqoruta.features.auth.data.local.AuthDao
import com.ggs.qosqoruta.features.auth.data.local.UserEntity
import com.ggs.qosqoruta.features.mapa.data.local.RouteHistoryDao
import com.ggs.qosqoruta.features.mapa.data.local.RouteHistoryEntity

@Database(
    entities = [UserEntity::class, RouteHistoryEntity::class],
    version = 3,
    exportSchema = false
)
abstract class QosQoRutaDatabase : RoomDatabase() {

    abstract fun authDao(): AuthDao
    abstract fun routeHistoryDao(): RouteHistoryDao

    companion object {
        @Volatile
        private var INSTANCE: QosQoRutaDatabase? = null

        fun getDatabase(context: Context): QosQoRutaDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    QosQoRutaDatabase::class.java,
                    "qosqoruta_local.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()

                INSTANCE = instance
                instance
            }
        }
    }
}