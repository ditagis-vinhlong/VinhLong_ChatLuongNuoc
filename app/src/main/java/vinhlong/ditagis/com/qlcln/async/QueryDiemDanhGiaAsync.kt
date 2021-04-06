package vinhlong.ditagis.com.qlcln.async

import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import android.widget.TextView
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import vinhlong.ditagis.com.qlcln.R
import vinhlong.ditagis.com.qlcln.adapter.DanhSachDiemDanhGiaAdapter
import java.util.*

/**
 * Created by ThanLe on 4/16/2018.
 */

class QueryDiemDanhGiaAsync : AsyncTask<String, List<Feature>, Void?> {
    private var dialog: ProgressDialog? = null
    private var mContext: Context? = null
    private var serviceFeatureTable: ServiceFeatureTable? = null
    private var danhSachDiemDanhGiaAdapter: DanhSachDiemDanhGiaAdapter? = null
    private var txtTongItem: TextView? = null

    private var delegate: AsyncResponse? = null


    constructor(mainActivity: Activity, serviceFeatureTable: ServiceFeatureTable, txtTongItem: TextView, adapter: DanhSachDiemDanhGiaAdapter, asyncResponse: AsyncResponse) {
        this.delegate = asyncResponse
        mContext = mainActivity
        this.serviceFeatureTable = serviceFeatureTable
        this.danhSachDiemDanhGiaAdapter = adapter
        this.txtTongItem = txtTongItem
        dialog = ProgressDialog(mainActivity, android.R.style.Theme_Material_Dialog_Alert)
    }

    interface AsyncResponse {
        fun processFinish(features: List<Feature>)
    }


    override fun onPreExecute() {
        super.onPreExecute()
        dialog!!.setMessage(mContext!!.getString(R.string.async_dang_xu_ly))
        dialog!!.setCancelable(false)
        dialog!!.show()

    }

    override fun doInBackground(vararg params: String): Void? {
        val features = ArrayList<Feature>()
        val queryParameters = QueryParameters()
        val queryClause = params[0]
        queryParameters.whereClause = queryClause
        val queryResultListenableFuture = serviceFeatureTable!!.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
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
        danhSachDiemDanhGiaAdapter!!.clear()
        danhSachDiemDanhGiaAdapter!!.setItems(values.first() as MutableList<Feature>)
        danhSachDiemDanhGiaAdapter!!.notifyDataSetChanged()
        if (txtTongItem != null)
            txtTongItem!!.text = mContext!!.getString(R.string.nav_thong_ke_tong_diem) + values[0].size
        if (dialog != null && dialog!!.isShowing) dialog!!.dismiss()
        super.onProgressUpdate(*values)

    }


}

