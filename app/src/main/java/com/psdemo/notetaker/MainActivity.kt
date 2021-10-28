package com.psdemo.notetaker

import android.app.Activity
import android.content.Intent

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val TAG = MainActivity::class.qualifiedName

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Initialize firebase authentication
        // to support the sign-in providers
        auth = FirebaseAuth.getInstance()

        //Wire up the traditional authentication  to the sign-in Button
        btnSignIn.setOnClickListener { launchSignInFlow() }

        //Wire up the anonymous authentication to the skip btn
        btnSkip.setOnClickListener { signInAnonymously() }

    }

    //Check the current state of the user to prevent them
    // from having to sign-in every time they launch the app
    public override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null && !currentUser.isAnonymous) {
            val intent = Intent(this, ListActivity::class.java)
            intent.putExtra(USER_ID, currentUser.uid)
            startActivity(intent)
        }
    }

    //Enabling anonymous authentication
    private fun signInAnonymously() {
        auth.signInAnonymously()
            .addOnCompleteListener {
                //Captures the response complete event and passes it to the  lamba fun
                if (it.isSuccessful) {
                    loadListActivity()
                } else {
                    Log.e(TAG, "Anonymous sign-in failed", it.exception)
                    Toast.makeText(this, "Sign-in failed", Toast.LENGTH_LONG).show()
                }
            }
    }


    private fun launchSignInFlow() {

        //Whitelist array for specific countries rather than the whole world
        val countries = arrayListOf<String>()
        countries.add("UG")
        countries.add("CD")
        countries.add("RW")


        // Choose authentication providers for the user to sign-in
        // If the user chooses to use their email,
        // they will need to create a password as well.
        val providers = arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(),
            AuthUI.IdpConfig.GoogleBuilder().build(),
            AuthUI.IdpConfig.FacebookBuilder().build(),
            // AuthUI.IdpConfig.PhoneBuilder().setDefaultCountryIso("AU").build()
            AuthUI.IdpConfig.PhoneBuilder().setWhitelistedCountries(countries).build()
        )

        // Create and launch pre-built UI sign-in Activity.
        // This will listen to the response of this activity
        // with the RC_SIGN_IN (request code)
        // to report success or failure.
        val signInIntent = AuthUI.getInstance()
            .createSignInIntentBuilder()
            .setAvailableProviders(providers)
            .setIsSmartLockEnabled(!BuildConfig.DEBUG, true)
            .enableAnonymousUsersAutoUpgrade()//Tell the AuthUI to automatically convert anonymous accounts to standard ones.
            .build()
        startActivityForResult(signInIntent, RC_SIGN_IN)

    }

    companion object {
        const val USER_ID = "user_id"
        const val RC_SIGN_IN = 15
    }


    /**
     * Handle the result that comes back from the AuthUI intent
     * once the user has completed the flow.
     * */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (RC_SIGN_IN == 15) {
            //Get and parse the response from the intent result and parse
            val response = IdpResponse.fromResultIntent(data)

            //Check whether or not the  AuthUI flow was successful via the resultCode
            if (resultCode == Activity.RESULT_OK) {

                loadListActivity() // load the notes upon sign - in success.
                Log.i(
                    TAG,
                    "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}"
                )
            } else {
                // Sign in failed. If response is null the user canceled the
                // sign-in flow using the back button.
                Log.e(TAG, "Sign-in failed ${response?.error?.errorCode}")

            }
        }
    }

    private fun loadListActivity() {
        val user = FirebaseAuth.getInstance().currentUser
        val intent = Intent(this, ListActivity::class.java)
        intent.putExtra(USER_ID, user!!.uid)
        startActivity(intent)
    }

}
