package com.example.faketrade.repo


import kotlinx.coroutines.CoroutineScope
import org.json.JSONObject

class ApiRequestParameters(
    var endpoint:String = Endpoints.AuthEndpoints.Api.value,
    var headersMap: Map<String,String?> = mapOf(),
    var data: JSONObject? = null,
    var method: Methods = Methods.GET,
    var scope: CoroutineScope
)