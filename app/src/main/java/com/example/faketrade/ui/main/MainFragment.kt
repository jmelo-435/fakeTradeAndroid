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
import com.example.faketrade.databinding.MainFragmentBinding
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
    private lateinit var binding: MainFragmentBinding

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
        binding = MainFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }


    override fun onStart() {
        super.onStart()

        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)

        val buttonLogin: Button? = binding.login
        val senha: EditText? = binding.editTextTextPassword
        val email: EditText? = binding.editTextTextEmailAddress
        val textView: TextView? = binding.textView2
        val googleButton: SignInButton? = binding.signInButton
        val passWarn = binding.passWarn
        val emailWarn = binding.emailWarn

        val googleLoginRepo = GoogleLoginRepo(this.requireContext())

        googleButton?.setOnClickListener {
            resultLauncher.launch(googleLoginRepo.signInIntent)
        }

        buttonLogin?.setOnClickListener(View.OnClickListener {
            viewModel.loginUser(email = email?.text.toString(), password = senha?.text.toString())

        })

        viewModel.checkedFields.observe(this){
            if(!it["email"]!!){
                emailWarn.visibility = View.VISIBLE
            }else{
                emailWarn.visibility = View.GONE

            }
            if(!it["password"]!!){
                passWarn.visibility = View.VISIBLE

            }else{
                passWarn.visibility = View.GONE

            }
        }

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
                    Toast.makeText(this.requireContext(), result.message.toString(), Toast.LENGTH_LONG).show()

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


