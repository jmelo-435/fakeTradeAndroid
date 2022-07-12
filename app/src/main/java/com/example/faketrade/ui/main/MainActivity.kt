package com.example.faketrade.ui.main


import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.faketrade.R
import com.example.faketrade.repo.NetworkResult
import com.example.faketrade.ui.dashboard.MainDashboardActivity
import com.example.faketrade.ui.dashboard.MainDashboardFragment
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch


class MainActivity : AppCompatActivity() {
    private lateinit var viewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(MainViewModel::class.java)
        viewModel.checkIfTokenIsValid()
        fun initializeUI() {
            setContentView(R.layout.main_activity)
            supportFragmentManager.commit {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.container, MainFragment.newInstance())
                    .commitNow()
            }
        }
        viewModel.isValidToken.observe(this) { result ->
            when (result) {
                is NetworkResult.Success -> {
                    result.data?.let {
                        if (it) {
                            if (savedInstanceState == null) {
                                val intent =
                                    Intent(this@MainActivity, MainDashboardActivity::class.java)
                                startActivity(intent)
                            }
                        } else {
                            initializeUI()
                        }
                    }

                }
                is NetworkResult.Loading -> {
                }
                is NetworkResult.Error -> {
                    initializeUI()
                }
            }
        }
    }
}
