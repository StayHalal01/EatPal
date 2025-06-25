package com.example.eatpal.data.network

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.net.SocketTimeoutException
import java.util.concurrent.TimeUnit

/**
 * Network module for handling API communications
 */
object NetworkModule {
    private const val TAG = "NetworkModule"
    private const val TIMEOUT_SECONDS = 30L
    private const val BASE_URL = "https://trackapi.nutritionix.com/"

    // Create OkHttp Client with timeouts
    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()

    // Configure Gson for API responses
    private val gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    // Create Retrofit instance
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()

    // Create API Service
    val nutritionixApi: NutritionixApi = retrofit.create(NutritionixApi::class.java)

    /**
     * Wrapper to safely make API calls with error handling
     */
    suspend fun <T> safeApiCall(apiCall: suspend () -> Response<T>): ApiResult<T> {
        return try {
            val response = apiCall()
            if (response.isSuccessful) {
                ApiResult.Success(response.body())
            } else {
                Log.e(TAG, "API Error: ${response.code()} - ${response.message()}")
                ApiResult.Error("API Error: ${response.code()} - ${response.message()}")
            }
        } catch (e: HttpException) {
            Log.e(TAG, "HttpException", e)
            ApiResult.Error("Network error: ${e.message}")
        } catch (e: SocketTimeoutException) {
            Log.e(TAG, "SocketTimeoutException", e)
            ApiResult.Error("Network timeout - please try again")
        } catch (e: IOException) {
            Log.e(TAG, "IOException", e)
            ApiResult.Error("Network unavailable - check your connection")
        } catch (e: Exception) {
            Log.e(TAG, "Exception", e)
            ApiResult.Error("An unexpected error occurred")
        }
    }
}

/**
 * Sealed class to handle API results
 */
sealed class ApiResult<out T> {
    data class Success<out T>(val data: T?) : ApiResult<T>()
    data class Error(val message: String) : ApiResult<Nothing>()
}