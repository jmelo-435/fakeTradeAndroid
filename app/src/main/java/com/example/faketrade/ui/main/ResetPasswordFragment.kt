package com.example.faketrade.ui.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.faketrade.databinding.CreateAccountFragmentBinding
import com.example.faketrade.databinding.MainFragmentBinding
import com.example.faketrade.databinding.ResetPasswordFragmentBinding
import com.example.faketrade.repo.NetworkResult

class ResetPasswordFragment: Fragment() {

    companion object {
        fun newInstance() = ResetPasswordFragment()
    }

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ResetPasswordFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = ResetPasswordFragmentBinding.inflate(inflater, container, false)
        return binding.root

    }

    override fun onStart() {
        super.onStart()
        val email = binding.editTextTextEmailAddress
        val emailWarn = binding.emailWarn
        val button = binding.login
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        button.setOnClickListener {
            viewModel.sendResetPasswordEmail(email = email.text.toString())
        }
        viewModel.checkedFields.observe(this){
            if(it["email"]==false){
                emailWarn.visibility= View.VISIBLE
            }
            else{
                emailWarn.visibility = View.GONE
            }

            }
        viewModel.isPassReset.observe(this){ result ->

            when (result) {

                is NetworkResult.Success -> {
                    result.data?.let {
                        Toast.makeText(this.requireContext(),it.toString(), Toast.LENGTH_LONG)

                    }
                }

                is NetworkResult.Loading -> {

                    //TODO: handle conection loading

                }

                is NetworkResult.Error -> {

                    //TODO:handle send password reset email error

                }
            }
        }
        }



    }