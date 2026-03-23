package com.aikido.musubi

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.aikido.musubi.ui.AikidoNavHost
import com.aikido.musubi.ui.PracticeViewModel
import com.aikido.musubi.ui.theme.AikidoMusubiTheme
import com.aikido.musubi.ui.theme.Onyx
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState

class MainActivity : ComponentActivity() {

    @OptIn(ExperimentalPermissionsApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as AikidoApp

        setContent {
            AikidoMusubiTheme(darkTheme = true) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color    = Onyx
                ) {
                    val cameraPermission = rememberPermissionState(
                        android.Manifest.permission.CAMERA
                    )

                    val vm: PracticeViewModel = viewModel(
                        factory = PracticeViewModel.Factory(app.repository)
                    )

                    if (cameraPermission.status.isGranted) {
                        AikidoNavHost(viewModel = vm)
                    } else {
                        CameraPermissionScreen(
                            onRequestPermission = { cameraPermission.launchPermissionRequest() }
                        )
                    }
                }
            }
        }
    }
}
