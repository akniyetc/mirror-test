package com.vp.storage.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(indices = [Index(value = ["title"], unique = true)])
data class MovieDetailDB (
        @PrimaryKey
        val imdbID: String = "",

        val title: String = "",
        val year: String = "",
        val runtime: String = "",
        val director: String = "",
        val plot: String = "",
        val poster: String? = null
)