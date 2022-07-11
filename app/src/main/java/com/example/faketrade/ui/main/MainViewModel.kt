package com.example.faketrade.ui.main


import android.app.Application
import android.content.Context
import androidx.lifecycle.*
import com.example.faketrade.repo.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.job
import kotlinx.coroutines.launch
import okhttp3.Request
import org.json.JSONObject
import kotlin.coroutines.coroutineContext


class MainViewModel(application: Application) : AndroidViewModel(application) {

    val app = application
    private val authRepo = Repo()
    private var tokenRepo = TokensRepo(app)

    private var _responseCode: MutableLiveData<NetworkResult<Int>> = MutableLiveData()
    var responseCode: LiveData<NetworkResult<Int>> = _responseCode

    private var _bearerToken: MutableLiveData<NetworkResult<String>> = MutableLiveData()
    var bearerToken: LiveData<NetworkResult<String>> = _bearerToken

    private var _accessToken: MutableLiveData<NetworkResult<String>> = MutableLiveData()
    var accessToken: LiveData<NetworkResult<String>> = _accessToken

    private var _authTokens: MutableLiveData<Map<TokenType, String>> = MutableLiveData()
    var authTokens: LiveData<Map<TokenType, String>> = _authTokens

    private var _isValidToken: MutableLiveData<NetworkResult<Boolean>> = MutableLiveData()
    var isValidToken: LiveData<NetworkResult<Boolean>> = _isValidToken

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

    private suspend fun getLoginResponse(parameters: ApiRequestParameters) {
        _isValidToken.value = NetworkResult.Loading()

        parameters.scope.launch {
            try {
                Repo().buildRequest(
                    method = parameters.method.value,
                    listener = object : CustomListener {
                        override fun onApiJSONResponse(response: JSONObject) {
                            if (response.has("code")) {
                                _responseCode.postValue(NetworkResult.Success(response.get("code") as Int))
                            }

                            if (response.has("accessData") && response.has("bearerData")) {


                                val access = response.get("accessData").toString()
                                val bearer = response.get("bearerData").toString()


                                val tokens = mapOf<TokenType, String>(
                                    TokenType.ACCESS to access,
                                    TokenType.BEARER to bearer
                                )
                                _authTokens.postValue(tokens)
                                _isValidToken.postValue(NetworkResult.Success(true))
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

                        override fun onJsonExpiredResponse(request: Request) {
                            _isValidToken.postValue(NetworkResult.Success(false))
                        }

                    },
                    url = parameters.endpoint,
                    headersParams = parameters.headersMap,
                    data = parameters.data
                )
            } catch (e: Exception) {
                _isValidToken.postValue(NetworkResult.Error(data = false))

            }
        }
    }

    fun createUser(user: JSONObject) {

        viewModelScope.launch {
            val parameters = ApiRequestParameters(scope = this)
            parameters.scope = viewModelScope
            parameters.data = user
            parameters.endpoint = Endpoints.AuthEndpoints.ApiUsers.value
            parameters.method = com.example.faketrade.repo.Methods.PUT
            getLoginResponse(parameters)
        }
    }

    fun loginUser(user: JSONObject) {

        viewModelScope.launch {
            val parameters = ApiRequestParameters(scope = this)
            parameters.scope = viewModelScope
            parameters.data = user
            parameters.endpoint = Endpoints.AuthEndpoints.ApiUsers.value
            parameters.method = com.example.faketrade.repo.Methods.POST
            getLoginResponse(parameters)
        }
    }

    fun loginGoogleUser(userToken: String) {

        viewModelScope.launch {
            val parameters = ApiRequestParameters(scope = this)
            val data = JSONObject("{}")
            data.put("googleToken", userToken)
            parameters.scope = viewModelScope
            parameters.data = data
            parameters.endpoint = Endpoints.AuthEndpoints.ApiUsersGoogleToken.value
            parameters.method = com.example.faketrade.repo.Methods.POST
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