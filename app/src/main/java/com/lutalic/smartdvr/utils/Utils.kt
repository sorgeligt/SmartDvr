package com.lutalic.smartdvr.utils

import android.graphics.*
import android.media.Image
import java.io.ByteArrayOutputStream
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


fun Image.toBitmap(): Bitmap {
    val yBuffer = planes[0].buffer // Y
    val vuBuffer = planes[2].buffer // VU

    val ySize = yBuffer.remaining()
    val vuSize = vuBuffer.remaining()

    val nv21 = ByteArray(ySize + vuSize)

    yBuffer.get(nv21, 0, ySize)
    vuBuffer.get(nv21, ySize, vuSize)

    val yuvImage = YuvImage(nv21, ImageFormat.NV21, this.width, this.height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, yuvImage.width, yuvImage.height), 20, out)
    val imageBytes = out.toByteArray()
    return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
}


fun getDate() : String{
    val currentDate = Date()
// Форматирование времени как "день.месяц.год"
// Форматирование времени как "день.месяц.год"
    val dateFormat: DateFormat = SimpleDateFormat("dd_MM_yyyy", Locale.getDefault())
    val dateText: String = dateFormat.format(currentDate)
// Форматирование времени как "часы:минуты:секунды"
// Форматирование времени как "часы:минуты:секунды"
    val timeFormat: DateFormat = SimpleDateFormat("_HH_mm_ss", Locale.getDefault())
    val timeText: String = timeFormat.format(currentDate)
    return "$dateText$timeText"
}

fun Bitmap.drawStringOnBitmap(
    string: String,
    location: Point,
    color: Int,
    alpha: Int,
    size: Int,
    underline: Boolean,
    width: Int,
    height: Int
) :Bitmap{
    val result = Bitmap.createBitmap(width, height, this.config)
    val canvas = Canvas(result)
    canvas.drawBitmap(this, 0f, 0f, null)
    val paint = Paint()
    paint.color = color
    paint.alpha = alpha
    paint.textSize = size.toFloat()
    paint.isAntiAlias = true
    paint.isUnderlineText = underline
    canvas.drawText(string, location.x.toFloat(), location.y.toFloat(), paint)
    return result
}