package com.psdemo.notetaker

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.psdemo.notetaker.MainActivity.Companion.SIGN_IN_MESSAGE
import com.psdemo.notetaker.MainActivity.Companion.USER_ID
import kotlinx.android.synthetic.main.activity_new_note.*

class NewNoteActivity : AppCompatActivity() {

    var userId = "-1"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_note)

        //Prompt the user to sign-in in order to create a new note
        val user = FirebaseAuth.getInstance().currentUser
        if (user == null || user.isAnonymous) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra(SIGN_IN_MESSAGE, "Sign-in to create a new note")
            startActivityForResult(intent, ATTEMPT_SIGNIN)
        }

        //Receiving UID from MainActivity

        btnSave.setOnClickListener {
            val resultIntent = Intent()

            if (TextUtils.isEmpty(etTitle.text) || TextUtils.isEmpty(etBody.text)) {
                setResult(Activity.RESULT_CANCELED, resultIntent)
            } else {
                val title = etTitle.text.toString()
                val body = etBody.text.toString()

                resultIntent.putExtra(NEW_TITLE, title)
                resultIntent.putExtra(NEW_BODY, body)
                resultIntent.putExtra(USER_ID, userId)
                setResult(Activity.RESULT_OK, resultIntent)
            }
            finish()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == ATTEMPT_SIGNIN && resultCode == Activity.RESULT_CANCELED) {
            //The user terminate the sign-in flow prior to completion,
            // Hence, exit this activity to prevent them from proceeding and adding a new note.
            finish()
        }else{
            if (data != null && data.hasExtra(USER_ID)){
                userId = data.getStringExtra(USER_ID)
            }
            //Handle the result
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    companion object {
        const val NEW_TITLE = "new_title"
        const val NEW_BODY = "new_body"
        const val ATTEMPT_SIGNIN = 10 //Notify newNoteActivity once mainActivity Completes
    }
}
