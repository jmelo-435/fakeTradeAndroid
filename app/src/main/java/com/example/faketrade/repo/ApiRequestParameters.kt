package com.example.faketrade.repo


import kotlinx.coroutines.CoroutineScope
import org.json.JSONObject

class ApiRequestParameters(
    var endpoint: AuthRepo.AuthEndpoints = AuthRepo.AuthEndpoints.Api,
    var headersMap: Map<String,String?> = mapOf(),
    var data: JSONObject? = null,
    var method: Methods = Methods.GET,
    var scope: CoroutineScope
)