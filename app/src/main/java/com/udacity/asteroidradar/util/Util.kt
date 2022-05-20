package com.udacity.asteroidradar.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import com.udacity.asteroidradar.repository.AsteroidsRepository
import java.text.SimpleDateFormat
import java.util.*

/** Helper function to get the API key **/
fun getMetaData(context: Context): Bundle? {
    return context.packageManager.getApplicationInfo(
        context.packageName,
        PackageManager.GET_META_DATA
    ).metaData
}

fun getDateRangeFormatted(): DateRange {
    val calendar = Calendar.getInstance()
    return DateRange(
        startDate = formatDate(calendar),
        endDate = formatDate(calendar, Constants.DEFAULT_END_DATE_DAYS)
    )
}

fun formatDate(calendar: Calendar, daysToAdd: Int? = null): String {
    if (daysToAdd != null) calendar.add(Calendar.DAY_OF_YEAR, daysToAdd)
    val currentTime = calendar.time
    val dateFormat = SimpleDateFormat(
        if (Build.VERSION.SDK_INT >= 24) Constants.API_QUERY_DATE_FORMAT else Constants.API_QUERY_DATE_FORMAT_FALLBACK,
        Locale.getDefault()
    )
    return dateFormat.format(currentTime)
}

data class DateRange(val startDate: String, val endDate: String)