package com.example.tp2_pmr.api

import android.app.Application
import com.example.tp2_pmr.models.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.POST
import retrofit2.http.Query

data class Auth(val success : Boolean, val hash : String)

class Connector(application: Application) {
    private var baseurl = "http://tomnab.fr/todo-api/"
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseurl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val requests = retrofit.create<Requests>()
    suspend fun login(user: String, password: String): Auth = requests.login(user, password)
}

interface Requests{
    @POST("authenticate")
    suspend fun login(@Query("user")user:String, @Query("password")password:String) : Auth
}