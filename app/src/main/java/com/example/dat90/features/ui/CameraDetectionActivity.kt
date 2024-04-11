package com.example.dat90.features.ui

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.HandlerThread
import android.os.SystemClock
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.example.dat90.core.platform.BaseActivity
import com.example.dat90.databinding.CameraDetectActivityBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

open class CameraDetectionActivity: BaseActivity<CameraDetectActivityBinding>(), ImageAnalysis.Analyzer {
    private val viewModel by viewModels<CameraDetectionVM>()

    private lateinit var cameraExecutor: ExecutorService
    private var mLastAnalysisResultTime: Long = 0
    private  val FRONT_CAMERA: Int = 0
    private val BACK_CAMERA: Int = 1
    private var currentSide_Camera = BACK_CAMERA

    private val activityResultLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions())
        { permissions ->
            // Handle Permission granted/rejected
            var permissionGranted = true
            permissions.entries.forEach {
                if (it.key in REQUIRED_PERMISSIONS && !it.value)
                    permissionGranted = false
            }
            if (!permissionGranted) {
                Toast.makeText(baseContext,
                    "Permission request denied",
                    Toast.LENGTH_SHORT).show()
            } else {
                startCamera()
            }
        }
    private fun allPermissionsGranted(): Boolean{
        return REQUIRED_PERMISSIONS.all {
            ContextCompat.checkSelfPermission(
                baseContext, it) == PackageManager.PERMISSION_GRANTED
        }
    }

    override fun requestPermissions(){
        activityResultLauncher.launch(REQUIRED_PERMISSIONS)
    }

    override fun doThingsOnCreate() {
        // Do magic
        if(allPermissionsGranted()){
            startCamera()
        }else{
            requestPermissions()
        }
        cameraExecutor = Executors.newSingleThreadExecutor()
        binding.backbtn.setOnClickListener{
            onBackPressedDispatcher.onBackPressed()
        }
        viewModel.pathFile = assetFilePath(applicationContext, "asl.ptl")
        binding.flip.setOnClickListener{
            currentSide_Camera = currentSide_Camera xor 1
            startCamera()
        }
    }

    override fun initViewBinding() {
        binding = CameraDetectActivityBinding.inflate(layoutInflater)
    }


    fun applyToUiAnalyzeImageResult(result: CameraDetectionVM.AnalysisResult?){
        binding.prediction.text = result!!.result
    }
    private fun startCamera(){
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()
            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewCamera.surfaceProvider)
                }
            var cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            if(currentSide_Camera == 0){
                cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA
            }

            val imageAnalysis = ImageAnalysis.Builder()
                .setTargetResolution(Size(224, 224))
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor, this)

                }
            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()
                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageAnalysis
                )

            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(this))
    }
    @Throws(IOException::class)
    fun assetFilePath(context: Context, assetName: String?): String? {
        val file = File(context.filesDir, assetName!!)
        if (file.exists() && file.length() > 0) {
            return file.absolutePath
        }
        context.assets.open(assetName).use { `is` ->
            FileOutputStream(file).use { os ->
                val buffer = ByteArray(4 * 1024)
                var read: Int
                while (`is`.read(buffer).also { read = it } != -1) {
                    os.write(buffer, 0, read)
                }
                os.flush()
            }
            return file.absolutePath
        }
    }
    companion object{
        private val TAG = "HIUHIU"
        private val REQUIRED_PERMISSIONS = mutableListOf(
            Manifest.permission.CAMERA,
            Manifest.permission.RECORD_AUDIO
        ).apply {
            if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            }
        }.toTypedArray()
    }

    override fun analyze(image: ImageProxy) {
        if (SystemClock.elapsedRealtime() - mLastAnalysisResultTime < 500) {
            image.close()
            return
        }else{
            CoroutineScope(Dispatchers.Main).launch {
                viewModel.analyzeImage(image)
                viewModel.resultString.observe(this@CameraDetectionActivity){
                    runOnUiThread() {
                        applyToUiAnalyzeImageResult(viewModel.resultString.value)
                        Log.d("HIUHIU", "LiveData: " + it.result)
                    }
                }
                mLastAnalysisResultTime = SystemClock.elapsedRealtime()
                image.close()

            }
        }
    }
}