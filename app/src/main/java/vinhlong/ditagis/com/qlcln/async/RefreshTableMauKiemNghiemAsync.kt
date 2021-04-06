package vinhlong.ditagis.com.qlcln.async

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.QueryParameters
import com.esri.arcgisruntime.data.ServiceFeatureTable
import vinhlong.ditagis.com.qlcln.R
import vinhlong.ditagis.com.qlcln.adapter.MauKiemNghiemApdapter
import vinhlong.ditagis.com.qlcln.libs.Action
import vinhlong.ditagis.com.qlcln.utities.Constant
import java.util.*
import java.util.concurrent.ExecutionException

/**
 * Created by ThanLe on 4/16/2018.
 */

class RefreshTableMauKiemNghiemAsync(private val mContext: Context, private val table_thoigiancln: ServiceFeatureTable, private val mauKiemNghiemApdapter: MauKiemNghiemApdapter, private val action: Action, asyncResponse: AsyncResponse) : AsyncTask<String, List<MauKiemNghiemApdapter.MauKiemNghiem>, Void?>() {
    private val dialog: ProgressDialog?

    private var delegate: AsyncResponse? = null

    interface AsyncResponse {
        fun processFinish(features: List<Feature>, thoiGianChatLuongNuocs: List<MauKiemNghiemApdapter.MauKiemNghiem>)
    }

    init {
        this.delegate = asyncResponse
        dialog = ProgressDialog(mContext, android.R.style.Theme_Material_Dialog_Alert)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        dialog!!.setMessage(mContext.getString(R.string.async_dang_xu_ly))
        dialog.setCancelable(false)
        dialog.show()

    }

    override fun doInBackground(vararg params: String): Void? {
        val features = ArrayList<Feature>()
        val mauKiemNghiems = ArrayList<MauKiemNghiemApdapter.MauKiemNghiem>()
        val queryParameters = QueryParameters()
        val queryClause = "IDDiemDanhGia = '" + params[0] + "'"
        queryParameters.whereClause = queryClause
        val queryResultListenableFuture = table_thoigiancln.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
        queryResultListenableFuture.addDoneListener {
            try {
                val result = queryResultListenableFuture.get()
                val iterator = result.iterator()

                while (iterator.hasNext()) {
                    val feature = iterator.next() as Feature
                    features.add(feature)
                    feature.attributes["OBJECTID"]
                    val mauKiemNghiem = MauKiemNghiemApdapter.MauKiemNghiem()
                    mauKiemNghiem.objectid = (feature.attributes["OBJECTID"].toString())
                    mauKiemNghiem.idMauKiemNghiem = getValueAttributes(feature, Constant.FieldMauKiemNghiem.ID)
                    mauKiemNghiem.tenMau = getValueAttributes(feature, mContext.getString(R.string.TENMAU))
                    mauKiemNghiem.isView = action.isView
                    mauKiemNghiems.add(mauKiemNghiem)

                }
                delegate!!.processFinish(features, mauKiemNghiems)
                publishProgress(mauKiemNghiems)

            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }
        return null
    }

    private fun getValueAttributes(feature: Feature, fieldName: String): String? {
        return if (feature.attributes[fieldName] != null) feature.attributes[fieldName].toString() else null
    }


    override fun onProgressUpdate(vararg values: List<MauKiemNghiemApdapter.MauKiemNghiem>) {
        mauKiemNghiemApdapter.clear()
        mauKiemNghiemApdapter.setMauKiemNghiems(values.first() as MutableList<MauKiemNghiemApdapter.MauKiemNghiem>)
        mauKiemNghiemApdapter.notifyDataSetChanged()
        if (dialog != null && dialog.isShowing) dialog.dismiss()
        super.onProgressUpdate(*values)

    }

}

