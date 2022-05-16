package com.udacity.asteroidradar.database

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import timber.log.Timber

//TODO: Make unit tests
@Dao
interface AsteroidDao {
    // if returning a basic List, room library will block on the main thread
    // if using live data, the UI can watch for changes and room will update on a background thread
    @Query("SELECT * FROM DatabaseAsteroid ORDER BY closeApproachDate")
    fun getAsteroids(): LiveData<List<DatabaseAsteroid>>

    // vararg allows us to pass a variable number of DatabaseVideo objects to be
    // insert/updated by the room library
    // this is an upsert (update/insert) on the cache, so replace on conflict
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroids: DatabaseAsteroid)

    // remove asteroids that already passed
    @Query("DELETE FROM DatabaseAsteroid WHERE closeApproachDate < :targetDate")
    suspend fun deleteOldAsteroids(targetDate: String)

}

@Database(entities = [DatabaseAsteroid::class], version = 1, exportSchema = false)
abstract class AsteroidsDatabase : RoomDatabase() {
    abstract val asteroidDao: AsteroidDao
}

private lateinit var INSTANCE: AsteroidsDatabase
fun getDatabase(context: Context): AsteroidsDatabase {
    synchronized(AsteroidsDatabase::class.java) {
        if (!::INSTANCE.isInitialized) {
            INSTANCE = Room.databaseBuilder(
                context.applicationContext,
                AsteroidsDatabase::class.java,
                "asteroids"
            ).build()
        }
        Timber.d("Timber. AsteroidsDatabase initialized")
    }
    Timber.d("Timber. AsteroidsDatabase returned")
    return INSTANCE
}