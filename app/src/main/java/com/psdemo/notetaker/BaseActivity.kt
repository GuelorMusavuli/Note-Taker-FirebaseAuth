package com.psdemo.notetaker

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser

open class BaseActivity : AppCompatActivity(){


    protected var userId = "-1"
        private set

    protected  var currentUser : FirebaseUser? = null
        private set

    protected var signInMessage = "This action requires signing-in"

    override fun onResume() {
        super.onResume()

<<<<<<< HEAD
        //Redirects the user to MainActivity
        // if they are not signed-in to create a new note
=======
        //Redirects the users to MainActivity
        // if they no signed-in to create a new note
>>>>>>> tmp
        currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser == null || currentUser?.isAnonymous != false
            || currentUser?.providers?.size ?: 0 == 0 ) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(MainActivity.SIGN_IN_MESSAGE, signInMessage)
            startActivityForResult(intent, ATTEMPT_SIGN_IN)
        }
    }

    //Called when the user returns from the MainActivity
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ATTEMPT_SIGN_IN && resultCode == Activity.RESULT_CANCELED) {
            //The user terminate the sign-in flow prior to completion,
            // Hence, exit this activity to prevent them from proceeding and adding a new note.
            finish()
        }else{
            if (data != null && data.hasExtra(MainActivity.USER_ID)){
                userId = data.getStringExtra(MainActivity.USER_ID)
            }
            //Handle the result
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        const val  ATTEMPT_SIGN_IN = 10
    }
}