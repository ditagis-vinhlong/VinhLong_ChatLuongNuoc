package vinhlong.ditagis.com.qlcln.async

import android.R
import android.app.Activity
import android.app.ProgressDialog
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Build
import androidx.annotation.RequiresApi
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.tasks.geocode.LocatorTask
import vinhlong.ditagis.com.qlcln.entities.DApplication
import vinhlong.ditagis.com.qlcln.utities.Constant
import vinhlong.ditagis.com.qlcln.utities.DAlertDialog
import vinhlong.ditagis.com.qlcln.utities.DBitmap
import java.text.SimpleDateFormat
import java.util.*

class AddFeatureAsync(val mActivity: Activity, val mBitmaps: ArrayList<Bitmap>,
                      val mServiceFeatureTable: ServiceFeatureTable, val mAddress: String, val mDelegate: AsyncResponse) : AsyncTask<Point, Any, Void?>() {
    private val mDialog: ProgressDialog?
    private val mApplication = mActivity.application as DApplication
    private var loc = LocatorTask("http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer")

    init {
        mDialog = ProgressDialog(mActivity, R.style.Theme_Material_Dialog_Alert)
    }

    interface AsyncResponse {
        fun processFinish(o: Any)
    }

    private val dateString: String
        @RequiresApi(api = Build.VERSION_CODES.N)
        get() {
            val timeStamp = Constant.DATE_FORMAT.format(Calendar.getInstance().time)

            val writeDate = SimpleDateFormat("dd_MM_yyyy HH:mm:ss")
            writeDate.timeZone = TimeZone.getTimeZone("GMT+07:00")
            return writeDate.format(Calendar.getInstance().time)
        }

    private val timeID: String
        get() = Constant.DDMMYYYY.format(Calendar.getInstance().time)

    override fun onPreExecute() {
        super.onPreExecute()
        mDialog!!.setMessage("Đang xử lý...")
        mDialog.setCancelable(false)
        mDialog.show()
    }

    override fun doInBackground(vararg params: Point): Void? {
        val clickPoint = params[0]
        val feature = mServiceFeatureTable.createFeature()
        feature.geometry = clickPoint

                    feature.attributes[Constant.DIACHI] = mAddress

                var searchStr = ""
                var dateTime = ""
                var timeID = ""
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    dateTime = dateString
                    timeID = timeID
                    searchStr = Constant.IDDIEM_DANH_GIA + " like '%" + timeID + "'"
                }
                val queryParameters = QueryParameters()
                queryParameters.whereClause = searchStr
                val featureQuery = mServiceFeatureTable.queryFeaturesAsync(queryParameters)
                val finalDateTime = dateTime
                val finalTimeID = timeID


                featureQuery.addDoneListener {
                    var queryParameters = QueryParameters()
                    queryParameters.geometry = clickPoint
                    var listenableFuture = mApplication.serviceFeatureTableHanhChinh!!.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
                    listenableFuture.addDoneListener {
                        try {
                            var featureQueryResult = listenableFuture.get()
                            val iterator = featureQueryResult.iterator()

                            while (iterator.hasNext()) {
                                val featureHanhChinh = iterator.next() as Feature
                                feature.attributes[Constant.FieldChatLuongNuoc.MA_HUYEN] = featureHanhChinh.attributes[Constant.FieldHanhChinh.MA_HUYEN]
                                feature.attributes[Constant.FieldChatLuongNuoc.MA_XA] = featureHanhChinh.attributes[Constant.FieldHanhChinh.ID_HANH_CHINH]
                            }
                            addFeatureAsync(featureQuery, feature, finalTimeID, finalDateTime)
                        }
                        catch (e: Exception){
//                            publishProgress(e.toString())
                            addFeatureAsync(featureQuery, feature, finalTimeID, finalDateTime)
                        }
                    }


                }


        return null
    }

    private fun notifyError() {
        DAlertDialog().show(mActivity, "Đã xảy ra lỗi")


    }

    private fun addFeatureAsync(featureQuery: ListenableFuture<FeatureQueryResult>, feature: Feature, finalTimeID: String, finalDateTime: String) {
        try {
            // lấy stt_id lớn nhất
            var id_tmp: Int
            var stt_id = 0
            val result = featureQuery.get()
            val iterator = result.iterator()
            while (iterator.hasNext()) {
                val item = iterator.next() as Feature
                id_tmp = Integer.parseInt(item.attributes[Constant.IDDIEM_DANH_GIA].toString().split("_".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()[0])
                if (id_tmp > stt_id) stt_id = id_tmp
            }
            stt_id++
            if (stt_id < 10) {
                feature.attributes[Constant.IDDIEM_DANH_GIA] = "0" + stt_id + "_" + finalTimeID
            } else
                feature.attributes[Constant.IDDIEM_DANH_GIA] = stt_id.toString() + "_" + finalTimeID

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                val c = Calendar.getInstance()
                feature.attributes[Constant.NGAY_CAP_NHAT] = c
            }
            val mapViewResult = mServiceFeatureTable.addFeatureAsync(feature)
            mapViewResult.addDoneListener {
                val listListenableEditAsync = mServiceFeatureTable.applyEditsAsync()
                listListenableEditAsync.addDoneListener {
                    try {
                        val featureEditResults = listListenableEditAsync.get()
                        if (featureEditResults.size > 0) {
                            val objectId = featureEditResults[0].objectId
                            val queryParameters = QueryParameters()
                            val query = "OBJECTID = $objectId"
                            queryParameters.whereClause = query
                            val feature1 = mServiceFeatureTable.queryFeaturesAsync(queryParameters)
                            feature1.addDoneListener { addAttachment(feature1) }
                        } else publishProgress("Thêm điểm thất bại!")
                    } catch (e: Exception) {
                        publishProgress(e.toString())
                    }


                }
            }
        } catch (e: Exception) {
            publishProgress(e.toString())
        }

    }

    private fun addAttachment(feature: ListenableFuture<FeatureQueryResult>) {
        var result: FeatureQueryResult? = null
        try {
            result = feature.get()
            if (result!!.iterator().hasNext()) {
                val arcGISFeature = result.iterator().next() as ArcGISFeature
                var added: Int = 0
                mBitmaps.forEach { bitmap ->
                    run {
                        val attachmentName = String.format(Constant.AttachmentName.ADD, mApplication.user!!.userName, System.currentTimeMillis())
                        val addResult = arcGISFeature!!.addAttachmentAsync(DBitmap().getByteArray(bitmap), Bitmap.CompressFormat.PNG.toString(), attachmentName)
                        addResult.addDoneListener {
                            try {
                                val attachment = addResult.get()
                                added ++
                                if( added == mBitmaps.size){
                                    val tableResult = mServiceFeatureTable.updateFeatureAsync(arcGISFeature!!)
                                    tableResult.addDoneListener {
                                        val updatedServerResult = mServiceFeatureTable.applyEditsAsync()
                                        updatedServerResult.addDoneListener {
                                            var edits: List<FeatureEditResult>? = null
                                            try {
                                                edits = updatedServerResult.get()
                                                if (edits!!.size > 0) {
                                                    if (!edits[0].hasCompletedWithErrors()) {
                                                        publishProgress(arcGISFeature)
                                                    }
                                                    else publishProgress("Thêm ảnh thất bại!")
                                                }
                                            } catch (e: Exception) {
                                                publishProgress(e.toString())
                                            }

                                        }
                                    }
                                }

                            } catch (e: Exception) {
                                publishProgress(e.toString())
                            }
                        }
                    }
                }


            }
        } catch (e: Exception) {
            publishProgress(e.toString())
        }


    }

    override fun onProgressUpdate(vararg values: Any) {
        super.onProgressUpdate(*values)
        if (mDialog != null && mDialog.isShowing) {
            mDialog.dismiss()
        }
        mDelegate.processFinish(values.first())

    }


}
