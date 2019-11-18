package vinhlong.ditagis.com.qlcln.utities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.os.AsyncTask
import android.os.Build
import androidx.annotation.RequiresApi
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.widget.ListView
import com.esri.arcgisruntime.concurrent.ListenableFuture
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.tasks.geocode.LocatorTask
import vinhlong.ditagis.com.qlcln.R
import vinhlong.ditagis.com.qlcln.adapter.DanhSachDiemDanhGiaAdapter
import vinhlong.ditagis.com.qlcln.libs.FeatureLayerDTG
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException


/**
 * Created by ThanLe on 2/2/2018.
 */

class MapViewHandler(private val mFeatureLayerDTG: FeatureLayerDTG, private val mMapView: MapView, private val mContext: Context) : Activity() {
    private val mMap: ArcGISMap
    private val suCoTanHoaLayer: FeatureLayer
    internal var loc = LocatorTask("http://geocode.arcgis.com/arcgis/rest/services/World/GeocodeServer")
    private var mClickPoint: android.graphics.Point? = null
    private var mSelectedArcGISFeature: ArcGISFeature? = null
    private var isClickBtnAdd = false
    private val mServiceFeatureTable: ServiceFeatureTable
    var popupInfos: Popup? = null

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

    init {
        this.mServiceFeatureTable = mFeatureLayerDTG.featureLayer.featureTable as ServiceFeatureTable
        this.mMap = mMapView.map
        this.suCoTanHoaLayer = mFeatureLayerDTG.featureLayer
    }

    fun setClickBtnAdd(clickBtnAdd: Boolean) {
        isClickBtnAdd = clickBtnAdd
    }

    fun addFeature(image: ByteArray) {
        val singleTapAdddFeatureAsync = SingleTapAdddFeatureAsync(mContext, image)
        val add_point = mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).targetGeometry.extent.center
        singleTapAdddFeatureAsync.execute(add_point)
    }


    fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): DoubleArray {
        val center = mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).targetGeometry.extent.center
        val project = GeometryEngine.project(center, SpatialReferences.getWgs84())
//        Geometry geometry = GeometryEngine.project(project, SpatialReferences.getWebMercator());
        return doubleArrayOf(project.extent.center.x, project.extent.center.y)
    }

    fun onSingleTapMapView(e: MotionEvent) {
        val clickPoint = mMapView.screenToLocation(android.graphics.Point(Math.round(e.x), Math.round(e.y)))
        if (isClickBtnAdd) {
            mMapView.setViewpointCenterAsync(clickPoint, 10.0)
        } else {
            mClickPoint = android.graphics.Point(e.x.toInt(), e.y.toInt())
            mSelectedArcGISFeature = null
            // get the point that was clicked and convert it to a point in map coordinates
            val tolerance = 10
            val mapTolerance = tolerance * mMapView.unitsPerDensityIndependentPixel
            // create objects required to do a selection with a query
            val envelope = Envelope(clickPoint.x - mapTolerance, clickPoint.y - mapTolerance, clickPoint.x + mapTolerance, clickPoint.y + mapTolerance, mMap.spatialReference)
            val query = QueryParameters()
            query.geometry = envelope
            // add done loading listener to fire when the selection returns

            val singleTapMapViewAsync = SingleTapMapViewAsync(mContext)
            singleTapMapViewAsync.execute(clickPoint)
        }
    }

    fun queryByObjectID(objectID: String) {
        val queryParameters = QueryParameters()
        val query = "OBJECTID = $objectID"
        queryParameters.whereClause = query
        val feature = mServiceFeatureTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
        feature.addDoneListener {
            try {
                val result = feature.get()
                if (result.iterator().hasNext()) {
                    val item = result.iterator().next()
                    showPopup(item)
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }

    }

    private fun showPopup(selectedFeature: Feature?) {
        if (selectedFeature != null) {
            popupInfos!!.setFeatureLayerDTG(mFeatureLayerDTG)
            popupInfos!!.showPopup(selectedFeature as ArcGISFeature)
        }
    }

    fun querySearch(searchStr: String, listView: ListView, adapter: DanhSachDiemDanhGiaAdapter) {
        adapter.clear()
        adapter.notifyDataSetChanged()
        val queryParameters = QueryParameters()
        val builder = StringBuilder()
        for (field in mServiceFeatureTable.fields) {
            when (field.fieldType) {
                Field.Type.OID, Field.Type.INTEGER, Field.Type.SHORT -> try {
                    val search = Integer.parseInt(searchStr)
                    builder.append(String.format("%s = %s", field.name, search))
                    builder.append(" or ")
                } catch (e: Exception) {

                }

                Field.Type.FLOAT, Field.Type.DOUBLE -> try {
                    val search = java.lang.Double.parseDouble(searchStr)
                    builder.append(String.format("%s = %s", field.name, search))
                    builder.append(" or ")
                } catch (e: Exception) {

                }

                Field.Type.TEXT -> {
                    builder.append(field.name + " like N'%" + searchStr + "%'")
                    builder.append(" or ")
                }
            }
        }
        builder.append(" 1 = 2 ")
        queryParameters.whereClause = builder.toString()
        val feature = mServiceFeatureTable.queryFeaturesAsync(queryParameters)
        feature.addDoneListener {
            try {
                val result = feature.get()
                val iterator = result.iterator()
                while (iterator.hasNext()) {
                    val item = iterator.next() as Feature
                    adapter.add(item)
                    adapter.notifyDataSetChanged()
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }

    }

    internal inner class SingleTapMapViewAsync(private val mContext: Context) : AsyncTask<Point, Void, Void>() {
        private val mDialog: ProgressDialog?

        init {
            mDialog = ProgressDialog(mContext, android.R.style.Theme_Material_Dialog_Alert)
        }

        override fun onPreExecute() {
            super.onPreExecute()
            mDialog!!.setMessage("Đang xử lý...")
            mDialog.setCancelable(false)
            mDialog.show()
        }

        override fun doInBackground(vararg params: Point): Void? {
            val clickPoint = params[0]
            @SuppressLint("WrongThread") val identifyFuture = mMapView.identifyLayerAsync(suCoTanHoaLayer, mClickPoint!!, 5.0, false, 1)
            identifyFuture.addDoneListener {
                try {
                    val layoutInflater = LayoutInflater.from(mContext)
                    if (mDialog != null && mDialog.isShowing) {
                        mDialog.dismiss()
                    }
                    val layerResult = identifyFuture.get()
                    val resultGeoElements = layerResult.elements
                    if (resultGeoElements.size > 0) {
                        if (resultGeoElements[0] is ArcGISFeature) {
                            mSelectedArcGISFeature = resultGeoElements[0] as ArcGISFeature
                        }
                    } else {
                        mSelectedArcGISFeature = null
                    }
                    publishProgress()
                } catch (e: Exception) {
                    Log.e(mContext.resources.getString(R.string.app_name), "Select feature failed: " + e.message)
                }
            }
            return null
        }

        override fun onProgressUpdate(vararg values: Void) {
            super.onProgressUpdate(*values)
            popupInfos!!.setFeatureLayerDTG(mFeatureLayerDTG)
            if (mSelectedArcGISFeature != null)
                popupInfos!!.showPopup(mSelectedArcGISFeature!!)
            else
                popupInfos!!.dimissCallout()
        }


        override fun onPostExecute(result: Void) {
            super.onPostExecute(result)

        }

    }

    internal inner class SingleTapAdddFeatureAsync(private val mContext: Context, private val mImage: ByteArray) : AsyncTask<Point, Void, Void>() {
        private val mDialog: ProgressDialog?

        init {
            mDialog = ProgressDialog(mContext, android.R.style.Theme_Material_Dialog_Alert)
        }

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
            val listListenableFuture = loc.reverseGeocodeAsync(clickPoint)
            listListenableFuture.addDoneListener {
                try {
                    val geocodeResults = listListenableFuture.get()
                    if (geocodeResults.size > 0) {
                        val geocodeResult = geocodeResults[0]
                        val attrs = HashMap<String, Any>()
                        for (key in geocodeResult.attributes.keys) {
                            geocodeResult.attributes[key]?.let { attrs.put(key, it) }
                        }
                        val address = geocodeResult.attributes["LongLabel"].toString()
                        feature.attributes[Constant.DIACHI] = address
                    }
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
                    featureQuery.addDoneListener { addFeatureAsync(featureQuery, feature, finalTimeID, finalDateTime) }
                } catch (e1: InterruptedException) {
                    notifyError()
                    e1.printStackTrace()
                } catch (e1: ExecutionException) {
                    notifyError()
                    e1.printStackTrace()
                }
            }

            return null
        }

        private fun notifyError() {
            MySnackBar.make(mMapView, "Đã xảy ra lỗi", false)
            if (mDialog != null && mDialog.isShowing) {
                mDialog.dismiss()
            }

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
                            }
                        } catch (e: InterruptedException) {
                            notifyError()
                            e.printStackTrace()
                        } catch (e: ExecutionException) {
                            notifyError()
                            e.printStackTrace()
                        }


                    }
                }
            } catch (e: InterruptedException) {
                notifyError()
                e.printStackTrace()
            } catch (e: ExecutionException) {
                notifyError()
                e.printStackTrace()
            }

        }

        private fun addAttachment(feature: ListenableFuture<FeatureQueryResult>) {
            var result: FeatureQueryResult? = null
            try {
                result = feature.get()
                if (result!!.iterator().hasNext()) {
                    val item = result.iterator().next()
                    mSelectedArcGISFeature = item as ArcGISFeature
                    val attachmentName = mContext.getString(R.string.attachment) + "_" + System.currentTimeMillis() + ".png"
                    val addResult = mSelectedArcGISFeature!!.addAttachmentAsync(mImage, Bitmap.CompressFormat.PNG.toString(), attachmentName)
                    addResult.addDoneListener {
                        if (mDialog != null && mDialog.isShowing) {
                            mDialog.dismiss()
                        }
                        try {
                            val attachment = addResult.get()
                            if (attachment.size > 0) {
                                val tableResult = mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature!!)
                                tableResult.addDoneListener {
                                    val updatedServerResult = mServiceFeatureTable.applyEditsAsync()
                                    updatedServerResult.addDoneListener {
                                        var edits: List<FeatureEditResult>? = null
                                        try {
                                            edits = updatedServerResult.get()
                                            if (edits!!.size > 0) {
                                                if (!edits[0].hasCompletedWithErrors()) {
                                                }
                                            }
                                        } catch (e: InterruptedException) {
                                            e.printStackTrace()
                                        } catch (e: ExecutionException) {
                                            e.printStackTrace()
                                        }

                                        if (mDialog != null && mDialog.isShowing) {
                                            mDialog.dismiss()
                                        }
                                    }
                                }
                            }

                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        } catch (e: ExecutionException) {
                            e.printStackTrace()
                        }
                    }
                    val extent = item.getGeometry().extent
                    mMapView.setViewpointGeometryAsync(extent)
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }


        }

        override fun onProgressUpdate(vararg values: Void) {
            super.onProgressUpdate(*values)

        }


        override fun onPostExecute(result: Void) {
            super.onPostExecute(result)

        }

    }

    companion object {

        private val REQUEST_ID_IMAGE_CAPTURE = 1
        private val DELTA_MOVE_Y = 0.0//7000;
    }
}