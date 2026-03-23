package com.aikido.musubi

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.aikido.musubi.databinding.ActivityMainBinding
import com.aikido.musubi.ui.PracticeViewModel
import com.aikido.musubi.ui.screens.CameraPermissionFragment
import com.aikido.musubi.ui.screens.HomeFragment

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    lateinit var viewModel: PracticeViewModel

    private val requestPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) showHome() else showPermissionScreen()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val app = application as AikidoApp
        viewModel = ViewModelProvider(
            this,
            PracticeViewModel.Factory(app.repository)
        )[PracticeViewModel::class.java]

        if (savedInstanceState == null) {
            if (hasCameraPermission()) showHome()
            else showPermissionScreen()
        }
    }

    private fun hasCameraPermission() =
        ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED

    fun requestCameraPermission() = requestPermission.launch(Manifest.permission.CAMERA)

    fun showHome() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, HomeFragment())
            .commit()
    }

    private fun showPermissionScreen() {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, CameraPermissionFragment())
            .commit()
    }
}
