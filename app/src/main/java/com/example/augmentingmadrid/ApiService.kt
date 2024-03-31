package com.example.augmentingmadrid

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Header


interface EmtMadridService {
    @GET("v2/transport/busemtmad/stops/arroundxy/{longitude}/{latitude}/{radius}")
    fun getStopsAround(
        @Header("accessToken") accessToken: String,
        @Path("longitude") longitude: Double,
        @Path("latitude") latitude: Double,
        @Path("radius") radius: Int
    ): Call<BusStopsResponse>
}

data class BusStopsResponse(
    val code: String,
    val description: String,
    val data: List<BusStop>
)


data class BusStop(
    val stopId: Int,
    val stopName: String,
    val address: String,
    val metersToPoint: Int
)



