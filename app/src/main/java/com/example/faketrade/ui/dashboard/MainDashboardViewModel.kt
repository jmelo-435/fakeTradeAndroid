package com.example.faketrade.ui.dashboard

import android.app.Application
import androidx.lifecycle.*
import com.example.faketrade.repo.*
import com.example.faketrade.repo.ApiRequestParameters
import com.example.faketrade.repo.NetworkResult
import kotlinx.coroutines.launch
import okhttp3.Request
import org.json.JSONObject


class MainDashboardViewModel(application: Application) : AndroidViewModel(application) {


    val app = application
    private var _responseCode: MutableLiveData<NetworkResult<Int>> = MutableLiveData()
    var responseCode: LiveData<NetworkResult<Int>> = _responseCode

    private var _saldo: MutableLiveData<NetworkResult<Int>> = MutableLiveData()
    var saldo: LiveData<NetworkResult<Int>> = _saldo

    private var _isValidToken: MutableLiveData<NetworkResult<Boolean>> = MutableLiveData()
    var isValidToken: LiveData<NetworkResult<Boolean>> = _isValidToken

    private var tokenRepo = TokensRepo(app)

    private val resourceRepo = ResourceRepo()
    private val authRepo = AuthRepo()

    fun checkIfTokenIsValid() {
        _isValidToken.value = NetworkResult.Loading()
        var currentToken: Map<String, String?> = mapOf(
            TokenType.BEARER.value to tokenRepo.retreaveToken(TokenType.BEARER)
        )
        try {
            viewModelScope.launch {
                authRepo.buildRequest(
                    endpoint = AuthRepo.AuthEndpoints.ApiUserRefreshToken,
                    headersMap = currentToken,
                    listener = object : CustomListener {
                        override fun onApiJSONResponse(response: JSONObject) {
                            _isValidToken.postValue(NetworkResult.Success(true))
                            if (response.has("accessData") && response.has("bearerData")) {


                                val access = response.get("accessData").toString()
                                val bearer = response.get("bearerData").toString()
                                tokenRepo.saveToken(access, TokenType.ACCESS)
                                tokenRepo.saveToken(bearer, TokenType.BEARER)
                            }
                        }

                        override fun onTokenExpiredResponse(request: Request) {

                            _isValidToken.postValue(NetworkResult.Success(false))

                        }
                    }
                )
            }
        } catch (e: Exception) {

            _isValidToken.value = NetworkResult.Error(data = false, message = e.message)

        }


    }

    private fun refreshTokenAndRebuildRequest(request: Request) {
        var currentToken: Map<String, String?> = mapOf(
            TokenType.BEARER.value to tokenRepo.retreaveToken(TokenType.BEARER)
        )


        viewModelScope.launch {
            authRepo.buildRequest(
                endpoint = AuthRepo.AuthEndpoints.ApiUserRefreshToken,
                headersMap = currentToken,
                listener = object : CustomListener {
                    override fun onApiJSONResponse(response: JSONObject) {
                        if (response.has("accessData") && response.has("bearerData")) {

                            val access = response.get("accessData").toString()
                            val bearer = response.get("bearerData").toString()
                            tokenRepo.saveToken(access, TokenType.ACCESS)
                            tokenRepo.saveToken(bearer, TokenType.BEARER)
                            var newToken: Map<String, String?> = mapOf(
                                TokenType.ACCESS.value to access
                            )
                            resourceRepo.rebuildRequest(request, listener = object : CustomListener {
                                override fun onApiJSONResponse(response: JSONObject) {
                                    handleResponseBody(response)
                                }

                                override fun onTokenExpiredResponse(request: Request) {
                                    TODO("Not yet implemented")
                                }

                            }, newToken)

                        }


                    }

                    override fun onTokenExpiredResponse(request: Request) {

                        _isValidToken.postValue(NetworkResult.Success(false))
                    }


                }
            )


        }


    }

    private fun handleResponseBody(response: JSONObject) {
        if (response.has("code")) {
            _responseCode.postValue(NetworkResult.Success(response.get("code") as Int))
        }

        if (response.has("saldo")) {
            _saldo.postValue(NetworkResult.Success(response.get("saldo") as Int))

        }
        if (response.has("localError")) {
            _responseCode.postValue(
                NetworkResult.Error(
                    data = 504,
                    message = response.get("message").toString()
                )
            )
        }

    }

     fun getUserSaldo() {

        viewModelScope.launch {
            val newTokens: Map<String, String?> = mapOf(TokenType.ACCESS.value to tokenRepo.retreaveToken(TokenType.ACCESS))
            _saldo.value = NetworkResult.Loading()
                try {
                    resourceRepo.buildRequest(
                        method = Methods.GET,
                        listener = object : CustomListener {
                            override fun onApiJSONResponse(response: JSONObject) {
                                handleResponseBody(response)
                            }

                            override fun onTokenExpiredResponse(request: Request) {

                                refreshTokenAndRebuildRequest(request)

                            }

                        },
                        endpoint = ResourceRepo.ResourceEndpoints.UserSaldo,
                        headersMap = newTokens,

                    )
                } catch (e: Exception) {
                    _saldo.value = NetworkResult.Error(data = 100, message = e.message)
                }

        }
    }



}
