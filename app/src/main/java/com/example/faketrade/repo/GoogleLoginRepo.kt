package com.example.faketrade.repo

import android.content.Context
import android.content.Intent
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException


class GoogleLoginRepo(context: Context, private val listener: GoogleSigninListener? = null) {


    private val clientId = "802585832012-jgt4h595tr7464i4d5gf5p2q3ghks60j.apps.googleusercontent.com"
    private val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
        .requestIdToken(clientId)
        .requestEmail()
        .build()
    private val mGoogleSignInClient = GoogleSignIn.getClient(context, gso)
    val signInIntent = mGoogleSignInClient.signInIntent

    fun handleSingnInResult(data : Intent?){
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)

        try {
            val account = task.getResult(ApiException::class.java)
            account.idToken?.let { listener?.onGooglePositiveResponse(it) }


        } catch (e: ApiException) {

            listener?.onGoogleErrorResponse(e)

        }

    }



}