package com.example.dat90.features.utils

import android.graphics.Bitmap
import android.graphics.Color
import android.os.SystemClock
import android.util.Pair
import org.pytorch.IValue
import org.pytorch.Module
import org.pytorch.Tensor
import java.nio.FloatBuffer

private const val SIZE = 224
internal fun bitmapRecognition(bitmap: Bitmap, module: Module?): kotlin.Pair<Int, Long> {
        val inTensorBuffer: FloatBuffer =
            Tensor.allocateFloatBuffer(3 * SIZE * SIZE)
        for (x in 0 until SIZE) {
            for (y in 0 until SIZE) {
                val colour = bitmap.getPixel(x, y)
                val red = Color.red(colour)
                val blue = Color.blue(colour)
                val green = Color.green(colour)
                inTensorBuffer.put(x + SIZE * y, blue.toFloat())
                inTensorBuffer.put(
                    SIZE * SIZE + x + SIZE * y,
                    green.toFloat()
                )
                inTensorBuffer.put(
                    2 * SIZE * SIZE + x + SIZE * y,
                    red.toFloat()
                )
            }
        }
        val inputTensor: Tensor = Tensor.fromBlob(
            inTensorBuffer,
            longArrayOf(1, 3, SIZE.toLong(), SIZE.toLong())
        )
        val startTime = SystemClock.elapsedRealtime()
        val outTensor: Tensor? = module?.forward(IValue.from(inputTensor))?.toTensor()
        val inferenceTime = SystemClock.elapsedRealtime() - startTime
        val scores: FloatArray? = outTensor?.dataAsFloatArray
        var maxScore = -Float.MAX_VALUE
        var maxScoreIdx = -1
        for (i in scores?.indices!!) {
            if (scores[i] > maxScore) {
                maxScore = scores[i]
                maxScoreIdx = i
            }
        }
        return maxScoreIdx to inferenceTime
    }