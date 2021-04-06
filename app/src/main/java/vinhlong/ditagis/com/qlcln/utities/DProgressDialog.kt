package vinhlong.ditagis.com.qlcln.utities

import android.app.Activity
import android.app.Dialog
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.android.synthetic.main.layout_progress_dialog.view.*
import vinhlong.ditagis.com.qlcln.R

class DProgressDialog {
    private var mDialog: Dialog? = null
    private var mLayout: LinearLayout? = null

    fun show(activity: Activity, view: ViewGroup, title: String, isCancelable: Boolean = false) {
        mDialog = BottomSheetDialog(activity)
        mLayout = activity.layoutInflater.inflate(
                R.layout.layout_progress_dialog,
                view,
                false
        ) as LinearLayout
        (mLayout!!.txt_progress_dialog_title as TextView).text = title

        mDialog!!.setCancelable(isCancelable)
        mDialog!!.setContentView(mLayout!!)


        if (!activity.isFinishing && mDialog != null && mDialog!!.isShowing)
            mDialog!!.dismiss()
        mDialog!!.show()
    }

    fun changeTitle(activity: Activity, view: ViewGroup, title: String) {
        if (mDialog != null && mDialog!!.isShowing) {
            (mLayout!!.txt_progress_dialog_title as TextView).text = title
        } else {
            show(activity, view, title)
        }
    }

    fun dismiss(activity: Activity) {
        if (!activity.isFinishing && mDialog != null && mDialog!!.isShowing)
            mDialog!!.dismiss()
    }
}
