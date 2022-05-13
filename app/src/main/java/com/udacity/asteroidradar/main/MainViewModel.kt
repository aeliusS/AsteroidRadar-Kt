package com.udacity.asteroidradar.main

import android.app.Application
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.lifecycle.*
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import kotlinx.coroutines.launch
import org.json.JSONObject
import timber.log.Timber
import java.lang.Exception
import java.lang.IllegalArgumentException

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val _asteroids = MutableLiveData<List<Asteroid>>()
    val asteroids: LiveData<List<Asteroid>>
        get() = _asteroids

    private var metadata: Bundle? = null

    init {
        metadata = getMetaData(application.applicationContext)
        val apikey: String = metadata?.get("nasa_api_key").toString() // the api key
        getAsteroids(apikey)
    }

    private fun getAsteroids(apiKey: String) {
        viewModelScope.launch {
            try {
                Timber.i("Timber. Contacting the API")
                val apiResult =
                    AsteroidApi.asteroids.getAsteroids("2022-05-12", "2022-05-19", apiKey)
                _asteroids.value = parseAsteroidsJsonResult(JSONObject(apiResult))
            } catch (e: Exception) {
                Timber.i("Timber. Error with API: $e")
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
            if(modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("unable to construct MainViewModel")
        }
    }
}