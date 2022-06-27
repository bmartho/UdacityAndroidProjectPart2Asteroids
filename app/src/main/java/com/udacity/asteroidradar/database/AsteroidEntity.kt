package com.udacity.asteroidradar.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.udacity.asteroidradar.Asteroid
import com.udacity.asteroidradar.Constants.API_QUERY_DATE_FORMAT
import java.text.SimpleDateFormat

@Entity
data class AsteroidEntity(
    @PrimaryKey
    val id: Long,
    val codename: String,
    val closeApproachDate: Long,
    val absoluteMagnitude: Double,
    val estimatedDiameter: Double,
    val relativeVelocity: Double,
    val distanceFromEarth: Double,
    val isPotentiallyHazardous: Boolean
)

fun List<AsteroidEntity>.asDomainModel(): List<Asteroid> {
    return map {
        val stringDate = SimpleDateFormat(API_QUERY_DATE_FORMAT).format(it.closeApproachDate)
        Asteroid(
            id = it.id,
            codename = it.codename,
            closeApproachDate = stringDate,
            absoluteMagnitude = it.absoluteMagnitude,
            estimatedDiameter = it.estimatedDiameter,
            relativeVelocity = it.relativeVelocity,
            distanceFromEarth = it.distanceFromEarth,
            isPotentiallyHazardous = it.isPotentiallyHazardous
        )
    }
}

fun List<Asteroid>.asDatabaseModel(): List<AsteroidEntity> {
    return map {
        val longDate = SimpleDateFormat(API_QUERY_DATE_FORMAT).parse(it.closeApproachDate)
        AsteroidEntity(
            id = it.id,
            codename = it.codename,
            closeApproachDate = longDate?.time ?: 0L,
            absoluteMagnitude = it.absoluteMagnitude,
            estimatedDiameter = it.estimatedDiameter,
            relativeVelocity = it.relativeVelocity,
            distanceFromEarth = it.distanceFromEarth,
            isPotentiallyHazardous = it.isPotentiallyHazardous
        )
    }
}