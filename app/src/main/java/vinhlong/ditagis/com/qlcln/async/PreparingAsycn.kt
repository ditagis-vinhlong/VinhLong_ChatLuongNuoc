package vinhlong.ditagis.com.qlcln.async

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import vinhlong.ditagis.com.qlcln.R
import vinhlong.ditagis.com.qlcln.entities.DAppInfo
import vinhlong.ditagis.com.qlcln.entities.DApplication
import vinhlong.ditagis.com.qlcln.entities.DLayerInfo
import vinhlong.ditagis.com.qlcln.entities.entitiesDB.LayerInfoDTG
import vinhlong.ditagis.com.qlcln.utities.Constant
import vinhlong.ditagis.com.qlcln.utities.Preference
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class PreparingAsycn(private val mContext: Context,private val mApplication: DApplication, private val mDelegate: AsyncResponse) : AsyncTask<Void, Void, List<DLayerInfo>?>() {
    private var mDialog: ProgressDialog? = null

    interface AsyncResponse {
        fun processFinish(output: List<DLayerInfo>?)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        this.mDialog = ProgressDialog(this.mContext, android.R.style.Theme_Material_Dialog_Alert)
        this.mDialog!!.setMessage(mContext.getString(R.string.preparing))
        this.mDialog!!.setCancelable(false)
        this.mDialog!!.show()
    }

    override fun doInBackground(vararg params: Void): List<DLayerInfo>? {
        try {
            getCapabilities(mApplication)
//            if (isAccess(mApplication)) {
            getAppInfo(mApplication)
            val layerInfos = getLayerInfo(mApplication)
            return layerInfos
//            } else {
//                handler.post {
//                    postExecute()
//                    delegate.post(null)
//                }
//            }
//            return layerInfos
        } catch (e: Exception) {
            Log.e("Lỗi lấy danh sách DMA", e.toString())
        }
        return null
    }

    override fun onProgressUpdate(vararg values: Void) {
        super.onProgressUpdate(*values)


    }

    override fun onPostExecute(value: List<DLayerInfo>?) {
        //        if (khachHang != null) {
        mDialog!!.dismiss()
        this.mDelegate.processFinish(value)
        //        }
    }

    private fun getCapabilities(application: DApplication) {
        try {
            val url = URL(Constant.API_URL.CAPABILITIES)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.doOutput = false
                conn.requestMethod = Constant.HTTPRequest.GET_METHOD
                conn.setRequestProperty(Constant.HTTPRequest.AUTHORIZATION, "Bearer " + application.user!!.accessToken)
                conn.connect()

                val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
                val builder = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    builder.append(line)
                }
                application.user!!.capability = parseStringArray(builder.toString())!![0]
            } catch (e: Exception) {
                Log.e("error", e.toString())
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.e("Lỗi lấy LayerInfo", e.toString())
        }
    }

    private fun getAppInfo(application: DApplication) {
        try {
            val url = URL(Constant.API_URL.APP_INFO + application.user!!.capability)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.doOutput = false
                conn.requestMethod = Constant.HTTPRequest.GET_METHOD
                conn.setRequestProperty(Constant.HTTPRequest.AUTHORIZATION, "Bearer " + application.user!!.accessToken)
                conn.connect()

                val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
                val builder = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    builder.append(line)
                }
                application.appInfo = parseAppInfo(builder.toString())
            } catch (e: Exception) {
                Log.e("error", e.toString())
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.e("Lỗi lấy LayerInfo", e.toString())
        }
    }

    private fun getLayerInfo(application: DApplication): List<DLayerInfo>? {
        try {
            val API_URL = Constant.API_URL.LAYER_INFO
            val url = URL(API_URL)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.doOutput = false
                conn.requestMethod = Constant.HTTPRequest.GET_METHOD
                conn.setRequestProperty(Constant.HTTPRequest.AUTHORIZATION, "Bearer " + application.user!!.accessToken)
                conn.connect()

                val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
                val builder = StringBuilder()
                var line: String?
                while (bufferedReader.readLine().also { line = it } != null) {
                    builder.append(line)
                }
                return parseLayerInfo(builder.toString())
            } catch (e: Exception) {
                Log.e("error", e.toString())
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.e("Lỗi lấy LayerInfo", e.toString())
        }
        return listOf()
    }

    @Throws(JSONException::class)
    private fun parseLayerInfo(data: String?): List<DLayerInfo>? {
        val outputType = object : TypeToken<List<DLayerInfo>>() {}.type
        val gson = Gson()
        val list: List<DLayerInfo> = gson.fromJson(data, outputType)
        return list
    }
    @Throws(JSONException::class)
    private fun parseStringArray(data: String?): Array<String>? {
        val outputType = object : TypeToken<Array<String>>() {}.type
        val gson = Gson()
        val array: Array<String> = gson.fromJson(data, outputType)
        return array
    }
    @Throws(JSONException::class)
    private fun parseAppInfo(data: String?): DAppInfo? {
        val outputType = object : TypeToken<DAppInfo>() {}.type
        val gson = Gson()
        val dAppInfo: DAppInfo = gson.fromJson(data, outputType)
        return dAppInfo
    }

}
