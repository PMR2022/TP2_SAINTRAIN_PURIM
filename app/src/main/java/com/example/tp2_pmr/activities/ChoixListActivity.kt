package com.example.tp2_pmr.activities

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tp2_pmr.R
import com.example.tp2_pmr.adapters.ListTdAdapter
import com.example.tp2_pmr.api.Connector
import com.example.tp2_pmr.api.apiLists
import com.example.tp2_pmr.models.ItemTD
import com.example.tp2_pmr.models.ListTD
import com.example.tp2_pmr.models.Profile
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class ChoixListActivity : AppCompatActivity(), View.OnClickListener {
    val apiConnector: Connector by lazy { Connector(this.application) }
    private var sharedPreferences: SharedPreferences? = null
    private var profile: Profile? = null
    private var dataSet: MutableList<ListTD>? = null

    private var recyclerList : RecyclerView? = null
    private var refBtnOK: Button? = null
    private var refEdtNewList: EditText? = null

    private val choixListActivityScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choix_list)

        refEdtNewList = findViewById(R.id.newList)
        refBtnOK = findViewById(R.id.btnLogin)
        refBtnOK?.setOnClickListener(this)

        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        sharedPreferences = getSharedPreferences("Profiles",0)
        // Gets profile from the bundle
        val extras = intent.extras
        val pseudo = extras?.getString("pseudo")
        val hash = extras?.getString("hash")
        choixListActivityScope.launch {
            if(hash != null) {
                profile = Profile(pseudo!!)
                var apiLists: apiLists?
                try {
                    apiLists = apiConnector.getLists(hash)
                }catch(exc: Exception){
                    Toast.makeText(applicationContext,"Using local DB",Toast.LENGTH_SHORT).show()
                    apiLists = apiConnector.getLists(hash)
                    //apiLists = apiConnector.dbGetLists(hash)
                }
                if (apiLists != null) {
                    for (apiList in apiLists.lists){
                        val newItems = mutableListOf<ItemTD>()
                        for (apiItem in apiConnector.getItems(hash, apiList.id).items){
                            newItems.add(ItemTD(apiItem.label,apiItem.checked,apiItem.id,apiItem.id))
                        }
                        val newList = ListTD(apiList.label,apiList.id)
                        newList.setItems(newItems)
                        profile!!.addList(newList)
                    }
                } else {
                    Toast.makeText(applicationContext,"API and DB failed to connect",Toast.LENGTH_SHORT).show()
                }
                // Saves new user profile
                val profileGson = Gson().toJson(profile)
                sharedPreferences?.edit()?.apply {
                    putString(profile?.getLogin(), profileGson)
                    apply()
                }
            } else {
                val jsonProfile = sharedPreferences?.getString(pseudo, "DEFAULT")
                profile = Gson().fromJson(jsonProfile, Profile::class.java)
            }
            dataSet = profile?.getLists()
            recyclerList = findViewById(R.id.list)
            recyclerList?.adapter = ListTdAdapter(profile!!,dataSet!!)
            recyclerList?.layoutManager = LinearLayoutManager(applicationContext, LinearLayoutManager.VERTICAL, false)
        }
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
                //Creates new listTD and update the dataset
                val listName = refEdtNewList?.text.toString()
                // Creates with name and id of the current size
                val listTD = ListTD(listName,dataSet!!.size,profile?.hash)
                dataSet!!.add(listTD)
                recyclerList?.adapter?.notifyItemInserted(dataSet!!.size -1)
                // Inserts on API
                choixListActivityScope.launch {
                    try{
                        val hash = intent.extras?.getString("hash")
                        if(hash != null){
                            apiConnector.setList(hash, listName)
                        }
                    } catch(exc: Exception){
                        Toast.makeText(applicationContext, "Failed to save to API or DB", Toast.LENGTH_SHORT).show()
                    }
                }
                // Saves new user profile
                val profileGson = Gson().toJson(profile)
                sharedPreferences?.edit()?.apply {
                    putString(profile?.getLogin(), profileGson)
                    apply()
                }
            }
        }
    }
}