package com.example.hikingdom.data.remote.hiking

import com.example.hikingdom.data.remote.BaseRes
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Query

interface HikingRetrofitInterface {
    @POST("hiking")
    fun saveHikingRecourd(@Body saveHikingRecordReq: SaveHikingRecordReq): Call<BaseRes>

//    @GET("info/mountains/location")
//    fun getNearMountains(@Query("lat") lat: Float,  @Query("lng") lng: Float): Call<MountainResponse>
//
//    @GET("hiking/meetups")
//    fun getTodayMeetups(): Call<MeetupResponse>
}