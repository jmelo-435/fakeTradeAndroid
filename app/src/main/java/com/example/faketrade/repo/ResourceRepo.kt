package com.example.faketrade.repo

import android.content.Context
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import kotlin.coroutines.coroutineContext

class ResourceRepo() {

    private val root = "http://192.168.15.7:5000"
    private val apiKey = "_zUYQ83k!x34%nh("
    private var client = OkHttpClient().newBuilder().build()


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
    public fun rebuildRequest(
        request: Request,
        listener: CustomListener,
        newTokens: Map<String, String?>
    ){
        val newRequest = request.newBuilder().header("x-access-token", newTokens["x-access-token"]!!)
            .build();

        client.newCall(newRequest).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                listener.onApiJSONResponse(JSONObject("""{"localError":504, "message":"$e"}"""))
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
        data: JSONObject? = null,

    ) {
        val mediaType = "application/json; charset=utf-8".toMediaType()

        val body = data?.toString()?.toRequestBody(mediaType)


        val builder: Headers.Builder = Headers.Builder()
        headersParams.forEach {
            builder.add(it.key, it.value ?: "null")
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