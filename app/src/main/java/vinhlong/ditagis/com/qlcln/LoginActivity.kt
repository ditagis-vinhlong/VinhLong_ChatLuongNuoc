package vinhlong.ditagis.com.qlcln

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login.*
import vinhlong.ditagis.com.qlcln.async.CheckForUpdateAsync
import vinhlong.ditagis.com.qlcln.async.NewLoginAsycn
import vinhlong.ditagis.com.qlcln.entities.DApplication
import vinhlong.ditagis.com.qlcln.entities.UpdateInfo
import vinhlong.ditagis.com.qlcln.utities.CheckConnectInternet.isOnline
import vinhlong.ditagis.com.qlcln.utities.Constant
import vinhlong.ditagis.com.qlcln.utities.Preference


class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private var mTxtUsername: TextView? = null
    private var mTxtPassword: TextView? = null
    private var isLastLogin: Boolean = false
    private var mTxtValidation: TextView? = null
    private var mApplication: DApplication? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        val btnLogin = findViewById<Button>(R.id.btnLogin)
        btnLogin.setOnClickListener(this)
        findViewById<View>(R.id.txt_login_changeAccount).setOnClickListener(this)

        mTxtUsername = findViewById(R.id.txtUsername)
        mTxtPassword = findViewById(R.id.txtPassword)
//        mTxtUsername!!.text = "test_cln"
//        mTxtPassword!!.text = "test_cln"
        mApplication = application as DApplication
        mTxtValidation = findViewById(R.id.txt_login_validation)
        val versionCode = packageManager.getPackageInfo(packageName, 0).versionCode
        txt_version.text = "Phiên bản: $versionCode"

        CheckForUpdateAsync(this, object : CheckForUpdateAsync.AsyncResponse {
            override fun processFinish(output: UpdateInfo?) {
                if (output != null) {
                    if (versionCode < output.Version!!) {
                        var builder = AlertDialog.Builder(this@LoginActivity)
                        builder.setTitle("Bạn có muốn cập nhật lên phiên bản ${output.Version} không?")
                        builder.setMessage("Nội dung cập nhật:\n${output.Info}")
                        builder.setCancelable(false)
                        builder.setNegativeButton("CẬP NHẬT") { dialog, which -> goURLBrowser(output.LinkApp!!) }
                        builder.setPositiveButton("BỎ QUA") { dialog, _ -> dialog.dismiss() }
                        val dialog = builder.create()
                        dialog.show()

                    }
                } else
                    create()
            }
        }).execute()
    }

    private fun create() {
        Preference.instance.setContext(this)
        val username = Preference.instance!!.loadPreference(Constant.PreferenceKey.USERNAME)
        val password = Preference.instance!!.loadPreference(Constant.PreferenceKey.PASSWORD)
        //nếu chưa từng đăng nhập thành công trước đó
        //nhập username và password bình thường
        if (!username.isNullOrEmpty() && !password.isNullOrEmpty()) {
            mTxtUsername!!.setText(username)
            mTxtPassword!!.setText(password)
//            login()
        }
    }

    private fun goURLBrowser(url: String) {
        var url = url
        var result = false
        if (!url.startsWith("http://") && !url.startsWith("https://")) url = "http://$url"
        val webpage = Uri.parse(url)
        val intent = Intent(Intent.ACTION_VIEW, webpage)
        try {
            startActivity(intent)
            result = true
        } catch (ignored: Exception) {
        }
    }

    private fun login() {
        if (!isOnline(this)) {
            mTxtValidation!!.setText(R.string.validate_no_connect)
            mTxtValidation!!.visibility = View.VISIBLE
            return
        }
        mTxtValidation!!.visibility = View.GONE
        val userName: String?
        userName = mTxtUsername!!.text.toString().trim { it <= ' ' }
        val passWord = mTxtPassword!!.text.toString().trim { it <= ' ' }
        if (userName!!.isEmpty() || passWord.isEmpty()) {
            handleInfoLoginEmpty()
            return
        }
        NewLoginAsycn(this,
                object : NewLoginAsycn.AsyncResponse {
                    override fun processFinish(output: Any?) {
                        if (mApplication!!.user != null) handleLoginSuccess(userName, passWord) else handleLoginFail()
                    }
                }).execute(userName, passWord)
    }

    private fun handleInfoLoginEmpty() {
        mTxtValidation!!.setText(R.string.info_login_empty)
        mTxtValidation!!.visibility = View.VISIBLE
    }

    private fun handleLoginFail() {
        mTxtValidation!!.setText(R.string.validate_login_fail)
        mTxtValidation!!.visibility = View.VISIBLE
    }

    private fun handleLoginSuccess(username: String, password: String) {


        Preference.instance.savePreferences(Constant.PreferenceKey.USERNAME, username)
        Preference.instance.savePreferences(Constant.PreferenceKey.PASSWORD, password)
        mTxtUsername!!.text = ""
        mTxtPassword!!.text = ""
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun changeAccount() {
        mTxtUsername!!.text = ""
        mTxtPassword!!.text = ""

        create()
    }

    override fun onPostResume() {
        super.onPostResume()
        create()
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.btnLogin -> login()
            R.id.txt_login_changeAccount -> changeAccount()
        }

    }

    override fun onKeyUp(keyCode: Int, event: KeyEvent): Boolean {
        when (keyCode) {
            KeyEvent.KEYCODE_ENTER -> {
                if (mTxtPassword!!.text.toString().trim { it <= ' ' }.length > 0) {
                    login()
                    return true
                }
                return super.onKeyUp(keyCode, event)
            }
            else -> return super.onKeyUp(keyCode, event)
        }
    }
}
