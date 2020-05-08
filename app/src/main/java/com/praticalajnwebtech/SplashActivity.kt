package com.praticalajnwebtech

import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseUser
import com.praticalajnwebtech.activity.LoginActivity.Companion.callLoginActivity


class SplashActivity : AppCompatActivity() {

    private var mFirebaseUser: FirebaseUser? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        Handler().postDelayed({


            callLoginActivity(this@SplashActivity)
            finish()
//            mFirebaseUser = FirebaseAuth.getInstance().getCurrentUser()
//
//
//            if (mFirebaseUser == null) { // Not signed in, launch the Sign In activity
//                callChatActivity(this@SplashActivity)
//
//            } else {
//                callLoginActivity(this@SplashActivity)
//            }
//            finish()
        }, 1000)


    }
}
