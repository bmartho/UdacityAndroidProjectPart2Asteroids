package com.udacity.asteroidradar.main

import android.app.Application
import androidx.lifecycle.*
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.database.getDatabase
import com.udacity.asteroidradar.repository.AsteroidRepository
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : ViewModel() {
    private val database = getDatabase(application)
    private val asteroidRepository = AsteroidRepository(database)

    var fetchedFromDatabase = false
    private val _pictureOfDay = MutableLiveData(PictureOfDay("", "", ""))
    val pictureOfDay: LiveData<PictureOfDay>
        get() = _pictureOfDay

    private val _downloadSuccess = MutableLiveData(true)
    val downloadSuccess: LiveData<Boolean>
        get() = _downloadSuccess

    init {
        viewModelScope.launch {
            _downloadSuccess.value = asteroidRepository.refreshAsteroids()
            _pictureOfDay.value = asteroidRepository.getPictureOfDay()
        }
    }

    val asteroids = asteroidRepository.asteroids
    val todayAsteroids = asteroidRepository.todayAsteroids
    val weekAsteroids = asteroidRepository.weekAsteroids

    class Factory(val app: Application) : ViewModelProvider.Factory {
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
                @Suppress("UNCHECKED_CAST")
                return MainViewModel(app) as T
            }
            throw IllegalArgumentException("Unable to construct viewmodel")
        }
    }
}