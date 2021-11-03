package com.psdemo.notetaker

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.firebase.ui.auth.AuthUI
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import kotlinx.android.synthetic.main.activity_settings.*
/**
 * This class allows the user to interact with their account, by
 * signing-out, modifying the username and deleting the account.
 * */
class SettingsActivity : BaseActivity() {


    

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        actionBar?.setDisplayHomeAsUpEnabled(true)

        //Sign-in message to be displayed by MainActivity
        signInMessage = "Sign-in to see your settings"

        val authUI = AuthUI.getInstance()

        //Wire up the buttons
        btnSignOut.setOnClickListener {
            authUI
                .signOut(this)
                .addOnCompleteListener(){
                    if (it.isSuccessful){
                        //Send the user back and finish this activity.
                        startActivity(Intent(this, MainActivity::class.java))
                    }else{
                        Log.e(TAG, "Sign-out failed", it.exception)
                        Toast.makeText(this, "Sign-out failed", Toast.LENGTH_LONG).show()
                    }
                }
        }

        btnDelete.setOnClickListener {
            MaterialAlertDialogBuilder(this, R.style.DialogTheme)
                .setTitle("Delete Account")
                .setMessage("This is permanent, are you sure ?")
                .setPositiveButton("Yes"){ _,_ ->
                //start the delegate call that will respond to a Yes click
                    authUI
                        .delete(this)
                        .addOnCompleteListener(){
                            if (it.isSuccessful){
                                startActivity(Intent(this, MainActivity::class.java))
                            }else{
                                Log.e(TAG, "Delete account failed", it.exception)
                                Toast.makeText(this, "Delete account failed", Toast.LENGTH_LONG).show()
                            }
                        }
                }
                .setNegativeButton("No", null)
                .show()

        }
    }

    //Populate the name textField
    // when the screen is presented
    override fun onResume() {
        super.onResume()
        etName.setText(currentUser?.displayName)
    }

    //Save and update the username when the activity is faded to the BG
    override fun onPause() {
        super.onPause()
        val profile = UserProfileChangeRequest.Builder()
                .setDisplayName(etName.text.toString())
                .build()

        if (currentUser != null) {
            currentUser!!.updateProfile(profile)
                .addOnCompleteListener { task ->
                    if (!task.isSuccessful) {
                        Log.e(TAG, "Failed to update display name", task.exception)
                        Toast.makeText(this, "Name update failed", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    companion object {
        private val TAG = SettingsActivity::class.qualifiedName
    }
}
