package com.praticalajnwebtech.utils

import android.Manifest
import android.app.Activity
import android.app.Dialog
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import android.telephony.TelephonyManager
import android.text.format.Formatter
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.View
import android.view.animation.Animation
import android.view.animation.DecelerateInterpolator
import android.view.animation.TranslateAnimation
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.annotation.VisibleForTesting
import androidx.appcompat.app.AlertDialog
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import com.google.android.material.snackbar.Snackbar
import java.net.NetworkInterface
import java.util.regex.Pattern
import kotlin.math.roundToInt


object CommanUtils {

    private var mProgressDialog: Dialog? = null
    private lateinit var prefsDefault: SharedPreferences



    /**
     * old method but working perfactly (https://stackoverflow.com/questions/3407256/height-of-status-bar-in-android)
     */
    fun getStatusBarHeight(mContext: Context): Int {
        var result = 0
        val resourceId = mContext.resources.getIdentifier("status_bar_height", "dimen", "android")
        if (resourceId > 0) {
            result = mContext.resources.getDimensionPixelSize(resourceId)
        }
        return result
    }



    fun setBackgroundClickEffect(mContext: Context, view: View) {

        val outValue = TypedValue()
        mContext.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
        view.setBackgroundResource(outValue.resourceId)

    }

    fun slideToDown(view: View) {
        val slide = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 5.2f)
        slide.duration = 300
//        slide.interpolator = DecelerateInterpolator()
        slide.fillAfter = true
        slide.isFillEnabled = true
        view.startAnimation(slide)
        slide.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationRepeat(animation: Animation) {}

            override fun onAnimationEnd(animation: Animation) {
                view.clearAnimation()
                view.visibility = View.GONE
            }
        })
    }

    fun slideToAbove(view: View) {
        val slide = TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 5.0f, Animation.RELATIVE_TO_SELF, 0.0f)
        slide.duration = 300
        slide.interpolator = DecelerateInterpolator()
        slide.fillAfter = true
        slide.isFillEnabled = true
        view.startAnimation(slide)
        slide.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationStart(animation: Animation) {
            }

            override fun onAnimationRepeat(animation: Animation) {}
            override fun onAnimationEnd(animation: Animation) {
                view.clearAnimation()
                view.visibility = View.VISIBLE

            }
        })
    }

    fun setImageClickEffect(mContext: Context, view: View) {

        val outValue = TypedValue()
        mContext.theme.resolveAttribute(android.R.attr.actionBarItemBackground, outValue, true)
        view.setBackgroundResource(outValue.resourceId)

    }

    fun isStringNumericOnly(string: String): Boolean {

        val regex = "[+-]?[0-9][0-9]*"
        val pattern = Pattern.compile(regex)

        if (pattern.matcher(string).matches()) {
            return true
        }
        return false
    }





    fun getAppVersion(context: Context): Int {
        try {
            return context.packageManager.getPackageInfo(context.packageName, 0).versionCode
        } catch (e: PackageManager.NameNotFoundException) {
            // should never happen
            throw RuntimeException("Could not get package noOfClothes: $e")
        }
    }

    fun getDeviceId(mContext: Context): String {
        return Settings.Secure.getString(mContext.contentResolver, Settings.Secure.ANDROID_ID)
    }

    val deviceModel: String
        get() {
            val manufacturer = Build.MANUFACTURER
            val model = Build.MODEL
            return if (model.startsWith(manufacturer)) {
                capitalize(model)
            } else {
                capitalize(manufacturer) + " " + model
            }
        }

    private fun capitalize(deviceModel: String?): String {
        if (deviceModel == null || deviceModel.isEmpty()) {
            return ""
        }
        val first = deviceModel[0]
        return if (Character.isUpperCase(first)) {
            deviceModel
        } else {
            Character.toUpperCase(first) + deviceModel.substring(1)
        }
    }

    val deviceOSVersion = Build.VERSION.SDK_INT.toString()


    //--- get IMEI number of Device
    @RequiresPermission(Manifest.permission.READ_PHONE_STATE)
    fun getIMEINumber(mContext: Context): String? {
        val telephonyManager = mContext.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        return if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {

            null
        } else telephonyManager.deviceId.toString()

    }

    //--- get ip address of mobile
    val ipAddress: String?
        @Throws(Exception::class)
        get() {
            val en = NetworkInterface.getNetworkInterfaces()
            while (en.hasMoreElements()) {
                val intf = en.nextElement()
                val enumIpAddr = intf.inetAddresses
                while (enumIpAddr.hasMoreElements()) {
                    val inetAddress = enumIpAddr.nextElement()
                    if (!inetAddress.isLoopbackAddress) {
                        return Formatter.formatIpAddress(inetAddress.hashCode())
                    }
                }
            }
            return null
        }


    fun showSettingsSnackBar(rootLayout: CoordinatorLayout, settingsText: String = "Settings", message: String) {
        val snackbar = Snackbar
                .make(rootLayout, message, Snackbar.LENGTH_LONG)
                .setAction("Settings") {
                    val intent = Intent()
                    intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                    val uri = Uri.fromParts("package", rootLayout.context.packageName, null)
                    intent.data = uri
                    rootLayout.context.startActivity(intent)
                }
        snackbar.show()
    }


    private fun showSettingsDialog(mContext: Context, settingsText: String? = "Settings", cancelText: String? = "Cancel", title: String, message: String) {
        val alertSettings = AlertDialog.Builder(mContext)
        alertSettings.setTitle(title)
        alertSettings.setMessage(message)
        alertSettings.setPositiveButton("Settings") { dialog, which ->
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", mContext.packageName, null)
            intent.data = uri
            mContext.startActivity(intent)
        }
        alertSettings.setNegativeButton("Cancel") { dialog, which -> dialog.dismiss() }
        alertSettings.show()
    }

    fun dpToPx(context: Context, dp: Int) = (dp * getPixelScaleFactor(context)).roundToInt()

    fun pxToDp(context: Context, px: Int) = (px / getPixelScaleFactor(context)).roundToInt()

    private fun getPixelScaleFactor(context: Context): Float {
        val displayMetrics = context.resources.displayMetrics
        return displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT
    }


    fun hideSoftKeyboard(activity: Activity) {
        if (activity.currentFocus != null) {
            val inputMethodManager = activity.getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(activity.currentFocus!!.windowToken, 0)
        }
    }

    fun showSoftKeyboard(activity: Activity, mView: View) {
        if (activity.currentFocus != null) {
            val inputMethodManager = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.toggleSoftInputFromWindow(mView.applicationWindowToken, InputMethodManager.SHOW_FORCED, 0)
        }
    }



    fun hideLoader() {
        if (mProgressDialog != null && mProgressDialog!!.isShowing)
            mProgressDialog!!.dismiss()

    }

    fun showToast(mContext: Context? = null, message: String) = Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()

    fun showLongToast(mContext: Context, message: String) = Toast.makeText(mContext, message, Toast.LENGTH_LONG).show()

    fun showSnakbar(rootView: View, mMessage: String) = Snackbar.make(rootView, mMessage, Snackbar.LENGTH_SHORT).show()

    fun showLongSnakbar(rootView: View, mMessage: String) = Snackbar.make(rootView, mMessage, Snackbar.LENGTH_LONG).show()

    fun isValidEmail(editText: EditText): Boolean {
        val pattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$")
        val matcher = pattern.matcher(editText.text.toString())
        return matcher.matches()
    }


    @VisibleForTesting
    fun isValidEmail(editText: String): Boolean {
        //val EMAIL_REGEX = "^[\\w-\\+]+(\\.[\\w]+)*@[\\w-]+(\\.[\\w]+)*(\\.[a-z]{2,})$"
        val pattern = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,4}$")
        val matcher = pattern.matcher(editText)
        return matcher.matches()
    }

    @VisibleForTesting
    fun isValidPhoneNumber(editText: String): Boolean {
        val PHONE_REGEX = "^\\+(?:[0-9] ?){6,14}[0-9]\$"
        val pattern = Pattern.compile(PHONE_REGEX)
        val matcher = pattern.matcher(editText)
        return matcher.matches()
    }


}
