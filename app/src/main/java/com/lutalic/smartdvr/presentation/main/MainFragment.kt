package com.lutalic.smartdvr.presentation.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.github.pwittchen.neurosky.library.exception.BluetoothNotEnabledException
import com.lutalic.smartdvr.R
import com.lutalic.smartdvr.databinding.FragmentMainBinding
import com.lutalic.smartdvr.presentation.graph.GraphFragment
import dev.bmcreations.scrcast.ScrCast
import org.koin.androidx.viewmodel.ext.android.viewModel
import kotlin.math.abs
import kotlin.random.Random


class MainFragment : Fragment() {

    private lateinit var binding: FragmentMainBinding

    private var recordingFlag = false

    private val viewModel: MainViewModel by viewModel()

    private val recorder by lazy {
        ScrCast.use(requireActivity()).apply {
            options {
                storage {
                    directoryName = "Smart_dvr"
                }
                notification {
                    title = "SmartDvr work"
                    description = "recording..."
                    channel {
                        id = "Recording Service1"
                        name = "Recording Service"
                    }
                    showStop = true
                    showPause = true
                    showTimer = true
                }
                startDelayMs = 500
                stopOnScreenOff = true
            }
        }
    }


    private val requestMultiplePermissions =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val granted = permissions.entries.all { it.value }
            if (granted) {
                startCamera()
            } else {
                checkPermissions()
            }
        }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentMainBinding.inflate(inflater, container, false)
        checkPermissions()
        recorder.onRecordingComplete {
            Toast.makeText(requireActivity(), "Video saved in $it", Toast.LENGTH_SHORT).show()
        }
        binding.videoCaptureButton.setOnClickListener {
            captureVideo()
        }
        binding.allInfoAboutBrain.setOnClickListener {
            parentFragmentManager.beginTransaction().addToBackStack(null)
                .replace(R.id.container, GraphFragment.newInstance()).commit()
        }
        viewModel.attention.observe(viewLifecycleOwner) {
            binding.attention.text = it
        }
        viewModel.meditation.observe(viewLifecycleOwner) {
            binding.meditation.text = it
        }
        viewModel.fatigue.observe(viewLifecycleOwner) {
            binding.fatigue.text = it
        }

        startTimer()

        try {
            viewModel.connect()
            Toast.makeText(requireActivity(), "Success connect neuroSky", Toast.LENGTH_LONG).show()
            viewModel.start()
        } catch (e: BluetoothNotEnabledException) {
            Toast.makeText(requireActivity(), e.message, Toast.LENGTH_LONG).show()
        }
        return binding.root
    }

    private fun startTimer() {


    }

    private fun checkPermissions() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestMultiplePermissions.launch(REQUIRED_PERMISSIONS)
        }
    }


    private fun captureVideo() {
        if (!recordingFlag) {
            Toast.makeText(requireActivity(), "Recording start!", Toast.LENGTH_SHORT).show()
            binding.videoCaptureButton.alpha = 0.5f
            recorder.record()
            recordingFlag = true
        } else {
            binding.videoCaptureButton.alpha = 1f
            recorder.stopRecording()
            recordingFlag = false
            Toast.makeText(requireActivity(), "Recording stop!", Toast.LENGTH_SHORT).show()
        }
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())

        cameraProviderFuture.addListener({
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(this, cameraSelector, preview)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(requireActivity()))
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireActivity().baseContext,
            it
        ) == PackageManager.PERMISSION_GRANTED
    }

    override fun onStop() {
        super.onStop()
        viewModel.disconnect()
    }

    companion object {
        fun newInstance() = MainFragment()

        private const val TAG = "SmartDvrMainFragment"

        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }

}