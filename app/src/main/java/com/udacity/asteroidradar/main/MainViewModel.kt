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

    private var metadata = getMetaData(application.applicationContext)
    private val apiKey: String = metadata?.get("nasa_api_key").toString() // the api key

    //TODO: Have an error handling variable
    private var _repositoryError = MutableLiveData<Boolean>()
    val repositoryError: LiveData<Boolean>
        get() = _repositoryError

    init {
        _repositoryError.value = false
        getAsteroids()
    }

    val asteroids = asteroidsRepository.asteroids

    //TODO: Have a loading variable
    fun refreshData() {
        getAsteroids()
    }

    private fun getAsteroids() {
        viewModelScope.launch {
            try {
                asteroidsRepository.removeOldAsteroids()
                asteroidsRepository.refreshAsteroids(apiKey)
                Timber.i("Timber. Refreshed cache")
            } catch (e: Exception) {
                Timber.i("Timber. Error with refreshing cache: $e")
                _repositoryError.value = true
            }
        }
    }

    private fun getMetaData(context: Context): Bundle? {
        return context.packageManager.getApplicationInfo(
            context.packageName,
            PackageManager.GET_META_DATA
        ).metaData
    }

    fun finishedDisplayingApiError() {
        _repositoryError.value = false
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