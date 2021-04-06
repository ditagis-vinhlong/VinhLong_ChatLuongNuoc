package vinhlong.ditagis.com.qlcln.entities


import android.app.Application
import android.graphics.Bitmap
import android.location.Location
import com.esri.arcgisruntime.data.Attachment
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.layers.FeatureLayer
import vinhlong.ditagis.com.qlcln.libs.FeatureLayerDTG
import vinhlong.ditagis.com.qlcln.utities.Constant
import vinhlong.ditagis.com.qlcln.utities.DProgressDialog

class DApplication : Application() {
    var progressDialog = DProgressDialog()
    var statusCode = Constant.StatusCode.NORMAL.value
    var appInfo: DAppInfo? = null
    var layerInfos: List<DLayerInfo>? = null
    var center: Point? = null
    var bitmaps: ArrayList<Bitmap>? = null
    var selectedAttachment: Attachment? = null
    var selectedBitmap: Bitmap? = null
    var user: User? = null
    var selectedFeature: Feature? = null
    private var mLocation: Location? = null
    var diemDanhGia: FeatureLayerDTG? = null
    var mauKiemNghiem: FeatureLayerDTG? = null
    var serviceFeatureTableHanhChinh: ServiceFeatureTable? = null
    fun setmLocation(mLocation: Location) {
        this.mLocation = mLocation
    }
}