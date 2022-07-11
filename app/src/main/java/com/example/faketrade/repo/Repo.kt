package com.example.faketrade.repo

import android.content.Context
import kotlinx.coroutines.runBlocking
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody

import java.io.IOException
import org.json.JSONObject


public class Repo() {
    private val client = OkHttpClient()
    private val root = "http://192.168.15.7:5001"
    private val apiKey = "_zUYQ83k!x34%nh("

//    class Authenticator() : okhttp3.Authenticator {
//
//
//        private suspend fun saveTokensAndBuildNewRequest(
//            rejectedCallresponse: Response
//        ): Request {
//            var currentTokens: Map<String, String> = mapOf(
//                "x-access-token" to tokenRepo.retreaveToken(TokenType.ACCESS),
//                "x-refresh-token" to tokenRepo.retreaveToken(TokenType.BEARER)
//            )
//            lateinit var newTokens: Map<String, String>
//
//
//            Repo(context).buildRequest(
//                url = AuthEndpoints.ApiUserRefreshToken,
//                headersParams = currentTokens,
//                listener = object : CustomListener {
//                    override fun onApiJSONResponse(response: JSONObject) {
//                        if (response.has("accessData") && response.has("bearerData")) {
//
//
//                            val access = response.get("accessData").toString()
//                            val bearer = response.get("bearerData").toString()
//                            tokenRepo.saveToken(access, TokenType.ACCESS)
//                            tokenRepo.saveToken(bearer, TokenType.BEARER)
//
//                            newTokens = mapOf(
//                                "x-access-token" to access,
//                                "x-refresh-token" to bearer
//                            )
//
//
//                        }
//
//
//                    }
//
//
//                })
//            return rejectedCallresponse.request.newBuilder()
//                .header("x-access-token", newTokens["x-access-token"]!!)
//                .header("x-refresh-token", newTokens["x-refresh-token"]!!).build();
//
//        }
//
//
//
//    }

    public fun testConnection(listener: CustomListener, url: String, apiKey: String) {


        val request = Request.Builder()
            .url(url)
            .addHeader("api-key", apiKey)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onApiJSONResponse(JSONObject("""{"msg":"${e.toString()}"}"""))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    listener.onApiJSONResponse(JSONObject(response.body?.string()))


                }
            }
        })


    }


    public fun buildRequest(
        method: String = "GET",
        listener: CustomListener,
        url: String,
        headersParams: Map<String, String?>,
        data: JSONObject? = null
    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = data?.toString()?.toRequestBody(mediaType)

        val builder: Headers.Builder = Headers.Builder()
        headersParams.forEach {
            builder.add(it.key, it.value?: "")
        }
        builder.add("api-key", apiKey)
        val h = builder.build()
        val address = root + url
        val request = Request.Builder()
            .url(address)
            .headers(h)
            .method(method, body = body)
            .build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onApiJSONResponse(JSONObject("""{"localError":504, "message":"$e"}"""))
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        if ( response.code==401){
                            println("code 401")
                            listener.onJsonExpiredResponse(request)
                        }
                        else{
                            throw IOException("Unexpected code $response")
                        }

                    }
                    else{
                        listener.onApiJSONResponse(JSONObject(response.body?.string()))
                    }





                }
            }
        })

    }

}