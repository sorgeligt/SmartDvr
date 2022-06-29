package com.lutalic.smartdvr.ui.main

import android.graphics.Bitmap
import android.media.Image
import android.os.Environment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lutalic.smartdvr.utils.YUVtoRGB
import com.lutalic.smartdvr.utils.getDate
import com.lutalic.smartdvr.utils.toBitmap
import org.jcodec.api.android.AndroidSequenceEncoder
import org.jcodec.common.io.FileChannelWrapper
import org.jcodec.common.io.NIOUtils
import org.jcodec.common.model.Rational
import java.io.File

class MainViewModel : ViewModel() {

    val saveDone = MutableLiveData("");

    private val bitmaps = mutableListOf<Bitmap>()

    private val translator = YUVtoRGB()


    fun addBitmapToList(bitmap: Bitmap) = bitmaps.add(bitmap)
    fun addBitmapToList(img: Image) = bitmaps.add(
        img.toBitmap()
    )

    fun getBitmaps(): List<Bitmap> = bitmaps

    fun clearBitmapList() = bitmaps.clear()


    fun proSave() {

    }


    fun saveBitmapsInVideo() {
        Thread {
            val dir: File =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val date = getDate()
            val file = File(dir, "$date.mp4")
            writeToFile(file)
            bitmaps.clear()
            saveDone.postValue( "save done $date.mp4")
        }.start()
    }

    private fun writeToFile(file: File) {
        var out: FileChannelWrapper? = null
        try {

            out = NIOUtils.writableFileChannel(file.absolutePath)

            val encoder = AndroidSequenceEncoder(out, Rational.R(15, 1))

            val newBitmaps: List<Bitmap> = bitmaps
            for (bitmap in newBitmaps) {
                encoder.encodeImage(bitmap)
            }

            encoder.finish()

        } finally {
            NIOUtils.closeQuietly(out)
        }
    }

    private fun evalImg(img: Image) :Bitmap{
        var bitmap = img.toBitmap()
        val size = bitmap.width * bitmap.height
        val pixels = IntArray(size)
        bitmap.getPixels(
            pixels, 0, bitmap.width, 0, 0,
            bitmap.width, bitmap.height
        )
        for (i in 0 until size) {
            val color = pixels[i]
            val r = color shr 16 and 0xff
            val g = color shr 8 and 0xff
            val b = color and 0xff
            val gray = (r + g + b) / 3
            pixels[i] = -0x1000000 or (gray shl 16) or (gray shl 8) or gray
        }
        bitmap.setPixels(
            pixels, 0, bitmap.width, 0, 0,
            bitmap.width, bitmap.height
        )
        return bitmap
    }


    companion object {
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
    }
}