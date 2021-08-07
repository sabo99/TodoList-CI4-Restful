package com.sabo.todolist_ci4_restful.Activity.Profile

import android.content.res.ColorStateList
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.sabo.todolist_ci4_restful.Helper.Callback.EventOnRefresh
import com.sabo.todolist_ci4_restful.Helper.Callback.KeyStore
import com.sabo.todolist_ci4_restful.Helper.Callback.ManagerCallback
import com.sabo.todolist_ci4_restful.Helper.SharedPreference.ManagerPreferences
import com.sabo.todolist_ci4_restful.Model.User
import com.sabo.todolist_ci4_restful.R
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIResponse
import com.sabo.todolist_ci4_restful.Restful_API.RestfulAPIService
import com.sabo.todolist_ci4_restful.databinding.ActivityProfileBinding
import com.squareup.picasso.Picasso
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class Profile : AppCompatActivity() {

    private lateinit var binding: ActivityProfileBinding
    private lateinit var user: User

    override fun onResume() {
        super.onResume()
        ManagerCallback.checkSelfMACAddressAuthentication(this)
    }

    override fun onRestart() {
        super.onRestart()
        ManagerCallback.checkSelfMACAddressAuthentication(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initViews()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) finish()
        return super.onOptionsItemSelected(item)
    }

    override fun onStart() {
        super.onStart()
        EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onRefreshProfile(event: EventOnRefresh) {
        if (event.onRefresh)
            initViews()
    }

    private fun initViews() {
        isEnabledBtn(false)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        supportActionBar!!.title = "My Account"

        binding.progressBar.visibility = View.VISIBLE

        RestfulAPIService.requestMethod().showUser(ManagerPreferences.getUID(this))
            .enqueue(object : Callback<RestfulAPIResponse> {
                override fun onResponse(
                    call: Call<RestfulAPIResponse>,
                    response: Response<RestfulAPIResponse>
                ) {
                    when(response.code()){
                        200 -> {
                            user = response.body()!!.user
                            Picasso.get().load(user.avatar?.let { ManagerCallback.getURLAvatar(it) })
                                .placeholder(
                                    R.drawable.ic_round_person_black
                                ).into(binding.civAvatar)

                            binding.tvProfileUsername.text =
                                StringBuilder().append(user.username).append("\n")
                                    .append(ManagerCallback.onHashNumber(user.uid)).toString()
                            binding.tvUsername.text = StringBuilder().append(user.username)
                                .append(ManagerCallback.onHashNumber(user.uid)).toString()
                            binding.tvEmail.text = user.email

                            if (user.two_factor_auth == 0) {
                                binding.btnTwoFactorAuth.text = "Enable Two-Factor Auth"
                                binding.btnTwoFactorAuth.backgroundTintList = ColorStateList.valueOf(
                                    resources.getColor(
                                        R.color.royal_blue,
                                        theme
                                    )
                                )
                            } else {
                                binding.btnTwoFactorAuth.text = "Disable Two-Factor Auth"
                                binding.btnTwoFactorAuth.backgroundTintList =
                                    ColorStateList.valueOf(resources.getColor(R.color.red, theme))
                            }

                            isEnabledBtn(true)
                        }
                        404 -> isEnabledBtn(false)

                    }

                    binding.progressBar.visibility = View.GONE
                    ManagerCallback.onLog("Profile", response)
                }

                override fun onFailure(call: Call<RestfulAPIResponse>, t: Throwable) {
                    binding.progressBar.visibility = View.GONE
                    isEnabledBtn(false)
                    ManagerCallback.onLog("Profile", "${t.message}")
                }
            })

        binding.btnEditProfile.setOnClickListener {
            ProfileCallback.onEdited(this, user, KeyStore.KEY_PROFILE)
        }

        binding.btnEditUsername.setOnClickListener {
            ProfileCallback.onEdited(this, user, KeyStore.KEY_USERNAME)
        }

        binding.btnEditEmail.setOnClickListener {
            ProfileCallback.onEdited(this, user, KeyStore.KEY_EMAIL)
        }

        binding.btnChangePassword.setOnClickListener {
            ProfileCallback.onEdited(this, user, KeyStore.KEY_PASSWORD)
        }

        binding.btnTwoFactorAuth.setOnClickListener {
            ProfileCallback.onEdited(this, user, KeyStore.KEY_TWO_FACTOR_AUTH)
        }

        binding.btnDeleteAccount.setOnClickListener {
            ProfileCallback.onDeleteAccount(this)
        }

        binding.btnLogout.setOnClickListener {
            ProfileCallback.onLogout(this)
        }
    }

    private fun isEnabledBtn(isEnabled: Boolean) {
        binding.btnEditProfile.isEnabled = isEnabled
        binding.btnEditUsername.isEnabled = isEnabled
        binding.btnEditEmail.isEnabled = isEnabled
        binding.btnChangePassword.isEnabled = isEnabled
        binding.btnTwoFactorAuth.isEnabled = isEnabled
        binding.btnDeleteAccount.isEnabled = isEnabled
        binding.btnLogout.isEnabled = isEnabled
    }

}

