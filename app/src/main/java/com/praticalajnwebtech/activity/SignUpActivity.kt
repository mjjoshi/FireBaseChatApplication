package com.praticalajnwebtech.activity

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Base64
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.praticalajnwebtech.R
import com.praticalajnwebtech.activity.UserDataActivity.Companion.callUserDataActivity
import com.praticalajnwebtech.model.UserModel
import com.praticalajnwebtech.utils.CommanUtils

import kotlinx.android.synthetic.main.activity_signup.*
import java.io.ByteArrayOutputStream


class SignUpActivity : AppCompatActivity() {

    private var bitmap: Bitmap? = null
    private var mFirebaseAuth: FirebaseAuth? = null
    private val MAIN_ACTIVITY_REQUEST_STORAGE = Activity.RESULT_FIRST_USER
    private val ACTION_REQUEST_GALLERY = 99
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        mFirebaseAuth = FirebaseAuth.getInstance()
        defineListeners()


    }


    private fun defineListeners() {
        btnSignUp.setOnClickListener {
            CommanUtils.hideSoftKeyboard(this@SignUpActivity)
            val message: String = checkFields()
            if (!TextUtils.isEmpty(message)) {
                CommanUtils.showSnakbar(findViewById(android.R.id.content), message)
            } else {
                progressBar.visibility = View.VISIBLE
                /**
                 * register user
                 */
                mFirebaseAuth?.createUserWithEmailAndPassword(
                    editEmail.text.toString().trim(),
                    editPassword.text.toString().trim()
                )?.addOnCompleteListener {
                    if (it.isSuccessful) {
                        val userInfo: FirebaseUser = it.result?.user!!
                        mFirebaseAuth?.signInWithEmailAndPassword(
                            editEmail.text.toString().trim(),
                            editPassword.text.toString().trim()
                        )!!.addOnCompleteListener {
                            var database = FirebaseDatabase.getInstance().getReference("users")
                            database = database.child(userInfo.uid)
                            val userModel = UserModel()
                            userModel.user_id = userInfo.uid
                            userModel.first_name = editFullname.text.toString().trim()
                            userModel.last_name = editLastName.text.toString().trim()
                            userModel.email = editEmail.text.toString().trim()
                            userModel.address = editAddress.text.toString().trim()
                            val stream = ByteArrayOutputStream()
                            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                            val b = stream.toByteArray()
                            val encodedImage = Base64.encodeToString(b, Base64.DEFAULT);
                            userModel.profilePic = encodedImage
                            //  userModel.profilePic = "https://i.picsum.photos/id/879/200/300.jpg?grayscale"
                            database.setValue(userModel)
                            database.addValueEventListener(object : ValueEventListener {
                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                    progressBar.visibility = View.GONE
                                    val user = FirebaseAuth.getInstance().currentUser
                                    if (null != user) {
                                        callUserDataActivity(this@SignUpActivity)
                                        Toast.makeText(
                                            this@SignUpActivity,
                                            "Your registration has been successfully done",
                                            Toast.LENGTH_LONG
                                        ).show()

                                        finish()
                                    }
                                }

                                override fun onCancelled(databaseError: DatabaseError) {
                                    progressBar.visibility = View.GONE

                                }
                            })


                        }


                    } else {
                        progressBar.visibility = View.GONE
                        Toast.makeText(this, it.exception?.message, Toast.LENGTH_LONG).show()
                    }

                }

            }

        }

        imgUserAvatar.setOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                if (packageManager.checkPermission(
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        packageName
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    pickFromGallery()
                }
                requestPermissions(
                    arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                    MAIN_ACTIVITY_REQUEST_STORAGE
                )
            } else {
                pickFromGallery()
            }

        }


    }


    private fun loadImage(data: Uri) {
        Glide.with(this@SignUpActivity).load(data)
            .into(imgUserAvatar)


    }

    private fun pickFromGallery() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "image/*"
        val chooser = Intent.createChooser(intent, "Choose a Picture")
        startActivityForResult(chooser, ACTION_REQUEST_GALLERY)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                ACTION_REQUEST_GALLERY -> {
                    val uri = data?.data
                    bitmap = MediaStore.Images.Media.getBitmap(this.contentResolver, uri)
                    loadImage(uri!!)
                }
            }
        }


    }

    companion object {
        fun callSignUpActivity(context: Context) {
            val mIntent = Intent(context, SignUpActivity::class.java)
            context.startActivity(mIntent)
        }
    }


    private fun checkFields(): String {
        var message = ""
        when {
            TextUtils.isEmpty(editFullname.text.toString().trim()) -> message =
                resources.getString(R.string.empty_first_name)
            CommanUtils.isStringNumericOnly(editFullname.text.toString().trim()) -> message =
                resources.getString(R.string.msg_valid_name)
            TextUtils.isEmpty(editLastName.text.toString().trim()) -> message =
                resources.getString(R.string.empty_last_name)
            CommanUtils.isStringNumericOnly(editLastName.text.toString().trim()) -> message =
                resources.getString(R.string.msg_valid_name)
            TextUtils.isEmpty(editEmail.text.toString().trim()) -> message =
                resources.getString(R.string.empty_email)
            !CommanUtils.isValidEmail(editEmail) -> message =
                resources.getString(R.string.msg_valid_email)
            TextUtils.isEmpty(editPassword.text.toString().trim()) -> message =
                resources.getString(R.string.empty_password)
            editPassword.text.toString().trim().length < 6 -> message =
                resources.getString(R.string.msg_valid_password)
            TextUtils.isEmpty(editLastName.text.toString().trim()) -> message =
                resources.getString(R.string.empty_address)
        }
        return message

    }


}