package vinhlong.ditagis.com.qlcln.async

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import vinhlong.ditagis.com.qlcln.R
import vinhlong.ditagis.com.qlcln.entities.entitiesDB.LayerInfoDTG
import vinhlong.ditagis.com.qlcln.entities.entitiesDB.ListObjectDB
import vinhlong.ditagis.com.qlcln.utities.Constant
import vinhlong.ditagis.com.qlcln.utities.Preference
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.*


class PreparingAsycn(private val mContext: Context, private val mDelegate: AsyncResponse) : AsyncTask<Void, Void, Void?>() {
    private var mDialog: ProgressDialog? = null

    interface AsyncResponse {
        fun processFinish(output: Void?)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        this.mDialog = ProgressDialog(this.mContext, android.R.style.Theme_Material_Dialog_Alert)
        this.mDialog!!.setMessage(mContext.getString(R.string.preparing))
        this.mDialog!!.setCancelable(false)
        this.mDialog!!.show()
    }

    override fun doInBackground(vararg params: Void): Void? {
        try {
            val url = URL(Constant.API_URL.LAYER_INFO)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.doOutput = false
                conn.requestMethod = "GET"
                conn.setRequestProperty("Authorization", Preference.instance.loadPreference(mContext.getString(R.string.preference_login_api)))
                conn.connect()

                val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
                val buffer = StringBuffer()
                var line: String?
                while (true) {
                    line = bufferedReader.readLine()
                    if (line == null)
                        break
                    buffer.append(line)
                }
                pajsonRouteeJSon(buffer.toString())
            } catch (e: Exception) {
                Log.e("error", e.toString())
            } finally {
                conn.disconnect()
            }
            //            ListFeatureLayerDTGDB listFeatureLayerDTGDB = new ListFeatureLayerDTGDB(mContext);
            //            ListObjectDB.getInstance().setLstFeatureLayerDTG(listFeatureLayerDTGDB.find(Preference.getInstance().loadPreference(
            //                    mContext.getString(R.string.preference_username)
            //            )));
        } catch (e: Exception) {
            Log.e("Lỗi lấy danh sách DMA", e.toString())
        }

        return null
    }

    override fun onProgressUpdate(vararg values: Void) {
        super.onProgressUpdate(*values)


    }

    override fun onPostExecute(value: Void?) {
        //        if (khachHang != null) {
        mDialog!!.dismiss()
        this.mDelegate.processFinish(value)
        //        }
    }

    @Throws(JSONException::class)
    private fun pajsonRouteeJSon(data: String?) {
        if (data == null)
            return
        val myData = "{ \"layerInfo\": $data}"
        val jsonData = JSONObject(myData)
        val jsonRoutes = jsonData.getJSONArray("layerInfo")
        val layerDTGS = ArrayList<LayerInfoDTG>()
        for (i in 0 until jsonRoutes.length()) {
            val jsonRoute = jsonRoutes.getJSONObject(i)


            //           LayerInfoDTG layerInfoDTG = new LayerInfoDTG();
            layerDTGS.add(LayerInfoDTG(jsonRoute.getString(mContext.getString(R.string.sql_coloumn_sys_id)),
                    jsonRoute.getString(mContext.getString(R.string.sql_coloumn_sys_title)),
                    jsonRoute.getString(mContext.getString(R.string.sql_coloumn_sys_url)),
                    jsonRoute.getBoolean(mContext.getString(R.string.sql_coloumn_sys_iscreate)), jsonRoute.getBoolean(mContext.getString(R.string.sql_coloumn_sys_isdelete)),
                    jsonRoute.getBoolean(mContext.getString(R.string.sql_coloumn_sys_isedit)), jsonRoute.getBoolean(mContext.getString(R.string.sql_coloumn_sys_isview)),
                    jsonRoute.getString(mContext.getString(R.string.sql_coloumn_sys_outfield)), jsonRoute.getString(mContext.getString(R.string.sql_coloumn_sys_definition))))


        }
        ListObjectDB.getInstance().lstFeatureLayerDTG = layerDTGS

    }

}
