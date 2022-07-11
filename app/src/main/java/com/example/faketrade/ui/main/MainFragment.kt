package com.example.faketrade.ui.main

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.faketrade.ui.dashboard.MainDashboardActivity
import com.example.faketrade.R
import com.example.faketrade.Utils
import com.example.faketrade.repo.NetworkResult
import com.example.faketrade.repo.TokenType
import com.example.faketrade.repo.TokensRepo
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.SignInButton
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }


    private lateinit var viewModel: MainViewModel
    var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                // There are no request codes
                val data: Intent? = result.data

                // The Task returned from this call is always completed, no need to attach
                // a listener.
                val task = GoogleSignIn.getSignedInAccountFromIntent(data)
                handleSignInResult(task)

            }
        }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {



        return inflater.inflate(R.layout.main_fragment, container, false)


    }


    override fun onStart() {
        super.onStart()
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        val buttonLogin: Button? = view?.findViewById(R.id.login)
        val nome: EditText? = view?.findViewById(R.id.editTextTextPersonName)
        val senha: EditText? = view?.findViewById(R.id.editTextTextPassword)
        val email: EditText? = view?.findViewById(R.id.editTextTextEmailAddress)
        val textView: TextView? = view?.findViewById(R.id.textView2)
        val googleButton: SignInButton? = view?.findViewById(R.id.sign_in_button)
        var user = JSONObject("{}")
        val tokenRepo= TokensRepo(this.requireContext())
        val clientId = getString(R.string.server_client_id)
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(clientId)
            .requestEmail()
            .build()
        // Build a GoogleSignInClient with the options specified by gso.

        val mGoogleSignInClient = GoogleSignIn.getClient(this.requireContext(), gso)
        val signInIntent = mGoogleSignInClient.signInIntent



        fun performIsAthorizedCheck(){
            lifecycleScope.launch {
                viewModel.isAuthorizedCheck(this@MainFragment.requireContext(), 1).collectLatest{
                    if(it){
                        val intent = Intent(requireActivity(), MainDashboardActivity::class.java)
                        startActivity(intent)

                    }
                }
            }

        }
        viewModel.isValidToken.observe(this) { result ->
            when (result) {

                is NetworkResult.Success -> {
                    result.data?.let {
                        if (it) {


                            val intent =
                                Intent(this.requireContext(), MainDashboardActivity::class.java)
                            startActivity(intent)


                        } else {
                            //TODO:handle api connection error

                        }


                    }
                }


                is NetworkResult.Loading -> {
                    //TODO: handle conection loading
                }
                is NetworkResult.Error -> {
                    //TODO:handle local connection error

                }

            }
        }

        fun handleResultCodes(resultCode: Int){
            //TODO: Implementar a interação de UI
            if (resultCode==10200||resultCode==20201){
                lifecycleScope.launch {

                    performIsAthorizedCheck()
                }

            }
            if (resultCode==504){
                //TODO: Implementar erro de conexão
                Toast.makeText(this.requireContext(), resultCode.toString(), Toast.LENGTH_LONG).show()
            }
            else{
                //TODO: Implementar erro retornado da API
                Toast.makeText(this.requireContext(), resultCode.toString(), Toast.LENGTH_LONG).show()
            }


        }
        fun handleRequestLoading(){
            //TODO: Implementar a interação de UI
            textView?.text = "loading"
        }

        fun lauchGoogleSignin() {

            resultLauncher.launch(signInIntent)
        }
        googleButton?.setOnClickListener {
            lifecycleScope.launch {

                lauchGoogleSignin()

            }
        }


        fun isEmpty(text: String?): Boolean {
            return Utils().isEmptyOrNull(text)
        }


        buttonLogin?.setOnClickListener(View.OnClickListener {
            //TODO: Implementar a administração de entrada de dados
            if (isEmpty(email?.text.toString()) || isEmpty(nome?.text.toString()) || isEmpty(senha?.text.toString())) {

                Toast.makeText(view?.context, "Preencha os campos", Toast.LENGTH_LONG).show()
            } else {
                user.put("email", email?.text)
                user.put("userName", nome?.text)
                user.put("password", senha?.text)

                viewModel.loginUser(user)


            }

        })

        viewModel.bearerToken.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        result.data?.let {
                            tokenRepo.saveToken(it,TokenType.BEARER)
                        }
                    }


                    else -> {}
                }

        }

        viewModel.accessToken.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        result.data?.let {
                            tokenRepo.saveToken(it,TokenType.ACCESS)
                        }
                    }


                    else -> {

                    }

                }

        }
        viewModel.authTokens.observe(viewLifecycleOwner){
            it[TokenType.BEARER]?.let { it1 -> tokenRepo.saveToken(it1,TokenType.BEARER) }
            it[TokenType.ACCESS]?.let { it1 -> tokenRepo.saveToken(it1,TokenType.ACCESS) }
        }
        viewModel.responseCode.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        result.data?.let {
                            handleResultCodes(it)
                        }
                    }



                    is NetworkResult.Loading -> {
                        handleRequestLoading()
                    }
                    is NetworkResult.Error->{

                        result.data?.let { handleResultCodes(it) }
                        result.message.let { Toast.makeText(this.requireContext(),it,Toast.LENGTH_LONG).show() }

                    }

                }


        }




    }


    private fun handleSignInResult(completedTask: Task<GoogleSignInAccount>) {

        try {
            val account = completedTask.getResult(ApiException::class.java)
            account.idToken?.let { viewModel.loginGoogleUser(it) }


        } catch (e: ApiException) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.

            Toast.makeText(view?.context, e.message, Toast.LENGTH_LONG).show()

        }

    }
}


