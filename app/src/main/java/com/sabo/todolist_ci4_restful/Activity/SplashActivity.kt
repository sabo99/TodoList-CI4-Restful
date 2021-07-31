package com.sabo.todolist_ci4_restful.Activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.WindowManager
import android.view.animation.AnimationUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import com.sabo.todolist_ci4_restful.Activity.Auth.Login
import com.sabo.todolist_ci4_restful.Helper.SharedPreference.ManagerPreferences
import com.sabo.todolist_ci4_restful.R

class SplashActivity : AppCompatActivity() {

    private lateinit var ivLogo: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        ivLogo = findViewById(R.id.ivLogo)
        ivLogo.startAnimation(AnimationUtils.loadAnimation(this, R.anim.modal_in))

        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )
        Handler().postDelayed({

            if (ManagerPreferences.getIsLoggedIn(this))
                startActivity(Intent(this, MainActivity::class.java))
            else
                startActivity(Intent(this, Login::class.java))

            finish()

        }, 1000)
    }
}