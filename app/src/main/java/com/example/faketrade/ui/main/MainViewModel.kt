package com.example.faketrade.ui.main


import android.app.Application
import android.content.Intent
import androidx.lifecycle.*
import com.example.faketrade.domain.APIsCalls
import com.example.faketrade.repo.*
import com.google.android.gms.common.api.ApiException
import kotlinx.coroutines.launch
import okhttp3.Request
import org.json.JSONObject
import java.lang.Error


class MainViewModel(application: Application) : AndroidViewModel(application) {



    val app = application
    private val apiCalls = APIsCalls(app)
    private val googleLoginRepo = GoogleLoginRepo(app, listener = object :GoogleSigninListener{
        override fun onGooglePositiveResponse(token: String) {
            refLoginGoogleUser(token)
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
    fun refCheckIfTokenIsValid(){
        _isValidToken.value = NetworkResult.Loading()
        try{
            viewModelScope.launch {
                val params = APIsCalls.AuthApiRequestParameters()
                params.endpoint =ValidEndpoints.AuthEndpoints.ApiUserRefreshToken
                params.method = Methods.GET

                APIsCalls(app).authApiCall(listener = object: RefCustomListener{
                    override fun onApiJSONResponse(response: JSONObject) {
                        _isValidToken.postValue(NetworkResult.Success(true))
                    }

                    override fun onTokenExpiredResponse(request: Request?) {
                        _isValidToken.postValue(NetworkResult.Success(false))
                    }

                }, params)
            }
        }
        catch(e: Error){
            _isValidToken.value = NetworkResult.Error(data = false, message = e.message)
        }
    }

    fun loginUser(user: JSONObject) {

        viewModelScope.launch {
            try {
                _isValidToken.value = NetworkResult.Loading()
                val params = APIsCalls.AuthApiRequestParameters()
                params.data = user
                params.method = Methods.POST
                params.endpoint = ValidEndpoints.AuthEndpoints.ApiUsers
                apiCalls.authApiCall(parameters = params , listener = object  : RefCustomListener{
                    override fun onApiJSONResponse(response: JSONObject) {

                        if (response.has("code")) {
                            _responseCode.postValue(NetworkResult.Success(response.get("code") as Int))
                        }
                        if (response.get("code")==10200) {
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

                    override fun onTokenExpiredResponse(request: Request?) {
                        _isValidToken.postValue(NetworkResult.Success(false))
                    }

                })

            }
            catch (e:Exception){
                _isValidToken.postValue(NetworkResult.Error(data = false))

            }
        }
    }

    fun refLoginGoogleUser(userToken: String){

        _isValidToken.value = NetworkResult.Loading()
        viewModelScope.launch {
            val data = JSONObject("{}")
            data.put("googleToken", userToken)
            try {

                val params = APIsCalls.AuthApiRequestParameters()
                params.data = data
                params.method = Methods.POST
                params.endpoint = ValidEndpoints.AuthEndpoints.ApiUsersGoogleToken
                APIsCalls(app).authApiCall(parameters = params , listener = object  : RefCustomListener{
                    override fun onApiJSONResponse(response: JSONObject) {
                        _isValidToken.postValue(NetworkResult.Success(true))
                        if (response.has("code")) {
                            _responseCode.postValue(NetworkResult.Success(response.get("code") as Int))
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
                    override fun onTokenExpiredResponse(request: Request?) {
                        _isValidToken.postValue(NetworkResult.Success(false))
                    }
                })
            }
            catch (e:Exception){
                _isValidToken.postValue(NetworkResult.Error(data = false))

            }
        }

    }

}