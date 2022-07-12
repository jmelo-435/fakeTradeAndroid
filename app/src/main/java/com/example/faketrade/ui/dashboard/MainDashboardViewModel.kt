package com.example.faketrade.ui.dashboard

import android.app.Application
import androidx.lifecycle.*
import com.example.faketrade.domain.APIsCalls
import com.example.faketrade.repo.*
import com.example.faketrade.repo.NetworkResult
import kotlinx.coroutines.launch
import okhttp3.Request
import org.json.JSONObject
import java.lang.Error


class MainDashboardViewModel(application: Application) : AndroidViewModel(application) {


    val app = application
    private var _responseCode: MutableLiveData<NetworkResult<Int>> = MutableLiveData()
    var responseCode: LiveData<NetworkResult<Int>> = _responseCode

    private var _saldo: MutableLiveData<NetworkResult<Int>> = MutableLiveData()
    var saldo: LiveData<NetworkResult<Int>> = _saldo

    private var _isValidToken: MutableLiveData<NetworkResult<Boolean>> = MutableLiveData()
    var isValidToken: LiveData<NetworkResult<Boolean>> = _isValidToken

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
        catch(e:Error){
            _isValidToken.value = NetworkResult.Error(data = false, message = e.message)
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
    fun refGetUserSaldo(){
        viewModelScope.launch {
            try{
                _saldo.value = NetworkResult.Loading()
                val params = APIsCalls.ResourceApiRequestParameters()
                params.method = Methods.GET
                params.endpoint = ValidEndpoints.ResourceEndpoints.UserSaldo
                APIsCalls(app).resourceApiCall(listener = object :RefCustomListener{
                    override fun onApiJSONResponse(response: JSONObject) {
                        handleResponseBody(response)
                    }

                    override fun onTokenExpiredResponse(request: Request?) {
                        TODO("Not yet implemented")
                    }

                }, parameters = params)
            }
            catch (e:Exception){
                _saldo.value = NetworkResult.Error(data = 100, message = e.message)
            }

        }

    }





}
