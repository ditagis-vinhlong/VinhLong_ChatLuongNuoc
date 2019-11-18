package vinhlong.ditagis.com.qlcln.async

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import vinhlong.ditagis.com.qlcln.MainActivity
import java.util.*

/**
 * Created by ThanLe on 4/16/2018.
 */

class QueryFeatureAsync(mainActivity: MainActivity, private val serviceFeatureTable: ServiceFeatureTable, asyncResponse: AsyncResponse) : AsyncTask<QueryParameters, List<Feature>, Void>() {
    private val dialog: ProgressDialog?
    private val mContext: Context

    private var mDelegate: AsyncResponse? = null


    init {
        this.mDelegate = asyncResponse
        mContext = mainActivity
        dialog = ProgressDialog(mainActivity, android.R.style.Theme_Material_Dialog_Alert)
    }

    interface AsyncResponse {
        fun processFinish(o: List<Feature>)
    }


    override fun onPreExecute() {
        super.onPreExecute()
        dialog!!.setMessage("Đang truy vấn...")
        dialog.setCancelable(false)
        dialog.show()

    }

    override fun doInBackground(vararg params: QueryParameters): Void? {
        val features = ArrayList<Feature>()
        val queryParameters = params[0]
        val queryResultListenableFuture = serviceFeatureTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
        queryResultListenableFuture.addDoneListener {
            try {
                val result = queryResultListenableFuture.get()
                val iterator = result.iterator()

                while (iterator.hasNext()) {
                    val feature = iterator.next() as Feature
                    features.add(feature)
                }
                publishProgress(features)

            } catch (e: Exception) {
                publishProgress(features)
            }
        }
        return null
    }

    override fun onProgressUpdate(vararg values: List<Feature>) {
        super.onProgressUpdate(*values)
        if (dialog != null && dialog.isShowing) dialog.dismiss()
        if (values != null) {
            mDelegate!!.processFinish(values[0])

        } else {
            mDelegate!!.processFinish(ArrayList())
        }
    }

}

