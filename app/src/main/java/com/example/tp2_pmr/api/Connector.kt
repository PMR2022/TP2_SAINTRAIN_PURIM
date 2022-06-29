package com.example.tp2_pmr.api

import android.app.Application
import com.example.tp2_pmr.models.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.*

data class Auth(val hash : String)
data class apiListTD(val id:Int, val label:String, var hashUser: String? = null)
data class apiLists(var lists : List<apiListTD>)
data class apiItemTD(val id : Int, val label :String, var checked : Boolean, var idListe : Int? = null)
data class apiItems(var items : List<apiItemTD>)

class Connector(application: Application) {
    private var baseurl = "http://tomnab.fr/todo-api/"
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseurl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val requests = retrofit.create<Requests>()
    suspend fun login(user: String, password: String): Auth = requests.login(user, password)
    suspend fun getLists(hash: String): apiLists = requests.getLists(hash)
    suspend fun getItems(hash: String, id: Int): apiItems = requests.getItems(hash, id)
}

interface Requests{
    @POST("authenticate")
    suspend fun login(@Query("user")user:String, @Query("password")password:String) : Auth
    @GET("lists")
    suspend fun getLists(@Header("hash") hash : String) : apiLists
    @GET("lists/{idList}/items")
    suspend fun getItems(@Header("hash") hash : String,@Path("idList") idList: Int): apiItems
}