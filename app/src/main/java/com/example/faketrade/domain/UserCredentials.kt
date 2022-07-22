package com.example.faketrade.domain

import android.text.TextUtils
import java.util.regex.Pattern

class UserCredentials(
    private val email: String,
    private val password: String?=null,
    private val confirmPassword: String? = null,
    private val userName: String? = null
) {

    private fun isEmailValid(email: String): Boolean {
        return !TextUtils.isEmpty(email) && android.util.Patterns.EMAIL_ADDRESS.matcher(email)
            .matches()
    }

    private fun isPasswordValid(password: String): Boolean {
        val pattern =
            Pattern.compile("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=])(?=\\S+$).{4,}$")
        val matcher = pattern.matcher(password)

        return password.length >= 8 && matcher.matches()
    }

    private fun checkPasswordMatches(password: String, passwordCheck: String): Boolean {
        return password == passwordCheck
    }

    private fun isUserNameValid(userName: String): Boolean {
        return userName.length >= 2
    }
    fun checkEmail():Boolean{
        return isEmailValid(email)
    }

    fun checkLoginCredentials(): Map<String, Boolean> {
        return mapOf<String, Boolean>(
            "email" to isEmailValid(email),
            "password" to isPasswordValid(password!!)
        )
    }

    fun checkCreateAccountCredentials(): Map<String, Boolean> {
        return mapOf(
            "email" to isEmailValid(email),
            "password" to isPasswordValid(password!!),
            "confirmPassword" to checkPasswordMatches(password!!, confirmPassword!!),
            "userName" to isUserNameValid(userName!!)
        )

    }
}