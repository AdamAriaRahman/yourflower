package org.adamrahman.assesmen03.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import org.adamrahman.assesmen03.model.OpStatus
import org.adamrahman.assesmen03.model.Bunga
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

private const val BASE_URL = "https://adamaria.my.id/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface BungaApiService {
    @GET("json.php")
    suspend fun getBunga(
        @Query("auth") userId: String
    ): List<Bunga>

    @Multipart
    @POST("json.php")
    suspend fun postBunga(
        @Part("auth") userId: String,
        @Part("nama") nama: RequestBody,
        @Part("namalatin") namalatin: RequestBody,
        @Part image: MultipartBody.Part
    ): OpStatus

    @DELETE("json.php")
    suspend fun deleteBunga(
        @Query("auth") userId: String,
        @Query("id") id: String
    ): OpStatus
}

object BungaApi {
    val service: BungaApiService by lazy {
        retrofit.create(BungaApiService::class.java)
    }
    fun getBungaUrl(image: String): String {
        return "$BASE_URL$image"
    }
}

enum class ApiStatus { LOADING, SUCCESS, FAILED }