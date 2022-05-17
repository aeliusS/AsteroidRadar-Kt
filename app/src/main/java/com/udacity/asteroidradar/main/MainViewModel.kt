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

enum class AsteroidApiStatus { LOADING, ERROR, DONE }

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = getDatabase(application)
    private val asteroidsRepository = AsteroidsRepository(database)

    private var metadata = getMetaData(application.applicationContext)
    private val apiKey: String = metadata?.get("nasa_api_key").toString() // the api key

    // used to show snack bar notifications
    private val _asteroidApiStatus = MutableLiveData<AsteroidApiStatus>()
    val asteroidApiStatus: LiveData<AsteroidApiStatus>
        get() = _asteroidApiStatus

    init {
        getAsteroids()
    }

    val asteroids = asteroidsRepository.asteroids

    fun refreshData() {
        getAsteroids()
    }

    private fun getAsteroids() {
        viewModelScope.launch {
            _asteroidApiStatus.value = AsteroidApiStatus.LOADING
            try {
                asteroidsRepository.refreshAsteroids(apiKey)
                _asteroidApiStatus.value = AsteroidApiStatus.DONE
                Timber.d("Timber. Refreshed cache")
            } catch (e: Exception) {
                _asteroidApiStatus.value = AsteroidApiStatus.ERROR
                Timber.w("Timber. Error with refreshing cache: $e")
            }
        }
    }

    /**
     * Helper function necessary to get API key
     * */
    private fun getMetaData(context: Context): Bundle? {
        return context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        ).metaData
    }

    fun finishedDisplayingApiMessage() {
        _asteroidApiStatus.value = AsteroidApiStatus.DONE
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
}