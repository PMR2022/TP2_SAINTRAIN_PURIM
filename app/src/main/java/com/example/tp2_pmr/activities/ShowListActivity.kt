package com.example.tp2_pmr.activities

import android.content.Intent
import android.content.SharedPreferences
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tp2_pmr.*
import com.example.tp2_pmr.adapters.ItemTdAdapter
import com.example.tp2_pmr.api.Connector
import com.example.tp2_pmr.models.ItemTD
import com.example.tp2_pmr.models.ListTD
import com.example.tp2_pmr.models.Profile
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ShowListActivity : AppCompatActivity(), View.OnClickListener {
    val apiConnector: Connector by lazy { Connector(this.application) }

    private var sharedPreferences: SharedPreferences? = null
    private var profile: Profile? = null
    private var listTD: ListTD? = null
    private var dataSet: MutableList<ItemTD>? = null

    private var recyclerList : RecyclerView? = null
    private var refBtnOK: Button? = null
    private var refEdtNewItem: EditText? = null

    private val showListActivityScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_show_list)

        val extras = intent.extras
        val pseudo = extras?.getString("pseudo")
        val listTdIndex = extras?.getInt("listTD index")
        // Gets listTD from the bundle
        sharedPreferences = getSharedPreferences("Profiles",0)
        val jsonProfile = sharedPreferences?.getString(pseudo,"DEFAULT")
        profile = Gson().fromJson(jsonProfile, Profile::class.java)
        listTD = profile?.getLists()?.get(listTdIndex!!)

        dataSet = listTD?.getItems()

        recyclerList = findViewById(R.id.list)
        recyclerList?.adapter = ItemTdAdapter(profile!!,dataSet!!)
        recyclerList?.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        recyclerList?.setOnClickListener(this)

        refEdtNewItem = findViewById(R.id.newItem)
        refBtnOK = findViewById(R.id.btnLogin)
        refBtnOK?.setOnClickListener(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_preferences -> {
                val extras = intent.extras
                val pseudo = extras?.getString("pseudo")
                val bundle = Bundle().apply {
                    putString("pseudo", pseudo)
                }
                val settingsIntent = Intent(this, SettingsActivity::class.java).apply {
                    putExtras(bundle)
                }
                startActivity(settingsIntent)
            }
        }
        return true
    }

    override fun onClick(view: View) {
        when (view.id){
            R.id.btnLogin -> {
                // Creates new itemTD with name, current list id size+1 and  and update the dataset
                val itemName = refEdtNewItem?.text.toString()
                val listId = intent.extras!!.getInt("listTD index")
                val itemTD = ItemTD(itemName,false,dataSet!!.size,listId, profile?.hash)
                dataSet!!.add(itemTD)
                recyclerList?.adapter?.notifyItemInserted(dataSet!!.size-1)
                // Inserts on API
                showListActivityScope.launch {
                    try{
                        val hash = intent.extras?.getString("hash")
                        if(hash != null){
                            apiConnector.setItem(hash,listId,itemName)
                        }
                    } catch(exc: Exception){
                        Toast.makeText(applicationContext, "Failed to save to API", Toast.LENGTH_SHORT).show()
                    }
                }
                // Saves new user profile
                val profileGson = Gson().toJson(profile)
                sharedPreferences?.edit()?.apply(){
                    putString(profile?.getLogin(),profileGson)
                    apply()
                }
            }
        }
    }
}