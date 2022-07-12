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
import com.example.faketrade.repo.*
import com.google.android.gms.common.SignInButton
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.json.JSONObject


class MainFragment : Fragment() {

    companion object {
        fun newInstance() = MainFragment()
    }

    private lateinit var viewModel: MainViewModel

    private var resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                val data: Intent? = result.data
                viewModel.handleSingnInResult(data)
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
        val googleLoginRepo = GoogleLoginRepo(this.requireContext())

        viewModel.googleError.observe(this){ result ->

            when (result) {

                is NetworkResult.Success -> {
                    result.data?.let {
                        Toast.makeText(this.requireContext(),it,Toast.LENGTH_LONG)


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


        googleButton?.setOnClickListener {

            resultLauncher.launch(googleLoginRepo.signInIntent)

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


        viewModel.responseCode.observe(viewLifecycleOwner) { result ->
                when (result) {
                    is NetworkResult.Success -> {
                        result.data?.let {
                            handleResultCodes(it)
                        }
                    }

                    is NetworkResult.Loading -> {

                        //TODO: Implementar interação de UI

                    }
                    is NetworkResult.Error->{

                        result.data?.let { handleResultCodes(it) }
                        result.message.let { Toast.makeText(this.requireContext(),it,Toast.LENGTH_LONG).show() }

                    }

                }

        }
    }
}


