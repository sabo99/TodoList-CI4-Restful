package com.sabo.todolist_ci4_restful.Helper.SharedPreference

import android.content.Context

class ManagerPreferences {

    companion object {
        private const val PREF_NAME = "PREF_NAME";
        private const val IS_LOGGED_IN_KEY = "IS_LOGGED_IN_KEY";
        private const val UID_KEY = "UID";

        /**
         * UserManager
         */
        fun setIsLoggedIn(context: Context, isLoggedIn: Boolean) {
            context.getSharedPreferences(PREF_NAME, 0).edit()
                .putBoolean(IS_LOGGED_IN_KEY, isLoggedIn).apply()
        }

        fun getIsLoggedIn(context: Context): Boolean {
            return context.getSharedPreferences(PREF_NAME, 0).getBoolean(IS_LOGGED_IN_KEY, false)
        }

        fun setUID(context: Context, UID: Int){
            context.getSharedPreferences(PREF_NAME, 0).edit()
                .putInt(UID_KEY, UID).apply()
        }

        fun getUID(context: Context): Int{
            return context.getSharedPreferences(PREF_NAME, 0).getInt(UID_KEY, 0)
        }


        fun clearUserPreferences(context: Context) {
            context.getSharedPreferences(PREF_NAME, 0).edit()
//                .remove(IS_LOGGED_IN_KEY)
//                .remove(UID_KEY)
//                .apply()
                .clear()
                .apply()
        }
    }
}