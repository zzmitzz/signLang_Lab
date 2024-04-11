package com.example.dat90.core.extensions

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageFormat
import android.graphics.Rect
import android.graphics.YuvImage
import android.media.Image
import android.view.View
import android.widget.ImageView
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream


internal fun View.isVisible() = (this.visibility == View.VISIBLE)

internal fun View.makeVisible(){
    this.visibility = View.VISIBLE
}
internal fun View.disableVisible(){
    this.visibility = View.GONE
}

internal fun ImageView.getFromURL(url: String){
    Picasso.get()
        .load(url)
        .into(this)
}
internal fun cvtImgToBitmap(image: Image): Bitmap{
    val planes = image.planes
    val yBuffer = planes[0].buffer
    val uBuffer = planes[1].buffer
    val vBuffer = planes[2].buffer
    val ySize = yBuffer.remaining()
    val uSize = uBuffer.remaining()
    val vSize = vBuffer.remaining()
    val nv21 = ByteArray(ySize + uSize + vSize)
    yBuffer[nv21, 0, ySize]
    vBuffer[nv21, ySize, vSize]
    uBuffer[nv21, ySize + vSize, uSize]
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 75, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)

}