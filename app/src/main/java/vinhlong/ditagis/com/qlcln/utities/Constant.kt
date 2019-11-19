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
    private val SERVER_API = "http://vwa.ditagis.com/api"
    //    private static final String SERVER_API = "http://113.161.88.180:798/apiv1/api";
    object LayerID {
        val DIEM_DANH_GIA = "diemdanhgiaLYR"
        val MAU_DANH_GIA = "maukiemnghiemTBL"
    }
    object API_URL {
        val LOGIN = "$SERVER_API/Login"
        val DISPLAY_NAME = "$SERVER_API/Account/Profile"
        val LAYER_INFO = "$SERVER_API/Account/LayerInfo"
        val IS_ACCESS = "$SERVER_API/Account/IsAccess/m_qlcln"
    }


    object FIELD_DIEM_DANH_GIA {
        val CANH_BAO_VUOT_NGUONG = "CanhBaoVuotNguong"
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
