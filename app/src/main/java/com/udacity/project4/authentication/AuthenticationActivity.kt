package com.udacity.project4.authentication

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.locationreminders.RemindersActivity

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    companion object {
        const val TAG = "Authentication"
        const val SIGN_IN_RESULT_CODE = 1001
    }
    lateinit var customLayout:AuthMethodPickerLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        val login = findViewById<Button>(R.id.login_btn)


        login.setOnClickListener {
            launchSignInFlow()
        }


        if (FirebaseAuth.getInstance().currentUser!=null){
            startActivity(Intent(this,RemindersActivity::class.java))
            this.finish()
        }



         customLayout =  AuthMethodPickerLayout.Builder(R.layout.custom_auth)
            .setGoogleButtonId(R.id.gmail_login_btn)
            .setEmailButtonId(R.id.email_login_btn)
            .build()

    }

    private fun launchSignInFlow() {
        //Give users option to sign in or register with their email or Google account

        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
            AuthUI.getInstance()
                .createSignInIntentBuilder().setIsSmartLockEnabled(false)
                .setAvailableProviders(providers).setAuthMethodPickerLayout(customLayout)
                .build(),
            RemindersActivity.SIGN_IN_REQUEST_CODE
        )

    }

    @SuppressLint("RestrictedApi")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in user.
                startActivity(Intent(this,RemindersActivity::class.java))
                this.finish()
                Toast.makeText(applicationContext,"Welcome back ${response?.email}",Toast.LENGTH_SHORT).show()

            } else {
                // Sign in failed. If response is null the user canceled the sign-in flow using
                // the back button. Otherwise check response.getError().getErrorCode() and handle
                // the error.
                Toast.makeText(applicationContext,"Sign in unsuccessful ${response?.error?.message}",Toast.LENGTH_SHORT).show()
                Log.i(TAG, "Sign in unsuccessful ${response?.error?.errorCode}")
            }
        }
    }
}
