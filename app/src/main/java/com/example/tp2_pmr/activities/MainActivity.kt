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
import java.io.IOException


class MainActivity : AppCompatActivity(), View.OnClickListener {
    val apiConnector: Connector by lazy { Connector(this.application) }
    private var sharedPreferences: SharedPreferences? = null
    private lateinit var refBtnLogin: Button
    private lateinit var refBtnNewUser: Button
    private lateinit var refEdtPseudo: EditText
    private lateinit var refEdtPass: EditText
    private lateinit var refEdtNewPseudo: EditText
    private lateinit var refEdtNewPass: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        sharedPreferences = getSharedPreferences("Profiles",0)

        refEdtPseudo = findViewById(R.id.pseudo)
        refEdtPass = findViewById(R.id.pass)
        refEdtNewPseudo = findViewById(R.id.newPseudo)
        refEdtNewPass = findViewById(R.id.newPass)

        refBtnLogin = findViewById(R.id.btnLogin)
        refBtnNewUser = findViewById(R.id.btnNewUser)
        refBtnLogin.setOnClickListener(this)
        refBtnNewUser.setOnClickListener(this)

        if (!isConnected()) {
            refBtnLogin.isEnabled = false
            refBtnNewUser.isEnabled = false
        }

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
                val pseudo = refEdtPseudo.text.toString()
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
            R.id.btnLogin -> {
                // The string typed by the user
                val pseudo = refEdtPseudo.text.toString()

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
                    var bundle = Bundle()
                    try{
                        val hash = apiConnector.login("tom","web").hash
                        bundle = bundle.apply {
                            putString("pseudo", pseudo)
                            putString("hash", hash)
                        }
                    } catch(exception:Exception){
                        Toast.makeText(applicationContext, "Erreur connexion", Toast.LENGTH_LONG).show()
                        bundle = bundle.apply {
                            putString("pseudo", pseudo)
                        }
                    } finally {
                        // Bundles the pseudo and start choixListActivity
                        val choixListIntent = Intent(applicationContext, ChoixListActivity::class.java).apply {
                            putExtras(bundle)
                        }
                        startActivity(choixListIntent)
                    }
                }
            }
        }
    }

    @Throws(InterruptedException::class, IOException::class)
    fun isConnected(): Boolean {
        val command = "ping -i 5 -c 1 tomnab.fr"
        return Runtime.getRuntime().exec(command).waitFor() == 0
    }
}