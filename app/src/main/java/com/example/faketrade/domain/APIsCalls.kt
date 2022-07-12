package com.example.faketrade.domain

import android.app.Application
import androidx.lifecycle.viewModelScope
import com.example.faketrade.repo.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import okhttp3.Request
import org.json.JSONObject

class APIsCalls(app: Application) {
    private var tokenRepo = TokensRepo(app.applicationContext)
    private val resourceRepo = RefResourceRepo()
    private val authRepo = RefAuthRepo()

    class AuthApiRequestParameters(
        var endpoint: ValidEndpoints.AuthEndpoints = ValidEndpoints.AuthEndpoints.Api,
        var data: JSONObject? = null,
        var method: Methods = Methods.GET
    )

    class ResourceApiRequestParameters(
        var endpoint: ValidEndpoints.ResourceEndpoints = ValidEndpoints.ResourceEndpoints.UserSaldo,
        var data: JSONObject? = null,
        var method: Methods = Methods.GET
    )

    private fun refreshTokenAndRebuildRequest(request: Request, listener: AuthAPIListener) {
        var currentToken: Map<String, String?> = mapOf(
            TokenType.BEARER.value to tokenRepo.retreaveToken(TokenType.BEARER)
        )
        authRepo.buildRequest(
            endpoint = ValidEndpoints.AuthEndpoints.ApiUserRefreshToken,
            headersMap = currentToken,
            method = Methods.GET,
            listener = object : AuthAPIListener {
                override fun onAuthApiJSONResponse(response: JSONObject) {
                    if (response.has("accessData") && response.has("bearerData")) {

                        val access = response.get("accessData").toString()
                        val bearer = response.get("bearerData").toString()
                        tokenRepo.saveToken(access, TokenType.ACCESS)
                        tokenRepo.saveToken(bearer, TokenType.BEARER)
                        var newToken: Map<String, String?> = mapOf(
                            TokenType.ACCESS.value to access
                        )
                        resourceRepo.rebuildRequest(request, listener = listener, newToken)

                    }
                }
                override fun onRefreshTokenExpiredResponse(request: Request?) {

                    listener.onRefreshTokenExpiredResponse(null)
                }
            }
        )


    }

    fun resourceApiCall(listener: RefCustomListener, parameters: ResourceApiRequestParameters) {


        val newTokens: Map<String, String?> =
            mapOf(TokenType.ACCESS.value to tokenRepo.retreaveToken(TokenType.ACCESS))
        try {
            resourceRepo.buildRequest(
                method = parameters.method,
                listener = object : ResourceAPIListener {
                    override fun onResourceApiJSONResponse(response: JSONObject) {
                        listener.onApiJSONResponse(response)
                    }

                    override fun onAccessTokenExpiredResponse(request: Request?) {

                        refreshTokenAndRebuildRequest(request!!, object : AuthAPIListener {
                            override fun onAuthApiJSONResponse(response: JSONObject) {
                                listener.onApiJSONResponse(response)
                            }

                            override fun onRefreshTokenExpiredResponse(request: Request?) {
                                listener.onTokenExpiredResponse(null)
                            }

                        })

                    }

                },
                endpoint = parameters.endpoint,
                headersMap = newTokens
                )
        } catch (e: Exception) {
            throw(e)
        }


    }

    fun authApiCall(listener: RefCustomListener, parameters:AuthApiRequestParameters) {

        val newTokens: Map<String, String?> =
            mapOf(TokenType.BEARER.value to tokenRepo.retreaveToken(TokenType.BEARER))
        try {
            authRepo.buildRequest(
                method = parameters.method,
                listener = object  :AuthAPIListener{
                    override fun onAuthApiJSONResponse(response: JSONObject) {
                        if (response.has("accessData") && response.has("bearerData")) {
                            val access = response.get("accessData").toString()
                            val bearer = response.get("bearerData").toString()
                            tokenRepo.saveToken(access, TokenType.ACCESS)
                            tokenRepo.saveToken(bearer, TokenType.BEARER)
                        }
                        listener.onApiJSONResponse(response)
                    }

                    override fun onRefreshTokenExpiredResponse(request: Request?) {
                        listener.onTokenExpiredResponse(null)
                    }

                },
                endpoint = parameters.endpoint,
                headersMap = newTokens,
                data = parameters.data
                )
        } catch (e: Exception) {
            throw(e)
        }


    }


}
