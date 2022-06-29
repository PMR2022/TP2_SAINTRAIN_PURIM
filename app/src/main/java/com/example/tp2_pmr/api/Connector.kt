package com.example.tp2_pmr.api

import android.app.Application
import androidx.room.*
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.create
import retrofit2.http.*
import retrofit2.http.Query

data class Auth(val hash : String)
@Entity
data class apiListTD(@PrimaryKey val id:Int, val label:String, val userHash:String? = null)
@Entity
data class apiItemTD(@PrimaryKey val id:Int, val label:String, var checked:Boolean, val idList: Int? = -1)
data class apiLists(var lists : List<apiListTD>)
data class apiItems(var items : List<apiItemTD>)

class Connector(application: Application, baseurl: String = "http://tomnab.fr/todo-api/") {
    // API part
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(baseurl)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // API Calls
    private val requests = retrofit.create<Requests>()
    suspend fun login(user: String, password: String): Auth = requests.login(user, password)
    suspend fun getLists(hash: String): apiLists = requests.getLists(hash)
    suspend fun getItems(hash: String, id: Int): apiItems = requests.getItems(hash, id)
    suspend fun setList(hash: String, listName: String): apiItems = requests.createList(hash, listName)
    suspend fun setItem(hash: String, idList: Int, itemName: String): apiItemTD = requests.createItem(hash, idList, itemName)
    suspend fun checkItem(hash: String, idList: Int, idItem: Int, checked: Boolean) = requests.checkItem(hash, idList, idItem, checked)

    // Database part
    private val database = Room.databaseBuilder(
        application,
        DatabasePMR::class.java, "db_pmr"
    ).fallbackToDestructiveMigration().build()
    private val dao = database.daoDB()
    // Keep the functions with the same returns as the API for consistency and same name of functions
    //suspend fun dbGetLists(hash: String): apiLists {
    //    return apiLists(Dao.getLists(hash))
    //}
    //suspend fun dbGetItems(id: Int): apiItems {
    //    return apiItems(Dao.getItems(id))
    //}
}

interface Requests{
    @POST("authenticate")
    suspend fun login(@Query("user")user:String, @Query("password")password:String) : Auth
    @GET("lists")
    suspend fun getLists(@Header("hash") hash : String) : apiLists
    @GET("lists/{idList}/items")
    suspend fun getItems(@Header("hash") hash : String,@Path("idList") idList: Int): apiItems
    @POST("lists")
    suspend fun createList(@Header("hash") hash : String,@Query("label") listName: String) : apiItems
    @POST("lists/{idList}/items")
    suspend fun createItem(@Header("hash") hash : String,@Path("idList") idList:Int ,@Query("label") listName: String) : apiItemTD
    @PUT("lists/{idList}/items/{idItem}")
    suspend fun checkItem(@Header("hash") hash : String,@Path("idList") idList:Int,@Path("idItem") idItem: Int, @Query("check") checked : Boolean)
}

@androidx.room.Database(entities = [apiListTD::class, apiItemTD::class],version = 8)
abstract class DatabasePMR : RoomDatabase() { abstract fun daoDB() : DaoInterface }

@Dao
interface DaoInterface{
    // Get
    /* @androidx.room.Query("SELECT * FROM apiListTD WHERE userHash LIKE :hash")
    suspend fun getLists(hash:String) : List<apiListTD>
    @androidx.room.Query("SELECT * FROM apiItemTD WHERE id LIKE :id")
    suspend fun getItems(id:Int) : List<apiItemTD>
    // Insert / Update */
    @androidx.room.Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertList(lists : apiListTD)
    @androidx.room.Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItem(items : apiItemTD)
}
