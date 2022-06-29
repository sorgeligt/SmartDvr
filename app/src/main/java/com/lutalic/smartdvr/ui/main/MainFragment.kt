package com.lutalic.smartdvr.ui.main

import android.Manifest
import android.content.ContentValues
import android.content.pm.PackageManager
import android.icu.text.SimpleDateFormat
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
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
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.lutalic.smartdvr.YUVtoRGB
import com.lutalic.smartdvr.databinding.FragmentMainBinding
import java.util.*
import java.util.concurrent.Executor


class MainFragment : Fragment(),ImageAnalysis.Analyzer {

    private var videoCapture: VideoCapture<Recorder>? = null
    private var recording: Recording? = null

    private lateinit var binding: FragmentMainBinding

    private val viewModel: MainViewModel by viewModels()

    var translator = YUVtoRGB()

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

        return binding.root
    }

    private fun checkPermissions() {
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            requestMultiplePermissions.launch(REQUIRED_PERMISSIONS)
        }
    }

    /*private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(), REQUIRED_PERMISSIONS, REQUEST_CODE_PERMISSIONS
        )
    }*/

    private fun captureVideo() {
        val videoCapture = this.videoCapture ?: return
        Toast.makeText(requireActivity(), "MDAAA", Toast.LENGTH_LONG).show()
        binding.videoCaptureButton.isEnabled = false

        val curRecording = recording
        if (curRecording != null) {
            // Stop the current recording session.
            curRecording.stop()
            recording = null
            return
        }

        // create and start a new recording session
        val name = SimpleDateFormat(
            FILENAME_FORMAT, Locale.US
        )
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CameraX-Video")
            }

        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(requireActivity().contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()
        recording = videoCapture.output
            .prepareRecording(requireActivity(), mediaStoreOutputOptions)
            .apply {
                if (PermissionChecker.checkSelfPermission(requireActivity(),
                        Manifest.permission.RECORD_AUDIO) ==
                    PermissionChecker.PERMISSION_GRANTED)
                {
                    withAudioEnabled()
                }
            }
            .start(ContextCompat.getMainExecutor(requireActivity())) { recordEvent ->
                when(recordEvent) {
                    is VideoRecordEvent.Start -> {
                        binding.videoCaptureButton.apply {
                            text = "stop"
                            isEnabled = true
                        }
                    }
                    is VideoRecordEvent.Finalize -> {
                        if (!recordEvent.hasError()) {
                            val msg = "Video capture succeeded: " +
                                    "${recordEvent.outputResults.outputUri}"
                            Toast.makeText(requireActivity().baseContext, msg, Toast.LENGTH_SHORT)
                                .show()
                            Log.d(TAG, msg)
                        } else {
                            recording?.close()
                            recording = null
                            Log.e(TAG, "Video capture ends with error: " +
                                    "${recordEvent.error}")
                        }
                        binding.videoCaptureButton.apply {
                            text = "start"
                            isEnabled = true
                        }
                    }
                }
            }}

    fun getExecutor(): Executor {
        return ContextCompat.getMainExecutor(requireContext())
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

            val recorder = Recorder.Builder()
                .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
                .build()
            videoCapture = VideoCapture.withOutput(recorder)



            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA



            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider
                    .bindToLifecycle(this, cameraSelector, preview, videoCapture)
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireActivity()))
    }


    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat
            .checkSelfPermission(requireActivity().baseContext, it) == PackageManager.PERMISSION_GRANTED
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