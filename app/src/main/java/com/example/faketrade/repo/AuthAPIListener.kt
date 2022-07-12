package com.example.faketrade.repo

import okhttp3.Request
import org.json.JSONObject

interface AuthAPIListener {
    fun onAuthApiJSONResponse(response: JSONObject)
    fun onRefreshTokenExpiredResponse(request: Request?)
}