package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.asDatabaseModel
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDao
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.domain.PictureOfDay
import com.udacity.asteroidradar.util.formatDate
import com.udacity.asteroidradar.util.getDateRangeFormatted
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.util.*

class AsteroidsRepository(
    private val asteroidDao: AsteroidDao,
    private val defaultDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    private val calendar = Calendar.getInstance()
    private var todayDate = formatDate(calendar)

    fun getAsteroidsAll() = asteroidDao.getAsteroidsAll(todayDate)

    fun getAsteroidsForToday() = asteroidDao.getAsteroidsByDate(todayDate)

    val pictureOfDay: LiveData<PictureOfDay?> =
        Transformations.map(asteroidDao.getPictureOfDay()) {
            it?.asDomainModel()
        }

    /**
     * Update the database (cache) for asteroids
     * */
    suspend fun refreshAsteroids(apiKey: String) {
        withContext(defaultDispatcher) {
            val (startDate, endDate) = getDateRangeFormatted()
            Timber.i("Timber. Date range is $startDate to $endDate")

            val apiResult = AsteroidApi.asteroids.getAsteroids(startDate, endDate, apiKey)

            val asteroids = parseAsteroidsJsonResult(JSONObject(apiResult))
            asteroidDao.insertAll(*asteroids.asDatabaseModel())
        }
    }

    /**
     * Remove asteroids that already passed
     * */
    suspend fun removeOldAsteroids() {
        withContext(defaultDispatcher) {
            asteroidDao.deleteOldAsteroids(todayDate)
        }
    }

    /**
     * Update the database (cache) for picture of day
     * */
    suspend fun refreshPictureOfDay(apiKey: String) {
        withContext(defaultDispatcher) {
            val networkPictureOfDay = AsteroidApi.asteroids.getPictureOfTheDay(apiKey)
            asteroidDao.insertPictureOfDay(networkPictureOfDay.asDatabaseModel())

            // clear up the older picture
            asteroidDao.getPictureOfDayNotLive()?.pictureId?.let {
                asteroidDao.clearOlderPictureOfDay(
                    it
                )
            }
        }
    }
}