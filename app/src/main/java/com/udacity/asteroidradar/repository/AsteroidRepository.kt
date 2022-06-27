package com.udacity.asteroidradar.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.Transformations
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants.API_KEY
import com.udacity.asteroidradar.Constants.API_QUERY_DATE_FORMAT
import com.udacity.asteroidradar.Constants.DEFAULT_END_DATE_DAYS
import com.udacity.asteroidradar.Constants.SUCCESS_CODE
import com.udacity.asteroidradar.PictureOfDay
import com.udacity.asteroidradar.api.AsteroidApi
import com.udacity.asteroidradar.api.parseAsteroidsJsonResult
import com.udacity.asteroidradar.database.AsteroidDatabase
import com.udacity.asteroidradar.database.asDatabaseModel
import com.udacity.asteroidradar.database.asDomainModel
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class AsteroidRepository(private val database: AsteroidDatabase) {
    val asteroids: LiveData<List<Asteroid>>
        get() {
            // ---- get yesterday data
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)

            return Transformations.map(
                database.asteroidDao.getAsteroids(
                    calendar.time.time
                )
            ) {
                it.asDomainModel()
            }
        }

    val todayAsteroids: LiveData<List<Asteroid>>
        get() {
            // ---- from yesterday until today
            val calendar = Calendar.getInstance()
            val today = calendar.time.time
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = calendar.time.time

            return Transformations.map(
                database.asteroidDao.getAsteroidsFromDates(
                    yesterday, today
                )
            ) {
                it.asDomainModel()
            }
        }

    val weekAsteroids: LiveData<List<Asteroid>>
        get() {
            // ---- from yesterday until 7 days from now
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, -1)
            val yesterday = calendar.time.time

            calendar.add(Calendar.DAY_OF_YEAR, 8)
            val oneWeekFromNow = calendar.time.time

            return Transformations.map(
                database.asteroidDao.getAsteroidsFromDates(
                    yesterday, oneWeekFromNow
                )
            ) {
                it.asDomainModel()
            }
        }

    suspend fun getPictureOfDay(): PictureOfDay {
        return try {
            AsteroidApi.retrofitService.getPictureOfDay(API_KEY)
        } catch (e: Exception) {
            PictureOfDay("", "", "")
        }
    }

    suspend fun deleteOldAsteroids() {
        // ---- get yesterday data
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)

        // delete asteroids from this day
        database.asteroidDao.deleteOldAsteroids(calendar.time.time)
    }

    suspend fun refreshAsteroids(): Boolean {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat(API_QUERY_DATE_FORMAT)
        val startDate = format.format(calendar.time)

        calendar.add(Calendar.DAY_OF_YEAR, DEFAULT_END_DATE_DAYS)
        val endDate = format.format(calendar.time)

        return try {
            val response = AsteroidApi.retrofitService.getAsteroids(startDate, endDate, API_KEY)
            if (response.code() != SUCCESS_CODE) {
                false
            } else {
                response.body()?.let {
                    val entityList = parseAsteroidsJsonResult(JSONObject(it))
                        .asDatabaseModel()
                    database.asteroidDao.insertAll(entityList)
                }
                true
            }
        } catch (e: Exception) {
            false
        }
    }
}
