package com.example.faketrade.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.example.faketrade.R
import com.example.faketrade.repo.NetworkResult
import com.example.faketrade.ui.main.MainActivity
import com.example.faketrade.ui.main.MainFragment
import com.example.faketrade.ui.main.MainViewModel

class MainDashboardFragment : Fragment() {
    private lateinit var viewModel: MainDashboardViewModel

    companion object {
        fun newInstance() = MainDashboardFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {


        return inflater.inflate(R.layout.main_dashboard_fragment, container, false)


    }

    override fun onStart() {
        super.onStart()
        val textView: TextView? = view?.findViewById(R.id.saldo)
        val button: Button? = view?.findViewById(R.id.button)
        viewModel = ViewModelProvider(this).get(MainDashboardViewModel::class.java)



        viewModel.isValidToken.observe(this) { result ->
            when (result) {

                is NetworkResult.Success -> {
                    result.data?.let {
                        if (!it) {
                            val intent = Intent(this.requireContext(), MainActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }


                is NetworkResult.Loading -> {

                }
                is NetworkResult.Error -> {

                    val intent = Intent(this.requireContext(), MainActivity::class.java)
                    startActivity(intent)

                }

            }
        }


        viewModel = ViewModelProvider(this).get(MainDashboardViewModel::class.java)

        button?.setOnClickListener {
            viewModel.getSaldo()
        }

        viewModel.saldo.observe(this) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    result.data?.let {
                        textView?.text = it.toString()
                    }
                }


                is NetworkResult.Loading -> {
                    textView?.text = "Loading"
                }
                is NetworkResult.Error -> {

                    result.message.let {
                        Toast.makeText(
                            this.requireContext(),
                            it,
                            Toast.LENGTH_LONG
                        ).show()
                    }

                }

            }
        }
    }
}