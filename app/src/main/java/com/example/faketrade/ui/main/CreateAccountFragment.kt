package com.example.faketrade.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.faketrade.databinding.CreateAccountFragmentBinding
import com.example.faketrade.databinding.MainFragmentBinding

class CreateAccountFragment: Fragment() {

    companion object {
        fun newInstance() = CreateAccountFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: CreateAccountFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = CreateAccountFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onStart() {
        super.onStart()
        val email = binding.editTextTextEmailAddress2
        val password = binding.editTextTextPassword2
        val confPassword = binding.editTextTextPassword3
        val name = binding.editTextTextPersonName
        val nameWarn = binding.nameWarn
        val passWarn = binding.createAccountPassWarn
        val emailWarn = binding.createAccountEmailWarn
        val passConfWarn = binding.createAccountPassConfWarn
        val button = binding.button2
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        button.setOnClickListener {
            viewModel.createUser(email.text.toString(), name.text.toString(),confPassword.text.toString(),password.text.toString())
        }
        viewModel.checkedFields.observe(this){
            if(it["email"]==false){
                emailWarn.visibility= View.VISIBLE
            }
            else{
                emailWarn.visibility = View.GONE
            }
            if(it["password"]==false){
                passWarn.visibility= View.VISIBLE
            }
            else{
                passWarn.visibility = View.GONE
            }
            if(it["confirmPassword"]==false){
                passConfWarn.visibility= View.VISIBLE
            }
            else{
                passConfWarn.visibility = View.GONE
            }
            if(it["userName"]==false){
                nameWarn.visibility= View.VISIBLE
            }
            else{
                nameWarn.visibility = View.GONE
            }
        }



    }
}