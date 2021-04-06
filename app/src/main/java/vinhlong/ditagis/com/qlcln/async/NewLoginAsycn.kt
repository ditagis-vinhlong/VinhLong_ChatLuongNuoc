package vinhlong.ditagis.com.qlcln.async

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.json.JSONException
import org.json.JSONObject
import vinhlong.ditagis.com.qlcln.R
import vinhlong.ditagis.com.qlcln.entities.DApplication
import vinhlong.ditagis.com.qlcln.entities.User
import vinhlong.ditagis.com.qlcln.utities.Constant
import vinhlong.ditagis.com.qlcln.utities.Preference
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

class NewLoginAsycn(activity: Activity, private val mDelegate: AsyncResponse) : AsyncTask<String, Void, Any?>() {
    private val exception: Exception? = null
    private var mDialog: ProgressDialog? = null
    private val mContext: Context
    private val mApplication: DApplication = activity.application as DApplication


    interface AsyncResponse {
        fun processFinish(output: Any?)
    }

    init {
        this.mContext = activity
    }

    override fun onPreExecute() {
        super.onPreExecute()
        this.mDialog = ProgressDialog(this.mContext, android.R.style.Theme_Material_Dialog_Alert)
        this.mDialog!!.setMessage(mContext.getString(R.string.connect_message))
        this.mDialog!!.setCancelable(false)
        this.mDialog!!.show()
    }

    override fun doInBackground(vararg params: String): Any? {
        val userName = params[0]
        val pin = params[1]
        var conn: HttpURLConnection? = null
        try {
            val user = getUser(userName!!, pin!!)
            mApplication.user = user
        } catch (e: Exception) {
            Log.e("Lỗi đăng nhập", e.toString())
        } finally {
            conn?.disconnect()
        }
        return null

    }


    override fun onPostExecute(user: Any?) {
        mDialog!!.dismiss()
        this.mDelegate.processFinish(user)
    }

    private fun getUser(username: String, password: String): User? {
        var conn: HttpURLConnection? = null
        try {
            val API_URL = Constant.API_URL.LOGIN
            val url = URL(API_URL)
            conn = url.openConnection() as HttpURLConnection
            conn!!.doOutput = true
            conn.instanceFollowRedirects = false
            conn.requestMethod = Constant.HTTPRequest.POST_METHOD
            val cred = JSONObject()
            cred.put("username", username)
            cred.put("password", password)
            conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
            conn.setRequestProperty("Accept", "application/json")
            conn.useCaches = false
            val wr = OutputStreamWriter(conn.outputStream)
            wr.write(cred.toString())
            wr.flush()
            conn.connect()
            val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
            val stringBuilder = StringBuilder()
            var line: String?
            while (true) {
                line = bufferedReader.readLine()
                if (line == null)
                    break
                stringBuilder.append(line)

            }

            bufferedReader.close()
            conn.disconnect()
            return parseUser(stringBuilder.toString())
        } catch (e: Exception) {
            Log.e("Lỗi đăng nhập", e.toString())
        } finally {
            conn?.disconnect()
        }
        return null
    }

    private fun parseUser(data: String?): User {
        val userType = object : TypeToken<User>() {}.type
        return Gson().fromJson(data, userType)
    }
}