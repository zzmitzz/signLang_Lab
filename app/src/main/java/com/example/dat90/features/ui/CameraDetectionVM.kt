package com.example.dat90.features.ui

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.annotation.OptIn
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import androidx.lifecycle.MutableLiveData
import com.example.dat90.core.extensions.cvtImgToBitmap
import com.example.dat90.core.platform.BaseViewModel
import com.example.dat90.features.utils.bitmapRecognition
import org.pytorch.LiteModuleLoader
import org.pytorch.Module

class CameraDetectionVM: BaseViewModel() {

    private var mModule: Module? = null
    private val DELETE = 26
    private val NOTHING = 27
    private val SPACE = 28
    val SIZE = 224
    var pathFile: String? = null
    var resultString: MutableLiveData<AnalysisResult> = MutableLiveData<AnalysisResult>()
    class AnalysisResult(val result: String)


    @OptIn(ExperimentalGetImage::class)
    fun analyzeImage(image: ImageProxy) {
            if (mModule == null) {
                try {
                    mModule = LiteModuleLoader.load(pathFile)

                } catch (e: Exception) {
                }
            }
            var bitmap: Bitmap = cvtImgToBitmap(image.image!!)
            var matrix: Matrix = Matrix()
            matrix.postRotate(90.0f);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true);
            bitmap = Bitmap.createScaledBitmap(bitmap, SIZE, SIZE, true);

            var idxTm: Pair<Int, Long> = bitmapRecognition(bitmap, mModule);
            var maxScoreIdx: Int = idxTm.first;
            var inferenceTime = idxTm.second;
            var result: String = (1 + maxScoreIdx + 64).toChar().toString();
            Log.d("HIUHIU", result)
            if (maxScoreIdx == DELETE) {
                result = "DELETE";
            } else if (maxScoreIdx == NOTHING) {
                result = "NOTHING";
            } else if (maxScoreIdx == SPACE) {
                result = "SPACE";
            }
            resultString.value = AnalysisResult(String.format("%s - %dms", result, inferenceTime))
        }
    }