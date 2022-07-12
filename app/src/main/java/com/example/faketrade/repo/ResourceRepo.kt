package com.example.faketrade.repo

import kotlinx.coroutines.CoroutineScope
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class ResourceRepo{

    private val root = "http://192.168.15.7:5000"
    private val apiKey = "_zUYQ83k!x34%nh("
    private var client = OkHttpClient()
    enum class ResourceEndpoints (val value: String){
        UserSaldo("/api/user/saldo")
    }


    fun rebuildRequest(
        request: Request,
        listener: CustomListener,
        newTokens: Map<String, String?>
    ){
        val newRequest = request.newBuilder().header(TokenType.ACCESS.value, newTokens[TokenType.ACCESS.value]!!)
            .build()
        client.newCall(newRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onApiJSONResponse(JSONObject("""{"localError":504, "message":"$e"}"""))
            }
            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) throw IOException("Unexpected code $response")

                    listener.onApiJSONResponse(JSONObject(response.body?.string()!!))

                }
            }
        })
    }

    fun buildRequest(
        listener: CustomListener,
        endpoint: ResourceEndpoints = ResourceEndpoints.UserSaldo,
        headersMap: Map<String,String?> = mapOf(),
        data: JSONObject? = null,
        method: Methods = Methods.GET,

    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val body = data?.toString()?.toRequestBody(mediaType)
        val builder: Headers.Builder = Headers.Builder()
        headersMap.forEach {
            builder.add(it.key, it.value ?: "null")
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
                      if ( response.code==401){
                          println("code 401")
                          listener.onTokenExpiredResponse(request)
                      }
                      else{
                          throw IOException("Unexpected code $response")
                      }
                  }
                    else{
                      listener.onApiJSONResponse(JSONObject(response.body?.string()?:"{}"))
                    }
                }
            }
        })

    }
}