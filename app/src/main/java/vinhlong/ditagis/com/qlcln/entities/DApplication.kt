package vinhlong.ditagis.com.qlcln.entities


import android.app.Application
import android.app.Service
import android.graphics.Bitmap
import android.location.Location
import com.esri.arcgisruntime.data.Attachment
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.FeatureLayer
import vinhlong.ditagis.com.qlcln.entities.entitiesDB.User
import vinhlong.ditagis.com.qlcln.utities.Constant
import vinhlong.ditagis.com.qlcln.utities.DProgressDialog

class DApplication : Application() {
    var progressDialog= DProgressDialog ()
    var statusCode = Constant.StatusCode.NORMAL.value

    var center: Point? = null
    var bitmaps: ArrayList<Bitmap>? = null
    var selectedAttachment: Attachment? = null
    var selectedBitmap: Bitmap? = null
    var user: User? = null
    var selectedFeature: Feature? = null
    private var mLocation: Location? = null
    var featureLayerDiemDanhGia: FeatureLayer? = null
    var serviceFeatureTableHanhChinh: ServiceFeatureTable? = null
    fun setmLocation(mLocation: Location) {
        this.mLocation = mLocation
    }
}