package com.example.faketrade.ui.dashboard

import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.example.faketrade.repo.*
import com.example.faketrade.repo.ApiRequestParameters
import com.example.faketrade.repo.NetworkResult
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import okhttp3.Request
import org.json.JSONObject
import kotlin.coroutines.coroutineContext


class MainDashboardViewModel(application: Application) : AndroidViewModel(application) {


    val app = application
    private var _responseCode: MutableLiveData<NetworkResult<Int>> = MutableLiveData()
    var responseCode: LiveData<NetworkResult<Int>> = _responseCode


    private var _authTokens: MutableLiveData<Map<TokenType, String>> = MutableLiveData()
    var authTokens: LiveData<Map<TokenType, String>> = _authTokens

    private var _saldo: MutableLiveData<NetworkResult<Int>> = MutableLiveData()
    var saldo: LiveData<NetworkResult<Int>> = _saldo

    private var _isValidToken: MutableLiveData<NetworkResult<Boolean>> = MutableLiveData()
    var isValidToken: LiveData<NetworkResult<Boolean>> = _isValidToken

    private var tokenRepo = TokensRepo(app)

    private val repo = ResourceRepo()
    private val authRepo = Repo()
    fun getSaldo() {
        viewModelScope.launch {
            getUserSaldo()
        }

    }

    fun checkIfTokenIsValid() {
        _isValidToken.value = NetworkResult.Loading()
        var currentToken: Map<String, String?> = mapOf(
            TokenType.BEARER.value to tokenRepo.retreaveToken(TokenType.BEARER)
        )
        try {
            viewModelScope.launch {
                authRepo.buildRequest(
                    url = Endpoints.AuthEndpoints.ApiUserRefreshToken.value,
                    headersParams = currentToken,
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

                        override fun onJsonExpiredResponse(request: Request) {

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
                url = Endpoints.AuthEndpoints.ApiUserRefreshToken.value,
                headersParams = currentToken,
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
                            repo.rebuildRequest(request, listener = object : CustomListener {
                                override fun onApiJSONResponse(response: JSONObject) {
                                    handleResponseBody(response)
                                }

                                override fun onJsonExpiredResponse(request: Request) {
                                    TODO("Not yet implemented")
                                }

                            }, newToken)

                        }


                    }

                    override fun onJsonExpiredResponse(request: Request) {

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

        if (response.has("accessData") && response.has("bearerData")) {


            val access = response.get("accessData").toString()
            val bearer = response.get("bearerData").toString()


            val tokens =
                mapOf<TokenType, String>(TokenType.ACCESS to access, TokenType.BEARER to bearer)
            _authTokens.postValue(tokens)
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


    private suspend fun getLoginResponse(parameters: ApiRequestParameters) {
        _saldo.value = NetworkResult.Loading()

        parameters.scope.launch {
            try {
                repo.buildRequest(
                    method = parameters.method.value,
                    listener = object : CustomListener {
                        override fun onApiJSONResponse(response: JSONObject) {
                            handleResponseBody(response)
                        }

                        override fun onJsonExpiredResponse(request: Request) {

                            refreshTokenAndRebuildRequest(request)


                        }

                    },
                    url = parameters.endpoint,
                    headersParams = parameters.headersMap,
                    data = parameters.data
                )
            } catch (e: Exception) {
                _saldo.value = NetworkResult.Error(data = 100, message = e.message)

            }
        }
    }


    fun getUserSaldo() {

        viewModelScope.launch {
            val parameters = ApiRequestParameters(scope = this)
            var newTokens: Map<String, String?> = mapOf(
                TokenType.ACCESS.value to tokenRepo.retreaveToken(TokenType.ACCESS)

            )
            parameters.headersMap = newTokens
            parameters.scope = viewModelScope
            parameters.endpoint = Endpoints.UserEndpoints.UserSaldo.value
            parameters.method = com.example.faketrade.repo.Methods.GET
            getLoginResponse(parameters)

        }
    }

    fun isAuthorizedCheck(context: Context, times: Int): Flow<Boolean> {

        return flow {
            repeat(times) {
                var test = !TokensRepo(context).retreaveToken(TokenType.ACCESS).isNullOrEmpty() &&
                        !TokensRepo(context).retreaveToken(TokenType.BEARER).isNullOrEmpty()
                emit(test)
                if (test) {
                    coroutineContext.job.cancel()
                }
                delay(500)
            }
        }
    }


}
