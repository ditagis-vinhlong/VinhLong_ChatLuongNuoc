package vinhlong.ditagis.com.qlcln.utities

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
}
