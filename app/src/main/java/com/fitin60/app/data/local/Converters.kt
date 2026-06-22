package com.fitin60.app.data.local

import androidx.room.TypeConverter

class Converters {

    @TypeConverter
    fun fromList(list: List<String>?): String =
        list?.joinToString(SEPARATOR) ?: ""

    @TypeConverter
    fun toList(value: String?): List<String> =
        if (value.isNullOrBlank()) emptyList()
        else value.split(SEPARATOR)

    companion object {
        private const val SEPARATOR = "␟"
    }
}
