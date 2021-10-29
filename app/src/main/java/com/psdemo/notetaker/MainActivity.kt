package com.psdemo.notetaker

import android.app.Activity
import android.content.Intent
import android.opengl.Visibility

import android.os.Bundle
import android.util.Log
import android.view.View.INVISIBLE
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val TAG = MainActivity::class.qualifiedName
    private var referred = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //Initialize firebase authentication
        // to support the sign-in providers
        auth = FirebaseAuth.getInstance()

        //Check the current state of the user to prevent them
        // from having to sign-in every time they launch the app
        // If user is signed in. update UI accordingly.
        val currentUser = auth.currentUser
        if (currentUser != null && !currentUser.isAnonymous
            && currentUser.providers?.size ?: 0 > 0) {

            val intent = Intent(this, ListActivity::class.java)
            intent.putExtra(USER_ID, currentUser.uid)
            startActivity(intent)
        }

        //Wire up the traditional authentication  to the sign-in Button
        btnSignIn.setOnClickListener { launchSignInFlow() }

        //Check if the calling activity sent a sign-in message
        if (intent.hasExtra(SIGN_IN_MESSAGE)) {
            btnSkip.visibility = INVISIBLE
            tvMessage.text = intent.getStringExtra(SIGN_IN_MESSAGE)
            referred = true // when this activity was referred by another that requires sign-in
        } else {
            //Wire up the anonymous authentication to the skip btn
            btnSkip.setOnClickListener { signInAnonymously() }
        }

    }

    /**
     * Set the result of the calling activity to canceled.
     * This will signal to original activity that the sign-in was aborted and
     * not to show the intended functionality.(addNewNote)
     * */
    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
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
            .setTheme(R.style.SignInTheme)
            .build()
        startActivityForResult(signInIntent, RC_SIGN_IN)

    }

    companion object {
        const val USER_ID = "user_id"
        const val RC_SIGN_IN = 15
        const val SIGN_IN_MESSAGE = "sign_in_message"
    }


    /**
     * Handle the result that comes back from the AuthUI intent
     * once the user has completed the sign-in process.
     * */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (RC_SIGN_IN == 15) {
            //Get and parse the response from the intent result and parse
            val response = IdpResponse.fromResultIntent(data)

            //Check whether or not the  AuthUI flow was successful via the resultCode
            if (resultCode == Activity.RESULT_OK) {

                //Inform the calling activity that this one has completed successfully
                if (referred) {
                    setResult(Activity.RESULT_OK)
                    finish()
                } else {
                    Log.i(
                        TAG,
                        "Successfully signed in user ${FirebaseAuth.getInstance().currentUser?.displayName}"
                    )
                    loadListActivity() // load the notes upon sign - in success.
                }


            } else {
                // Sign in failed due to merge conflict.
                if (response != null && response.error != null &&
                    response.error!!.errorCode == ErrorCodes.ANONYMOUS_UPGRADE_MERGE_CONFLICT
                ) {
                    //save temporarily any data associated with the anonymous user
                    // so that it can be associated with the existing account that the user
                    // is trying to log into, and then re-save it tied to the new account.
                    val existingCredential = response.credentialForLinking
                    if (existingCredential != null) {
                        auth.signInWithCredential(existingCredential)
                            .addOnSuccessListener {
                                if (referred) {
                                    val user = auth.currentUser
                                    val intentUser = Intent()
                                    intentUser.putExtra(USER_ID, user!!.uid)
                                    setResult(Activity.RESULT_OK, intentUser)
                                    finish() //go back to NewNoteActivity
                                } else {
                                    //here this activity wasn't skipped.
                                    // Head straight to ListActivity
                                    loadListActivity()
                                }
                            }
                    }


                }

                // If sign-in failed for a reason other than anonymous merge conflict.
                // Here we are not notifying users that their sign-in failed when they
                // just cancelled instead. Moreover, the message shouldn't be displayed
                // on success sign-in either.
                if (resultCode != Activity.RESULT_CANCELED && resultCode != Activity.RESULT_OK) {
                    Log.e(TAG, "Sign-in failed", response?.error)
                    Toast.makeText(this, "Sign-in failed", Toast.LENGTH_LONG).show()
                }

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
