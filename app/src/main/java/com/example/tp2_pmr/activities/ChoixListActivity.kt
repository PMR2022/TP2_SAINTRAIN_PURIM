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
import com.example.tp2_pmr.models.ListTD
import com.example.tp2_pmr.adapters.ListTdAdapter
import com.example.tp2_pmr.models.Profile
import com.example.tp2_pmr.R
import com.example.tp2_pmr.api.Connector
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
                val lists = apiConnector.getLists(hash)
                for (list in lists.lists){
                    profile!!.addList(ListTD(list.label))
                }
                dataSet = profile?.getLists()
            } else {
                val jsonProfile = sharedPreferences?.getString(pseudo, "DEFAULT")
                profile = Gson().fromJson(jsonProfile, Profile::class.java)
                dataSet = profile?.getLists()
            }
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
                val listTD = ListTD(refEdtNewList?.text.toString())
                dataSet!!.add(listTD)
                recyclerList?.adapter?.notifyItemInserted(dataSet!!.size -1)

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