package com.lutalic.smartdvr.ui.main

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lutalic.smartdvr.databinding.FragmentMainBinding


class MainFragment : Fragment(), ImageAnalysis.Analyzer {


    private lateinit var binding: FragmentMainBinding

    private var recordingFlag = false

    private val viewModel: MainViewModel by viewModels()


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
        binding.videoCaptureButton.setOnClickListener { captureVideo() }
        viewModel.saveDone.observe(viewLifecycleOwner) {
            Toast.makeText(requireActivity(), it + "chel", Toast.LENGTH_SHORT).show()
        }
        return binding.root
    }

    private fun checkPermissions() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestMultiplePermissions.launch(REQUIRED_PERMISSIONS)
        }
    }


    private fun captureVideo() {
        if (recordingFlag) {
            Toast.makeText(requireActivity(), "Stop $recordingFlag", Toast.LENGTH_SHORT).show()
            recordingFlag = false
            viewModel.saveBitmapsInVideo()
        } else {
            Toast.makeText(requireActivity(), "Start $recordingFlag", Toast.LENGTH_SHORT).show()
            recordingFlag = true
        }
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireActivity())

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }
            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(720, 480))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .setOutputImageFormat(ImageAnalysis.OUTPUT_IMAGE_FORMAT_YUV_420_888)
                .build()
            imageAnalysis.setAnalyzer(
                ContextCompat.getMainExecutor(requireActivity())
            ) { image ->
                if (recordingFlag) {
                    @SuppressLint("UnsafeOptInUsageError")
                    val img: Image = image.image ?: return@setAnalyzer
                    viewModel.addBitmapToList(img)
                }
                image.close()
            }


            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA


            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider
                    .bindToLifecycle(this, cameraSelector, preview, imageAnalysis)
            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireActivity()))
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat
            .checkSelfPermission(
                requireActivity().baseContext,
                it
            ) == PackageManager.PERMISSION_GRANTED
    }


    companion object {
        fun newInstance() = MainFragment()

        private const val TAG = "CameraXApp"

        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
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

    override fun analyze(image: ImageProxy) {

    }
}