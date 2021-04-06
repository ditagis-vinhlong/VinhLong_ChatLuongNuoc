package vinhlong.ditagis.com.qlcln.utities

import android.graphics.Bitmap
import java.text.SimpleDateFormat

/**
 * Created by ThanLe on 3/1/2018.
 */

object Constant {
    val DATE_FORMAT = SimpleDateFormat("dd/MM/yyyy")
    val DDMMYYYY = SimpleDateFormat("ddMMyyyy")
    val OBJECTID = "OBJECTID"
    val IDDIEM_DANH_GIA = "IDDiemDanhGia"
    val IDMAUKIEMNGHIEM = "IDMauKiemNghiem"
    val DIACHI = "DiaChi"
    val NGAY_CAP_NHAT = "NgayCapNhat"
    val REQUEST_LOGIN = 0
    const val APP_ID = "ChatLuongNuoc"

    //    private val SERVER_API = "http://vwa.ditagis.com/api/api"
    const val SERVER_API = "http://vwaco.vn:9095"

    //    private static final String SERVER_API = "http://113.161.88.180:798/apiv1/api";
    object LayerID {
        val DIEM_DANH_GIA = "DIEMDANHGIA"
        val MAU_DANH_GIA = "HOSO_MAUKIEMNGHIEM"
        val BASEMAP = "basemap"
        val TRU_HONG = "truhongLYR"
    }

    object IDMapLayer {
        val HanhChinh: Long = 6
    }

    object FieldHanhChinh {
        val ID_HANH_CHINH = "IDHanhChinh"
        val MA_HUYEN = "MaHuyen"
        val TEN_HUYEN = "TenHuyen"
        val TEN_HANH_CHINH = "TenHanhChinh"
    }
    object Field{
        const val OBJECT_ID = "OBJECTID"

        val NONE_SHOW_FIELDS = arrayOf(OBJECT_ID)
    }
    object HTTPRequest {
        const val GET_METHOD = "GET"
        const val POST_METHOD = "POST"
        const val AUTHORIZATION = "Authorization"
    }

    object FieldChatLuongNuoc {
        val MA_HUYEN = "MaHuyen"
        val MA_XA = "MaXa"
    }

    object API_URL {
        const val UPDATE = "http://bg.nhabe.ditagis.com/vwa-mobile.txt"
        const val LOGIN = "$SERVER_API/auth/Login"
        const val CAPABILITIES = "$SERVER_API/auth/capabilities"
        const val APP_INFO = "$SERVER_API/auth/appinfo/"
        const val LAYER_INFO = "$SERVER_API/auth/layerinfos"
    }


    object FieldDiemDanhGia {
        const val ID = "IDDiemDanhGia"
        val CANH_BAO_VUOT_NGUONG = "CanhBaoVuotNguong"
    }
    object FieldMauKiemNghiem{
        const val ID = "IDMauKiemNghiem"
    }

    object VALUE_CANH_BAO_VUOT_NGUONG {
        val VUOT: Short = 1
        val KHONG_VUOT: Short = 2
    }

    object Request {
        val LOGIN = 0
        val QUERY = 1
        val PERMISSION = 2
        val CAMERA = 3
        val SHOW_CAPTURE = 4
    }

    object CompressFormat {
        val JPEG = Bitmap.CompressFormat.JPEG
        val PNG = Bitmap.CompressFormat.PNG

        val TYPE_UPDATE = JPEG

    }

    object PreferenceKey {
        const val USERNAME = "username"
        const val PASSWORD = "pasword"
        const val DISPLAY_NAME = "displayname"
        const val TOKEN = "token"
    }

    object AttachmentName {
        val ADD = "img_%s_%d.png"
        val UPDATE = "img_update_%s_%d.png"
    }

    enum class StatusCode(val value: Int) {
        NORMAL(1),
        IS_ADDING(2),
        CANCEL_ADD(3),
        IS_CHANGING_GEOMETRY(4),
        CANCEL_CHANGE_GEOMETRY(5)
    }

}
