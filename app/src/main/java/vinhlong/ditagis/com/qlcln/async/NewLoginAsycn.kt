package vinhlong.ditagis.com.qlcln.async

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.util.Log
import org.json.JSONException
import org.json.JSONObject
import vinhlong.ditagis.com.qlcln.R
import vinhlong.ditagis.com.qlcln.entities.DApplication
import vinhlong.ditagis.com.qlcln.entities.entitiesDB.User
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

    private val displayName: String
        get() {
            val displayName = ""
            try {
                val url = URL(Constant.API_URL.DISPLAY_NAME)
                val conn = url.openConnection() as HttpURLConnection
                try {
                    conn.doOutput = false
                    conn.requestMethod = "GET"
                    conn.setRequestProperty("Authorization", Preference.instance.loadPreference(mContext.getString(R.string.preference_login_api)))
                    conn.connect()

                    val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
                    val line = bufferedReader.readLine()
                    if (line != null) {
                        return pajsonRouteeJSon(line)
                    } else {

                    }

                } catch (e: Exception) {
                    Log.e("error", e.toString())
                } finally {
                    conn.disconnect()
                }
            } catch (e: Exception) {
                Log.e("error", e.toString())
            } finally {
                return displayName
            }
        }

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
        //        String passEncoded = (new EncodeMD5()).encode(pin + "_DITAGIS");
        // Do some validation here
        try {
            //            + "&apiKey=" + API_KEY
            val url = URL(Constant.API_URL.LOGIN)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.requestMethod = "POST"
                val cred = JSONObject()
                cred.put("Username", userName)
                cred.put("Password", pin)

                conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8")
                conn.setRequestProperty("Accept", "application/json")
                conn.useCaches = false
                val wr = OutputStreamWriter(conn.outputStream)
                wr.write(cred.toString())
                wr.flush()
                conn.connect()
                val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
                val stringBuilder = StringBuilder()
                val line = bufferedReader.readLine()
                if (line != null) {
                    stringBuilder.append(line)
                }
                bufferedReader.close()
                val token = stringBuilder.toString().replace("\"", "")
                Preference.instance.savePreferences(mContext.getString(R.string.preference_login_api), token)
                if (checkAccess()!!) {
                    val user = User()
                    user.displayName = displayName
                    user.userName = userName
                    user.passWord = pin

                    conn.disconnect()
                    return user
                } else {
                    conn.disconnect()
                    return null
                }

            }catch (e: Exception){

                return e.toString()
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.e("ERROR", e.message, e)
            return null
        }

    }


    override fun onPostExecute(user: Any?) {
        mDialog!!.dismiss()
        this.mDelegate.processFinish(user)
    }

    private fun checkAccess(): Boolean? {
        var isAccess = false
        try {
            val url = URL(Constant.API_URL.IS_ACCESS)
            val conn = url.openConnection() as HttpURLConnection
            try {
                conn.doOutput = false
                conn.requestMethod = "GET"
                conn.setRequestProperty("Authorization", Preference.instance.loadPreference(mContext.getString(R.string.preference_login_api)))
                conn.connect()

                val bufferedReader = BufferedReader(InputStreamReader(conn.inputStream))
                val line = bufferedReader.readLine()
                if (line == "true")
                    isAccess = true

            } catch (e: Exception) {
                Log.e("error", e.toString())
            } finally {
                conn.disconnect()
            }
        } catch (e: Exception) {
            Log.e("error", e.toString())
        } finally {
            return isAccess
        }
    }

    @Throws(JSONException::class)
    private fun pajsonRouteeJSon(data: String?): String {
        if (data != null) {
            val myData = "{ \"account\": [$data]}"
            val jsonData = JSONObject(myData)
            val jsonRoutes = jsonData.getJSONArray("account")
            //        jsonData.getJSONArray("account");
            for (i in 0 until jsonRoutes.length()) {
                val jsonRoute = jsonRoutes.getJSONObject(i)
                val displayName = jsonRoute.getString(mContext.getString(R.string.sql_coloumn_login_displayname))
                val username = jsonRoute.getString(mContext.getString(R.string.sql_coloumn_login_username))
                return displayName
            }
        }
        return ""
    }
}