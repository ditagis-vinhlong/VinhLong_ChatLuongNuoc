package vinhlong.ditagis.com.qlcln.utities

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException

class DBitmap {
    fun getDecreaseSizeBitmap(bitmap: Bitmap): Bitmap{
        val maxSize = 700.toFloat()
        var width = bitmap.width.toFloat()
        var height = bitmap.height.toFloat()
        val bitmapRatio = width / height
        if (bitmapRatio > 1) {
            width = maxSize
            height = (width / bitmapRatio)
        } else {
            height = maxSize
            width = (height * bitmapRatio)
        }
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, width.toInt(), height.toInt(), true)
        return scaledBitmap
    }
    fun getBitmap(byteArray: ByteArray): Bitmap{

        return BitmapFactory.decodeByteArray(byteArray, 0, byteArray.size)
    }
    fun getByteArray(bitmap: Bitmap): ByteArray {
        val tmpBitmap = getDecreaseSizeBitmap(bitmap)
        val outputStream = ByteArrayOutputStream()
        val image: ByteArray
        tmpBitmap.compress(Constant.CompressFormat.TYPE_UPDATE, 100, outputStream)
        image = outputStream.toByteArray()

        try {
            outputStream.close()
        } catch (e: IOException) {

        }

        return image
    }
}