package com.udacity.asteroidradar.main

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.lifecycle.*
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidsRepository
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import java.lang.IllegalArgumentException

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application)
    private val asteroidsRepository = AsteroidsRepository(database)

    private var metadata: Bundle? = null

    init {
        metadata = getMetaData(application.applicationContext)
        val apikey: String = metadata?.get("nasa_api_key").toString() // the api key
        getAsteroids(apikey)
    }

    val asteroids = asteroidsRepository.asteroids

    //TODO: Have an error handling variable

    //TODO: Have a loading variable

    private fun getAsteroids(apiKey: String) {
        viewModelScope.launch {
            try {
                asteroidsRepository.refreshAsteroids(apiKey)
            } catch (e: Exception) {
                Timber.i("Timber. Error with refreshing cache: $e")
                // _asteroids.value = listOf()
            }
        }
    }

    private fun getMetaData(context: Context): Bundle? {
        return context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        ).metaData
    }

    /**
     * The factory for constructing the MainViewModel with parameter
     * */
    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("unable to construct MainViewModel")
        }
    }

    data class DateRange(val startDate: String, val endDate: String)
}