package com.example.faketrade.repo

import kotlinx.coroutines.CoroutineScope
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

import java.io.IOException
import org.json.JSONObject


class AuthRepo{
    private val client = OkHttpClient()
    private val root = "http://192.168.15.7:5001"
    private val apiKey = "_zUYQ83k!x34%nh("

    enum class AuthEndpoints(val value: String) {
        Api("/api"), ApiUsers("/api/users"),ApiUserPassword("/api/user/password"),
        ApiUserPasswordReset("/api/user/password_reset"),ApiUserRefreshToken("/api/user/refresh_token"),
        ApiUserLogout("/api/user/logout"),ApiUsersGoogleToken("/api/users/google_token")
    }

    fun buildRequest(
        endpoint: AuthEndpoints = AuthEndpoints.Api,
        headersMap: Map<String,String?> = mapOf(),
        data: JSONObject? = null,
         method: Methods = Methods.GET,
        listener: CustomListener
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = data?.toString()?.toRequestBody(mediaType)
        val builder: Headers.Builder = Headers.Builder()
        headersMap.forEach {
            builder.add(it.key, it.value ?: "")
        }
        builder.add("api-key", apiKey)
        val h = builder.build()
        val address = root + endpoint.value
        val request = Request.Builder()
            .url(address)
            .headers(h)
            .method(method.value, body = body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onApiJSONResponse(JSONObject("""{"localError":504, "message":"$e"}"""))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        if (response.code == 401) {
                            println("code 401")
                            listener.onTokenExpiredResponse(request)
                        } else {
                            throw IOException("Unexpected code $response")
                        }
                    } else {
                        listener.onApiJSONResponse(JSONObject(response.body?.string()?:"{}"))
                    }
                }
            }
        })

    }

}