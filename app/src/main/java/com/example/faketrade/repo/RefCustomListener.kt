package com.example.faketrade.repo

import okhttp3.Request
import org.json.JSONObject

interface RefCustomListener {
    fun onApiJSONResponse(response: JSONObject)
    fun onTokenExpiredResponse(request: Request?)
}