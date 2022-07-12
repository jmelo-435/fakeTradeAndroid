package com.example.faketrade.repo

import com.google.android.gms.common.api.ApiException
import okhttp3.Request

interface GoogleSigninListener {
    fun onGooglePositiveResponse(token: String)
    fun onGoogleErrorResponse(e: ApiException)
}