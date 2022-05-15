package com.udacity.asteroidradar.repository

import android.os.Build
import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Constants
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.asDatabaseModel
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidsDatabase
import com.udacity.asteroidradar.database.asDomainModel
import com.udacity.asteroidradar.domain.Asteroid
import com.udacity.asteroidradar.main.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import timber.log.Timber
import java.text.SimpleDateFormat
import java.util.*

class AsteroidsRepository(private val database: AsteroidsDatabase) {
    // the live data will only execute when a fragment is listening so it is safe to add this
    // as a property
    val asteroids: LiveData<List<Asteroid>> = Transformations.map(database.asteroidDao.getAsteroids()) {
        it.asDomainModel()
    }

    /**
     * Update the database (cache)
     * */
    suspend fun refreshAsteroids(apiKey: String) {
        withContext(Dispatchers.IO) {
            val (startDate, endDate) = getDateRangeFormatted()
            Timber.i("Timber. Date range is $startDate to $endDate")

            val apiResult = withContext(Dispatchers.IO) {
                AsteroidApi.asteroids.getAsteroids(startDate, endDate, apiKey)
            }
            val asteroids = parseAsteroidsJsonResult(JSONObject(apiResult))
            database.asteroidDao.insertAll(*asteroids.asDatabaseModel())
        }
    }

    private fun getDateRangeFormatted(): MainViewModel.DateRange {
        val calendar = Calendar.getInstance()
        return MainViewModel.DateRange(
            startDate = formatDate(calendar),
            endDate = formatDate(calendar, Constants.DEFAULT_END_DATE_DAYS)
        )
    }

    private fun formatDate(calendar: Calendar, daysToAdd: Int? = null): String {
        if (daysToAdd != null) calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
        val currentTime = calendar.time
        val dateFormat = SimpleDateFormat(
            if (Build.VERSION.SDK_INT >= 24) Constants.API_QUERY_DATE_FORMAT else Constants.API_QUERY_DATE_FORMAT_FALLBACK,
            Locale.getDefault()
        )
        return dateFormat.format(currentTime)
    }
}