package com.udacity.asteroidradar.viewmodels

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.data.DatabaseAsteroid
import com.udacity.asteroidradar.data.asDomainModel
import com.udacity.asteroidradar.data.getDatabase
import com.udacity.asteroidradar.domain.Asteroid
import com.udacity.asteroidradar.data.AsteroidsRepository
import com.udacity.asteroidradar.utilities.getMetaData
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import timber.log.Timber
import java.lang.Exception
import java.lang.IllegalArgumentException

enum class AsteroidApiStatus { LOADING, ERROR, DONE }

class MainViewModel(application: Application) : AndroidViewModel(application) {

    companion object {
        const val FILTERED_VIEW_ALL = -1
        const val FILTERED_VIEW_TODAY = 0
    }

    private val database = getDatabase(application)
    private val asteroidsRepository = AsteroidsRepository(database.asteroidDao)

    private var metadata = getMetaData(application.applicationContext)
    private val apiKey: String = metadata?.get("nasa_api_key").toString() // the api key

    // used to show snack bar notifications
    private val _asteroidApiStatus = MutableLiveData<AsteroidApiStatus>()
    val asteroidApiStatus: LiveData<AsteroidApiStatus>
        get() = _asteroidApiStatus

    init {
        refreshData()
    }

    private val filteredView: MutableStateFlow<Int> = MutableStateFlow(FILTERED_VIEW_ALL)

    @OptIn(ExperimentalCoroutinesApi::class)
    private val _asteroids: LiveData<List<DatabaseAsteroid>> = filteredView.flatMapLatest { filter ->
        if(filter == FILTERED_VIEW_ALL) {
            asteroidsRepository.getAsteroidsAll()
        } else {
            asteroidsRepository.getAsteroidsForToday()
        }
    }.asLiveData()

    val asteroids: LiveData<List<Asteroid>> =
        Transformations.map(_asteroids) {
            it.asDomainModel()
        }

    val pictureOfDay = asteroidsRepository.pictureOfDay

    fun refreshData() {
        updateAsteroidsAndPictureOfDay()
    }

    private fun updateAsteroidsAndPictureOfDay() {
        viewModelScope.launch {
            _asteroidApiStatus.value = AsteroidApiStatus.LOADING
            try {
                asteroidsRepository.refreshAsteroids(apiKey)
                Timber.d("Timber. Refreshed cache")

                asteroidsRepository.refreshPictureOfDay(apiKey)
                Timber.d("Timber. Updated picture")

                _asteroidApiStatus.value = AsteroidApiStatus.DONE
            } catch (e: Exception) {
                _asteroidApiStatus.value = AsteroidApiStatus.ERROR
                Timber.w("Timber. Error with refreshing cache: $e")
            }
        }
    }

    fun finishedDisplayingApiErrorMessage() {
        _asteroidApiStatus.value = AsteroidApiStatus.DONE
    }

    fun setFilteredView(filter: Int) {
        filteredView.value = filter
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