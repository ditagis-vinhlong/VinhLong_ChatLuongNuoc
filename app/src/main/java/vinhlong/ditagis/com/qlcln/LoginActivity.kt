package vinhlong.ditagis.com.qlcln

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_login.*
import vinhlong.ditagis.com.qlcln.async.NewLoginAsycn
import vinhlong.ditagis.com.qlcln.entities.DApplication
import vinhlong.ditagis.com.qlcln.entities.entitiesDB.User
import vinhlong.ditagis.com.qlcln.utities.CheckConnectInternet
import vinhlong.ditagis.com.qlcln.utities.Preference


class LoginActivity : AppCompatActivity(), View.OnClickListener {
    private var mTxtUsername: TextView? = null
    private var mTxtPassword: TextView? = null
    private var isLastLogin: Boolean = false
    private var mTxtValidation: TextView? = null
    private var dApplication: DApplication? = null

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
        dApplication = application as DApplication
        mTxtValidation = findViewById(R.id.txt_login_validation)
        create()
    }

    private fun create() {
        Preference.instance.setContext(this)
        val preference_userName = Preference.instance.loadPreference(getString(R.string.preference_username))

        //nếu chưa từng đăng nhập thành công trước đó
        //nhập username và password bình thường
        if (preference_userName == null || preference_userName!!.isEmpty()) {
            findViewById<View>(R.id.layout_login_tool).visibility = View.GONE
            findViewById<View>(R.id.layout_login_username).visibility = View.VISIBLE
            isLastLogin = false
        } else {
            isLastLogin = true
            findViewById<View>(R.id.layout_login_tool).visibility = View.VISIBLE
            findViewById<View>(R.id.layout_login_username).visibility = View.GONE
        }//ngược lại
        //chỉ nhập pasword

    }

    private fun login() {
        if (!CheckConnectInternet.isOnline(this)) {
            mTxtValidation!!.setText(R.string.validate_no_connect)
            mTxtValidation!!.visibility = View.VISIBLE
            return
        }
        mTxtValidation!!.visibility = View.GONE

        val userName: String?
        if (isLastLogin)
            userName = Preference.instance.loadPreference(getString(R.string.preference_username))
        else
            userName = mTxtUsername!!.text.toString().trim { it <= ' ' }
        val passWord = mTxtPassword!!.text.toString().trim { it <= ' ' }
        if (userName!!.length == 0 || passWord.length == 0) {
            handleInfoLoginEmpty()
            return
        }
        //        handleLoginSuccess(userName,passWord);
        val finalUserName = userName
        val loginAsycn = NewLoginAsycn(this, object : NewLoginAsycn.AsyncResponse {
            override fun processFinish(output: Any?) {

                if (output != null) {
                    if (output is User) {
                        handleLoginSuccess(output)
                        dApplication!!.user = output
                    }
                    else if(output is String){
//                        Snackbar.make(btnLogin, output, Snackbar.LENGTH_LONG).show()
                        handleLoginFail()
                    }
                } else
                    handleLoginFail()
            }
        })
        loginAsycn.execute(userName, passWord)
    }

    private fun handleInfoLoginEmpty() {
        mTxtValidation!!.setText(R.string.info_login_empty)
        mTxtValidation!!.visibility = View.VISIBLE
    }

    private fun handleLoginFail() {
        mTxtValidation!!.setText(R.string.validate_login_fail)
        mTxtValidation!!.visibility = View.VISIBLE
    }

    private fun handleLoginSuccess(user: User) {


        Preference.instance.savePreferences(getString(R.string.preference_username), user.userName!!)
        Preference.instance.savePreferences(getString(R.string.preference_password), user.passWord!!)
        Preference.instance.savePreferences(getString(R.string.preference_displayname), user.displayName!!)
        mTxtUsername!!.text = ""
        mTxtPassword!!.text = ""
        val intent = Intent()
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun changeAccount() {
        mTxtUsername!!.text = ""
        mTxtPassword!!.text = ""

        Preference.instance.savePreferences(getString(R.string.preference_username), "")
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
