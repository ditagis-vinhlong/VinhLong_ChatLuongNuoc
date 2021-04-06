package vinhlong.ditagis.com.qlcln.async

import android.app.Activity
import android.app.ProgressDialog
import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import vinhlong.ditagis.com.qlcln.R
import vinhlong.ditagis.com.qlcln.entities.DApplication
import vinhlong.ditagis.com.qlcln.entities.UpdateInfo
import vinhlong.ditagis.com.qlcln.utities.Constant
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL

class CheckForUpdateAsync(private val mActivity: Activity, private val mDelegate: AsyncResponse)
    : AsyncTask<Void, UpdateInfo, UpdateInfo?>() {
    private val exception: Exception? = null
    private var mDialog: ProgressDialog? = null
    private val mDApplication: DApplication = mActivity.application as DApplication


    interface AsyncResponse {
        fun processFinish(output: UpdateInfo?)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        this.mDialog = ProgressDialog(this.mActivity, android.R.style.Theme_Material_Dialog_Alert)
        this.mDialog!!.setMessage(mActivity.getString(R.string.connect_message))
        this.mDialog!!.setCancelable(false)
        this.mDialog!!.show()
    }

    override fun doInBackground(vararg params: Void): UpdateInfo? {
        var conn: HttpURLConnection? = null
        try {
            val updateInfo = getUpdateInfo()
            return updateInfo


        } catch (e: Exception) {
            Log.e("Lỗi đăng nhập", e.toString())
        } finally {
            conn?.disconnect()
        }
        return null
    }

    override fun onPostExecute(updateInfo: UpdateInfo?) {
        //        if (user != null) {
        mDialog!!.dismiss()
        //        }
        this.mDelegate.processFinish(updateInfo)
    }

    private fun getUpdateInfo(): UpdateInfo? {
        var conn: HttpURLConnection? = null
        try {
            val url = Constant.API_URL.UPDATE
            // Read all the text returned by the server
            var bufferedReader =  BufferedReader( InputStreamReader(URL(url).openStream()));
            val stringBuilder = StringBuilder()
            var line: String?
            while (true) {
                line = bufferedReader.readLine()
                if (line == null)
                    break
                stringBuilder.append(line)

            }
            bufferedReader.close();
            return parseUser(stringBuilder.toString())
        } catch (e: Exception) {
            Log.e("Lỗi đăng nhập", e.toString())
        } finally {
            conn?.disconnect()
        }
        return null
    }

    private fun parseUser(data: String?): UpdateInfo {
        val userType = object : TypeToken<UpdateInfo>() {}.type
        return Gson().fromJson(data, userType)
    }
}
