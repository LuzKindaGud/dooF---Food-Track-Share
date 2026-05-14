package com.example.doancoso3.ui.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.doancoso3.MainActivity
import com.example.doancoso3.R
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * Host Activity for the authentication flow (Login / Register fragments).
 * If user is already authenticated, navigates directly to MainActivity.
 */
@AndroidEntryPoint
class AuthActivity : AppCompatActivity() {

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // If user is already logged in, skip auth flow
        if (firebaseAuth.currentUser != null) {
            navigateToMain()
            return
        }

        setContentView(R.layout.activity_auth)
    }

    fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
