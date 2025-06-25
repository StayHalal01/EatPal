package com.example.eatpal.data.network

import com.example.eatpal.data.model.NutritionApiResponse
import com.example.eatpal.data.model.SearchResultItem
import com.example.eatpal.data.model.SearchResultResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.Query

/**
 * API Interface for Nutritionix API
 * Documentation: https://developer.nutritionix.com/docs/v1_1
 */
interface NutritionixApi {
    @Headers(
        "x-app-id: 8c6d032a",
        "x-app-key: 379b7ef107a9f0374c903b33dbf629bd"
    )
    @GET("v2/search/instant")
    suspend fun searchFood(
        @Query("query") query: String,
        @Query("detailed") detailed: Boolean = true
    ): SearchResultResponse

    @Headers(
        "x-app-id: 8c6d032a",
        "x-app-key: 379b7ef107a9f0374c903b33dbf629bd"
    )
    @GET("v1_1/item")
    suspend fun getFoodDetails(
        @Query("id") id: String
    ): NutritionApiResponse
}

/**
 * Api Service for food nutrition data
 */
object ApiService {
    private const val BASE_URL = "https://trackapi.nutritionix.com/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val nutritionixApi: NutritionixApi = retrofit.create(NutritionixApi::class.java)
}