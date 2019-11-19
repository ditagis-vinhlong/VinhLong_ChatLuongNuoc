package vinhlong.ditagis.com.qlcln.utities

import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.AsyncTask
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.widget.ListView
import android.widget.Toast
import com.esri.arcgisruntime.ArcGISRuntimeException
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.DefaultMapViewOnTouchListener
import com.esri.arcgisruntime.mapping.view.Graphic
import com.esri.arcgisruntime.mapping.view.GraphicsOverlay
import com.esri.arcgisruntime.mapping.view.MapView
import com.esri.arcgisruntime.symbology.SimpleMarkerSymbol
import kotlinx.android.synthetic.main.content_quan_ly_su_co.*
import vinhlong.ditagis.com.qlcln.MainActivity
import vinhlong.ditagis.com.qlcln.R
import vinhlong.ditagis.com.qlcln.adapter.DanhSachDiemDanhGiaAdapter
import vinhlong.ditagis.com.qlcln.async.AddFeatureAsync
import vinhlong.ditagis.com.qlcln.entities.DApplication
import vinhlong.ditagis.com.qlcln.libs.FeatureLayerDTG
import java.util.concurrent.ExecutionException
import kotlin.math.roundToInt


/**
 * Created by ThanLe on 2/2/2018.
 */

class MapViewHandler(private val mFeatureLayerDTG: FeatureLayerDTG, private val mMapView: MapView, private val mMainActivity: MainActivity) : Activity() {
    private var mGraphicsOverlay: GraphicsOverlay = GraphicsOverlay()
    private val mMap: ArcGISMap = mMapView.map
    private val suCoTanHoaLayer: FeatureLayer = mFeatureLayerDTG.featureLayer
    private val mApplication = mMainActivity.application as DApplication
    private var mClickPoint: android.graphics.Point? = null
    private var mSelectedArcGISFeature: ArcGISFeature? = null
    private var isClickBtnAdd = false
    private val mServiceFeatureTable: ServiceFeatureTable = mFeatureLayerDTG.featureLayer.featureTable as ServiceFeatureTable
    var popupInfos: Popup? = null

    init {
        mMapView.graphicsOverlays.add(mGraphicsOverlay)
        mMapView.onTouchListener = object : DefaultMapViewOnTouchListener(mMainActivity, mMapView) {
            override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
                try {
                    onSingleTapMapView(e!!)
                } catch (ex: ArcGISRuntimeException) {
                    Log.d("", ex.toString())
                }

                return super.onSingleTapConfirmed(e)
            }

            override fun onScroll(e1: MotionEvent, e2: MotionEvent?, distanceX: Float, distanceY: Float): Boolean {
                if (mMainActivity.isAddingFeatureOrChangingGeometry()) {
                    val center: Point = mMainActivity.mapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).targetGeometry.extent.center
                    addGraphic(center)
                    if (mMainActivity.mapView.callout.isShowing) mMainActivity.mapView.callout.dismiss()
                } else clearGraphics()
                return super.onScroll(e1, e2, distanceX, distanceY)
            }

            override fun onUp(e: MotionEvent?): Boolean {
                handlingOnUpTouchOrDoneLocate()
                return super.onUp(e)
            }

            override fun onScale(detector: ScaleGestureDetector): Boolean {
                return super.onScale(detector)
            }
        }
    }

    fun handlingOnUpTouchOrDoneLocate() {
        if (mMainActivity.isAddingFeatureOrChangingGeometry()) {
            if (mMainActivity.mapView.callout.isShowing) mMainActivity.mapView.callout.dismiss()
            showPopUpLocation()
        }
//        showLatLong()
    }
    fun showPopUpLocation(vararg e: MotionEvent) {
        try {
            val center: Point
            if (e.isEmpty())
                mApplication.center = mMainActivity.mapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).targetGeometry.extent.center
            else {
                mApplication.center = mMainActivity.mapView.screenToLocation(android.graphics.Point(e[0].x.roundToInt(), e[0].y.roundToInt()))
                mMainActivity.mapView.setViewpointCenterAsync(mApplication.center)
            }
            addGraphic(mApplication.center!!)
            if (mApplication?.featureLayerDiemDanhGia != null){
                mMainActivity.getPopUp()!!.showPopupAddFeatureOrChangeGeometry(mApplication?.center!!, mApplication?.selectedFeature,

                        mApplication?.featureLayerDiemDanhGia!!.featureTable as ServiceFeatureTable)

            }
            else{

            }

        } catch (ex: Exception) {
            Toast.makeText(mMainActivity.mapView.context, "Có lỗi xảy ra khi hiển thị cửa sổ", Toast.LENGTH_LONG).show()
        }
    }
    fun setClickBtnAdd(clickBtnAdd: Boolean) {
        isClickBtnAdd = clickBtnAdd
    }

//    fun addFeature(image: ByteArray) {
//        val singleTapAdddFeatureAsync = SingleTapAdddFeatureAsync(mMainActivity, image)
//        val add_point = mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).targetGeometry.extent.center
//        singleTapAdddFeatureAsync.execute(add_point)
//    }

    fun addFeature(point: Point?, bitmaps: ArrayList<Bitmap>) {
        AddFeatureAsync(mMainActivity, bitmaps, mServiceFeatureTable, object : AddFeatureAsync.AsyncResponse {
            override fun processFinish(o: Any) {
                if (o is ArcGISFeature) {
                    mApplication.selectedFeature = o
                    mMainActivity.getPopUp()!!.showPopup(mApplication.selectedFeature as ArcGISFeature)
                    mMainActivity.getPopUp()?.handlingCancelAdd()

                    DAlertDialog().show(mMainActivity, "Thông báo", "Cập nhật thành công")
                } else if (o is String) {
                    DAlertDialog().show(mMainActivity, "Thông báo", o)
                }

            }
        }).execute(point)

    }
//    fun editFeature(point: Point, feature: Feature, serviceFeatureTable: ServiceFeatureTable?, bitmaps: Bitmap?) {
////        val editPoint = mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).targetGeometry.extent.center
//        EditGeometryAsync(mMainActivity, feature, serviceFeatureTable,bitmaps, object : EditGeometryAsync.AsyncResponse {
//            override fun processFinish(o: Any) {
//
//                if (o is Boolean) {
//                    if (o) {
//                        DAlertDialog().show(mMainActivity, "Thông báo","Cập nhật thành công")
//                        mMainActivity.getPopUp()?.handlingCancelAdd()
//                    } else
//                        DAlertDialog().show(mMainActivity, "Thông báo","Cập nhật thất bại")
//                } else if (o is String) {
//                    DAlertDialog().show(mMainActivity, "Có lỗi xảy ra", o)
//                }
//            }
//        }).execute(point)
//    }


    fun addGraphic(center: Point) {
//        val symbol = PictureMarkerSymbol(Constant.URLSymbol.MAC_DINH)
//        symbol.height = 25f
//        symbol.width = 25f
//        val listenableFuture = PictureMarkerSymbol.createAsync(BitmapDrawable(mMainActivity.resources,
//                BitmapFactory.decodeResource(mMainActivity.resources, R.drawable.pin)))
//        listenableFuture.addDoneListener {
//            val symbol = listenableFuture.get()
//            symbol?.let {
//                symbol.height = 25f
//                symbol.width = 25f
//                val graphic = Graphic(center, symbol)
//                mGraphicsOverlay.graphics.clear()
//                mGraphicsOverlay.graphics.add(graphic)
//            }
//        }
        val symbol = SimpleMarkerSymbol(SimpleMarkerSymbol.Style.CROSS, Color.GREEN, 20f)
        val graphic = Graphic(center, symbol)
        clearGraphics()
        mGraphicsOverlay.graphics.add(graphic)

    }

    fun clearGraphics() {
        mGraphicsOverlay.graphics.clear()
    }

    fun onScroll(e1: MotionEvent, e2: MotionEvent, distanceX: Float, distanceY: Float): DoubleArray {
        val center = mMapView.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE).targetGeometry.extent.center
        val project = GeometryEngine.project(center, SpatialReferences.getWgs84())
//        Geometry geometry = GeometryEngine.project(project, SpatialReferences.getWebMercator());
        return doubleArrayOf(project.extent.center.x, project.extent.center.y)
    }

    fun onSingleTapMapView(e: MotionEvent) {
        val clickPoint = mMapView.screenToLocation(android.graphics.Point(Math.round(e.x), Math.round(e.y)))
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

            val singleTapMapViewAsync = SingleTapMapViewAsync(mMainActivity)
            singleTapMapViewAsync.execute(clickPoint)
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

    }


    companion object {

        private val REQUEST_ID_IMAGE_CAPTURE = 1
        private val DELTA_MOVE_Y = 0.0//7000;
    }
}