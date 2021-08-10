package com.sabo.todolist_ci4_restful.Helper.Callback

object KeyStore {
    /** Was Wrong ! */
    const val CURRENT_PASSWORD_WRONG = "Your current password was wrong."
    const val VERIFICATION_CODE_WRONG = "Your verification code is wrong."
    const val RESEND_CODE = "Resend verification code."

    /** Apache Shutdown */
    const val ON_FAILURE = "Something wrong with server connection"

    /** Request Permission Code */
    const val MULTIPLE_PERMISSION = 333

    /** CountDown Timer */
    const val DELAY: Long = 120000
    const val INTERVAL: Long = 1000

    /** ProfileCallback */
    const val contentLoadingEmail = "Update Email"
    const val contentLoadingUsername = "Update Username"
    const val contentLoadingPassword = "Update Password"
    const val contentLoadingTwoFactorAuthEnable = "Enable Two-Factor Authentication"
    const val contentLoadingTwoFactorAuthDisable = "Disable Two-Factor Authentication"

    const val contentSuccessEmail = "Email successfully updated"
    const val contentSuccessUsername = "Username successfully updated"
    const val contentSuccessPassword = "Password successfully updated"
    const val contentSuccessTwoFactorAuthEnable = "Two-Factor Authentication 'Enabled'"
    const val contentSuccessTwoFactorAuthDisable =
        "Two-Factor Authentication 'Disabled'"

    const val KEY_PROFILE = 0
    const val KEY_USERNAME = 1
    const val KEY_EMAIL = 2
    const val KEY_PASSWORD = 3
    const val KEY_TWO_FACTOR_AUTH = 4

    /**
     * Log User
     */
    const val SIGN_UP = "Sign Up"
    const val LOG_IN = "Log In"
    const val EDIT_PROFILE = "Edit Profile"
    const val UPDATE_PROFILE = "Update Avatar Profile"
    const val EDIT_USERNAME = "Edit Username"
    const val UPDATE_USERNAME = "Update Username"
    const val EDIT_EMAIL = "Edit Email"
    const val UPDATE_EMAIL = "Update Email"
    const val EDIT_PASSWORD = "Edit Password"
    const val UPDATE_PASSWORD = "Update Password"
    const val ENABLE_TWO_FACTOR_AUTH = "Enable Two Factor Authentication"
    const val DISABLE_TWO_FACTOR_AUTH = "Disable Two Factor Authentication"
    const val DELETE_ACCOUNT = "Delete Account"
    const val LOG_OUT = "Log Out"

    const val CREATE_TODO = "Create Todo"
    const val EDIT_TODO = "Edit Todo"
    const val UPDATE_TODO = "Update Todo"
    const val DELETE_TODO = "Delete Todo"
    const val DELETE_ALL_TODO = "Delete All TodoList"
}