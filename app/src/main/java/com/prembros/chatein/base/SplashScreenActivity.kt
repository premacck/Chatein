package com.prembros.chatein.base

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.prembros.chatein.ui.auth.StartActivity
import com.prembros.chatein.ui.main.MainActivity

class SplashScreenActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val mFirebaseAuth = FirebaseAuth.getInstance()
        val mFirebaseUser = mFirebaseAuth.currentUser

        startActivity(Intent(
                this,
                if (mFirebaseUser == null) StartActivity::class.java else MainActivity::class.java
        ))
        finish()
    }
}
