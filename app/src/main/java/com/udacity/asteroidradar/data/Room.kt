package com.udacity.asteroidradar.data

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.room.*
import kotlinx.coroutines.flow.Flow
import timber.log.Timber

@Dao
interface AsteroidDao {
    // if returning a basic List, room library will block on the main thread
    // if using live data, the UI can watch for changes and room will update on a background thread
    @Query("SELECT * FROM asteroid_table " +
            "WHERE close_approach_date >= :targetDate ORDER BY close_approach_date")
    fun getAsteroidsAll(targetDate: String): Flow<List<DatabaseAsteroid>>

    @Query("SELECT * FROM asteroid_table WHERE close_approach_date = :targetDate")
    fun getAsteroidsByDate(targetDate: String): Flow<List<DatabaseAsteroid>>

    // vararg allows us to pass a variable number of DatabaseVideo objects to be
    // insert/updated by the room library
    // this is an upsert (update/insert) on the cache, so replace on conflict
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(vararg asteroids: DatabaseAsteroid)

    // remove asteroids that already passed
    @Query("DELETE FROM asteroid_table WHERE close_approach_date < :targetDate")
    suspend fun deleteOldAsteroids(targetDate: String)

    /** Section for picture of day **/
    @Query("SELECT * FROM daily_picture_table ORDER BY pictureId DESC LIMIT 1")
    fun getPictureOfDay(): LiveData<DatabasePicture?>

    @Query("SELECT * FROM daily_picture_table ORDER BY pictureId DESC LIMIT 1")
    fun getPictureOfDayNotLive(): DatabasePicture?

    @Insert
    fun insertPictureOfDay(picture: DatabasePicture)

    @Query("DELETE FROM daily_picture_table WHERE pictureId != :pictureId")
    suspend fun clearOlderPictureOfDay(pictureId: Long)

}

@Database(
    entities = [DatabaseAsteroid::class, DatabasePicture::class],
    version = 4,
    exportSchema = false
)
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
            )
                .fallbackToDestructiveMigration()
                .build()
        }
        Timber.d("Timber. AsteroidsDatabase initialized")
    }
    Timber.d("Timber. AsteroidsDatabase returned")
    return INSTANCE
}