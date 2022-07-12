package com.example.faketrade.repo

import okhttp3.Request
import org.json.JSONObject

interface ResourceAPIListener {
    fun onResourceApiJSONResponse(response: JSONObject)
    fun onAccessTokenExpiredResponse(request: Request?)
}