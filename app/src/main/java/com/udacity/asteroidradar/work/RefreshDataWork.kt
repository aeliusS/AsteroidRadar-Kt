package com.udacity.asteroidradar.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidsRepository
import com.udacity.asteroidradar.util.getMetaData
import java.lang.Exception

class RefreshDataWork(appContext: Context, params: WorkerParameters): CoroutineWorker(appContext, params) {
    companion object {
        const val WORK_NAME = "AsteroidRefreshDataWorker"
    }

    override suspend fun doWork(): Result {
        val database = getDatabase(applicationContext)
        val repository = AsteroidsRepository(database.asteroidDao)
        return try {
            val metadata = getMetaData(applicationContext)
            val apiKey: String = metadata?.get("nasa_api_key").toString()

            // refresh the database (cache) and remove older asteroids
            repository.refreshAsteroids(apiKey)
            repository.removeOldAsteroids()
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }
}