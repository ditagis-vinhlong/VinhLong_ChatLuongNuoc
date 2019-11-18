package vinhlong.ditagis.com.qlcln.utities

import android.content.Context
import android.os.Environment

import java.io.File
import java.text.DateFormat
import java.text.SimpleDateFormat

import vinhlong.ditagis.com.qlcln.R

/**
 * Created by ThanLe on 12/8/2017.
 */

object ImageFile {
    var formatter: DateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")

    fun getFile(context: Context): File {
        val path = Environment.getExternalStorageDirectory().path
        val outFile = File(path, context.resources.getString(R.string.path_saveImage))
        if (!outFile.exists())
            outFile.mkdir()
        return File(outFile, "xxx.png")
    }

}
