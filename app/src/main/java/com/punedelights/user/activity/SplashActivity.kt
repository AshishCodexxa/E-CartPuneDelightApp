package com.punedelights.user.activity

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONException
import org.json.JSONObject
import com.punedelights.user.R
import com.punedelights.user.helper.ApiConfig
import com.punedelights.user.helper.Constant
import com.punedelights.user.helper.Session
import com.punedelights.user.helper.VolleyCallback

@SuppressLint("CustomSplashScreen")
class SplashActivity : Activity() {
    lateinit var session: Session
    lateinit var activity: Activity
    private val SPLASH_TIME_OUT = 500
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activity = this@SplashActivity
        session = Session(activity)
        session.setBoolean("update_skip", false)
        val window = this.window
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
        window.statusBarColor = ContextCompat.getColor(activity, R.color.colorPrimary)
        setContentView(R.layout.activity_splash)
        askNotificationPermission()
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        FirebaseMessaging.getInstance().token.addOnSuccessListener { token: String ->
            session.setData(Constant.FCM_ID, token)
            registerFcm(token)
        }


    }

    fun proceedFurther() {
        val data = this.intent.data
        if (data != null && data.isHierarchical) {
            when (data.path?.split("/".toRegex())?.toTypedArray()?.get(
                data.path!!.split("/".toRegex())
                    .toTypedArray().size - 2
            )) {
                "product" -> {
                    val intent = Intent(this@SplashActivity, activity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    intent.putExtra(
                        Constant.ID,
                        data.path?.split("/".toRegex())?.toTypedArray()?.get(
                            data.path?.split("/".toRegex())!!
                                .toTypedArray().size - 1
                        )
                    )
                    intent.putExtra(Constant.FROM, "share")
                    intent.putExtra(Constant.VARIANT_POSITION, 0)
                    startActivity(intent)
                    finish()
                }

                "refer" -> if (!session.getBoolean(Constant.IS_USER_LOGIN)) {
                    Constant.FRIEND_CODE_VALUE = data.path!!.split("/".toRegex())
                        .toTypedArray()[data.path!!.split("/".toRegex()).toTypedArray().size - 1]
                    val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
                    val clip = ClipData.newPlainText("label", Constant.FRIEND_CODE_VALUE)
                    assert(clipboard != null)
                    clipboard.setPrimaryClip(clip)
                    Toast.makeText(
                        this@SplashActivity,
                        R.string.refer_code_copied,
                        Toast.LENGTH_LONG
                    ).show()
                    val referIntent = Intent(this, LoginActivity::class.java)
                    referIntent.putExtra(Constant.FROM, "refer")
                    startActivity(referIntent)
                    finish()
                } else {
                    Handler().postDelayed({
                        startActivity(
                            Intent(this@SplashActivity, MainActivity::class.java).putExtra(
                                Constant.FROM, ""
                            ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        )
                    }, SPLASH_TIME_OUT.toLong())
                    Toast.makeText(
                        activity,
                        activity.getString(R.string.msg_refer),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                else -> Handler().postDelayed({
                    startActivity(
                        Intent(this@SplashActivity, MainActivity::class.java).putExtra(
                            Constant.FROM, ""
                        ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }, SPLASH_TIME_OUT.toLong())
            }
        } else {
            if (!session.getBoolean("is_first_time")) {
                Handler().postDelayed({
                    startActivity(
                        Intent(
                            this@SplashActivity,
                            WelcomeActivity::class.java
                        ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }, SPLASH_TIME_OUT.toLong())
            } else {
                Handler().postDelayed({
                    startActivity(
                        Intent(this@SplashActivity, MainActivity::class.java).putExtra(
                            Constant.FROM, ""
                        ).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    )
                }, SPLASH_TIME_OUT.toLong())
            }
        }
    }


    private fun registerFcm(token: String) {
        val params: MutableMap<String, String> = HashMap()
        if (session.getBoolean(Constant.IS_USER_LOGIN)) {
            params[Constant.USER_ID] = session.getData(Constant.USER_ID)
        }

        params[Constant.FCM_ID] = token
        ApiConfig.requestToVolley(object : VolleyCallback {
            override fun onSuccess(result: Boolean, response: String) {
                if (result) {
                    try {
                        val jsonObject = JSONObject(response)
                        if (!jsonObject.getBoolean(Constant.ERROR)) {
                            session.setData(Constant.FCM_ID, token)
                        }
                    } catch (e: JSONException) {
                        e.printStackTrace()
                    }
                    proceedFurther()
                }
            }
        }, activity, Constant.REGISTER_DEVICE_URL, params, false)
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.POST_NOTIFICATIONS
                ) ==
                PackageManager.PERMISSION_GRANTED
            ) {

                FirebaseMessaging.getInstance().token.addOnSuccessListener { token: String ->
                    session.setData(Constant.FCM_ID, token)

                    registerFcm(token)
                }
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {

                Toast.makeText(
                    activity,
                    "You reject the notification, you need to allow notification permission if you want to get the order updated and new offers notifications!",
                    Toast.LENGTH_LONG
                ).show()
                proceedFurther()
            } else {
                // Directly ask for the permission
                requestPermissions(
                    arrayOf(
                        android.Manifest.permission.POST_NOTIFICATIONS
                    ), 110
                )
            }
        } else {
            proceedFurther()
        }
    }
}