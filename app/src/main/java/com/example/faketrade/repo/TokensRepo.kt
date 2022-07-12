package com.example.faketrade.repo


import android.content.Context
import android.content.SharedPreferences
import android.provider.Settings.Global.getString
import com.example.faketrade.R


enum class TokenType(val value: String) {

    ACCESS("x-access-token"), BEARER("x-refresh-token")

}


class TokensRepo(context: Context) {
    private val sharedPref: SharedPreferences? = context.getSharedPreferences(
        context.getString(R.string.token_file), Context.MODE_PRIVATE
    )

    fun saveToken(token: String, type: TokenType) {

        with(sharedPref?.edit() ?: return) {
            putString(type.value, token)
            commit()

        }

    }

    fun retreaveToken(type: TokenType): String? {

        return sharedPref?.getString(type.value, null)

    }

}