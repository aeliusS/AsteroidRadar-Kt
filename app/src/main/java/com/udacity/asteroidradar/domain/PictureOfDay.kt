package com.udacity.asteroidradar.domain

data class PictureOfDay(val mediaType: String,
                        val title: String,
                        val url: String) {
    val isImage
        get() = mediaType == "image"
}