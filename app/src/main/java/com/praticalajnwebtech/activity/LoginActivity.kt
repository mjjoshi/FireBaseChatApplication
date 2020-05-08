package com.praticalajnwebtech.activity

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.praticalajnwebtech.activity.SignUpActivity.Companion.callSignUpActivity
import com.praticalajnwebtech.activity.UserDataActivity.Companion.callUserDataActivity
import com.praticalajnwebtech.R
import com.praticalajnwebtech.utils.CommanUtils
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {


    private var mFirebaseAuth: FirebaseAuth? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        defineListeners()
        mFirebaseAuth = FirebaseAuth.getInstance()

    }

    private fun defineListeners() {
        btnLogin.setOnClickListener {
            CommanUtils.hideSoftKeyboard(this@LoginActivity)
            val message: String = checkFields()
            if (!TextUtils.isEmpty(message)) {
                CommanUtils.showSnakbar(findViewById(android.R.id.content), message)
            } else {
                progressBar.visibility = View.VISIBLE
                /**
                 * login user
                 */
                mFirebaseAuth?.signInWithEmailAndPassword(
                    editEmail.text.toString().trim(),
                    editPassword.text.toString().trim()
                )
                    ?.addOnCompleteListener {
                        if (it.isSuccessful) {
                            progressBar.visibility = View.GONE
                            editEmail.setText("")
                            editPassword.setText("")
                            Toast.makeText(this, "Logged In SucessFully", Toast.LENGTH_LONG).show()
                            callUserDataActivity(this@LoginActivity)
                        } else {
                            progressBar.visibility = View.GONE
                            Toast.makeText(this, it.exception?.message, Toast.LENGTH_LONG).show()
                        }

                    }

            }

        }

        txtSignIn.setOnClickListener {
            callSignUpActivity(this@LoginActivity)
        }
    }

    companion object {
        fun callLoginActivity(context: Context) {
            val mIntent = Intent(context, LoginActivity::class.java)
            context.startActivity(mIntent)
        }
    }


    private fun checkFields(): String {
        var message = ""
        when {
            TextUtils.isEmpty(editEmail.text.toString().trim()) -> message =
                resources.getString(R.string.empty_email)
            !CommanUtils.isValidEmail(editEmail) -> message =
                resources.getString(R.string.msg_valid_email)
            TextUtils.isEmpty(editPassword.text.toString().trim()) -> message =
                resources.getString(R.string.empty_password)
            editPassword.text.toString().trim().length < 6 -> message =
                resources.getString(R.string.msg_valid_password)
        }
        return message

    }


}