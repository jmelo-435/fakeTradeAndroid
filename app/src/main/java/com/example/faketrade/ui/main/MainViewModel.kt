package com.example.faketrade.ui.main


import android.app.Application
import android.content.Context
import android.content.Intent
import androidx.lifecycle.*
import com.example.faketrade.repo.*
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.common.api.ApiException
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
    private val authRepo = AuthRepo()
    private val tokenRepo = TokensRepo(app)
    private val googleLoginRepo = GoogleLoginRepo(app, listener = object :GoogleSigninListener{
        override fun onGooglePositiveResponse(token: String) {
            loginGoogleUser(token)
        }

        override fun onGoogleErrorResponse(e: ApiException) {
            _googleError.postValue(NetworkResult.Success(e.message!!))
        }

    })


    private var _googleError: MutableLiveData<NetworkResult<String>> = MutableLiveData()
    var googleError: LiveData<NetworkResult<String>> = _googleError

    private var _responseCode: MutableLiveData<NetworkResult<Int>> = MutableLiveData()
    var responseCode: LiveData<NetworkResult<Int>> = _responseCode

    private var _isValidToken: MutableLiveData<NetworkResult<Boolean>> = MutableLiveData()
    var isValidToken: LiveData<NetworkResult<Boolean>> = _isValidToken




    fun handleSingnInResult(data :Intent?){

        googleLoginRepo.handleSingnInResult(data)

    }



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

                            if (response.has("accessData") && response.has("bearerData")) {


                                val access = response.get("accessData").toString()
                                val bearer = response.get("bearerData").toString()
                                tokenRepo.saveToken(access, TokenType.ACCESS)
                                tokenRepo.saveToken(bearer, TokenType.BEARER)
                                _isValidToken.postValue(NetworkResult.Success(true))
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

    private fun getLoginResponse(parameters: ApiRequestParameters) {
        _isValidToken.value = NetworkResult.Loading()

        parameters.scope.launch {
            try {
                authRepo.buildRequest(
                    method = parameters.method,
                    listener = object : CustomListener {
                        override fun onApiJSONResponse(response: JSONObject) {
                            if (response.has("code")) {
                                _responseCode.postValue(NetworkResult.Success(response.get("code") as Int))
                            }

                            if (response.has("accessData") && response.has("bearerData")) {


                                val access = response.get("accessData").toString()
                                val bearer = response.get("bearerData").toString()

                                tokenRepo.saveToken(access, TokenType.ACCESS)
                                tokenRepo.saveToken(bearer, TokenType.BEARER)
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

                        override fun onTokenExpiredResponse(request: Request) {
                            _isValidToken.postValue(NetworkResult.Success(false))
                        }
                    },
                    endpoint = parameters.endpoint,
                    headersMap = parameters.headersMap,
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
            parameters.endpoint = AuthRepo.AuthEndpoints.ApiUsers
            parameters.method = Methods.PUT
            getLoginResponse(parameters)
        }
    }

    fun loginUser(user: JSONObject) {

        viewModelScope.launch {
            val parameters = ApiRequestParameters(scope = this)
            parameters.scope = viewModelScope
            parameters.data = user
            parameters.endpoint = AuthRepo.AuthEndpoints.ApiUsers
            parameters.method =Methods.POST
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
            parameters.endpoint = AuthRepo.AuthEndpoints.ApiUsersGoogleToken
            parameters.method =Methods.POST
            getLoginResponse(parameters)
        }
    }

}