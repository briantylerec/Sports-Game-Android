package com.monksoft.sportsgame

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.monksoft.sportsgame.LoginActivity.Companion.providerSession
import com.monksoft.sportsgame.LoginActivity.Companion.userEmail

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    fun signOff(v: View){
        userEmail = ""
        if(providerSession == "facebook") LoginManager.getInstance().logOut()
        FirebaseAuth.getInstance().signOut()
        startActivity(Intent(this, LoginActivity::class. java))
    }
}