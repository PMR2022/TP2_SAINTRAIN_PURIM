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
import com.example.tp2_pmr.models.Profile
import com.example.tp2_pmr.api.Connector
import com.example.tp2_pmr.R
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity(), View.OnClickListener {
    val apiConnector: Connector by lazy { Connector(this.application) }
    private var sharedPreferences: SharedPreferences? = null
    private var refBtnOK: Button? = null
    private var refEdtPseudo: EditText? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("Profiles",0)

        refEdtPseudo = findViewById(R.id.pseudo)

        refBtnOK = findViewById(R.id.btnOK)
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
                val pseudo = refEdtPseudo?.text.toString()
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

    private val mainActivityScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Main
    )

    override fun onClick(view: View) {
        when(view.id) {
            R.id.btnOK -> {
                // The string typed by the user
                val pseudo = refEdtPseudo?.text.toString()

                // Creates a new profile if necessary
                if (sharedPreferences?.contains(pseudo) == false) {
                    val profile = Profile(pseudo)
                    val profileGson = Gson().toJson(profile)
                    sharedPreferences?.edit()?.apply() {
                        putString(pseudo, profileGson)
                        apply()
                    }
                }
                // Test
                mainActivityScope.launch {
                    try{
                        val hash = apiConnector.login("tom","web").hash
                        Toast.makeText(applicationContext, hash, Toast.LENGTH_LONG).show()
                    } catch(exception:Exception){
                        Toast.makeText(applicationContext, "Erreur connexion", Toast.LENGTH_LONG).show()
                    }
                }

                // Bundles the pseudo and start choixListActivity
                val bundle = Bundle().apply {
                    putString("pseudo", pseudo)
                }
                val choixListIntent = Intent(this, ChoixListActivity::class.java).apply {
                    putExtras(bundle)
                }
                //startActivity(choixListIntent)
            }
        }
    }
}