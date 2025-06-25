package com.example.eatpal.data.util

/**
 * A generic class that holds a value with its loading status.
 * @param <T> Type of the resource data
 */
sealed class Resource<out T> {
    class Loading<out T> : Resource<T>()
    data class Success<out T>(val data: T) : Resource<T>()
    data class Error<out T>(val message: String, val data: T? = null) : Resource<T>()

    companion object {
        fun <T> loading(): Resource<T> = Loading()
        fun <T> success(data: T): Resource<T> = Success(data)
        fun <T> error(message: String, data: T? = null): Resource<T> = Error(message, data)
    }
}