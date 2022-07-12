package com.example.faketrade.ui.dashboard

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.example.faketrade.R
import com.example.faketrade.repo.NetworkResult
import com.example.faketrade.ui.main.MainActivity


class MainDashboardActivity : AppCompatActivity() {
    private lateinit var viewModel: MainDashboardViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainDashboardViewModel::class.java)
        setContentView(R.layout.main_dashboard)
        viewModel.refCheckIfTokenIsValid()
        viewModel.isValidToken.observe(this){ result ->
            when (result){
                is NetworkResult.Success -> {
                    result.data?.let {
                        if(it){
                            if (savedInstanceState == null) {
                                supportFragmentManager.commit {
                                    supportFragmentManager.beginTransaction()
                                        .replace(R.id.main_dashboard_container, MainDashboardFragment.newInstance())
                                        .commitNow()
                                }

                            }
                        }
                        else{
                            val intent = Intent(this@MainDashboardActivity, MainActivity::class.java)
                            startActivity(intent)
                        }
                    }
                }
                is NetworkResult.Loading -> {
                }
                is NetworkResult.Error ->{

                    val intent = Intent(this@MainDashboardActivity, MainActivity::class.java)
                    startActivity(intent)

                }

            }
        }
    }
}