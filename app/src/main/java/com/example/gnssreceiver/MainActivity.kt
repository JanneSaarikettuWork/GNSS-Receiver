package com.example.gnssreceiver

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.ViewModelProvider
import com.example.gnssreceiver.ui.GnssMainScreen
import com.example.gnssreceiver.ui.GnssViewModel
import com.example.gnssreceiver.ui.theme.GNSSReceiverTheme

class MainActivity : ComponentActivity() {

    private lateinit var viewModel: GnssViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        viewModel = ViewModelProvider(this, GnssViewModel.Factory(this))[GnssViewModel::class.java]

        setContent {
            GNSSReceiverTheme {
                GnssMainScreen(viewModel = viewModel)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.checkGpsStatus()
    }
}
