package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface AsteroidDao {
    @Query("select * from asteroidentity where closeApproachDate > :date order by closeApproachDate asc")
    fun getAsteroids(date: Long): LiveData<List<AsteroidEntity>>

    @Query("select * from asteroidentity where closeApproachDate > :from and closeApproachDate < :to order by closeApproachDate asc")
    fun getAsteroidsFromDates(from: Long, to: Long): LiveData<List<AsteroidEntity>>

    @Query("delete from asteroidentity where closeApproachDate <= :date")
    suspend fun deleteOldAsteroids(date: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(asteroids: List<AsteroidEntity>)
}

@Database(entities = [AsteroidEntity::class], version = 1)
abstract class AsteroidDatabase : RoomDatabase() {
    abstract val asteroidDao: AsteroidDao
}

private lateinit var INSTANCE: AsteroidDatabase

fun getDatabase(context: Context): AsteroidDatabase {
    synchronized(AsteroidDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AsteroidDatabase::class.java,
                "asteroids"
            ).build()
        }
    }

    return INSTANCE
}