package vinhlong.ditagis.com.qlcln.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.esri.arcgisruntime.data.Feature
import vinhlong.ditagis.com.qlcln.R
import vinhlong.ditagis.com.qlcln.utities.Constant
import java.util.*

class DanhSachDiemDanhGiaAdapter(private var mContext: Context, private var items: MutableList<Feature>?) : ArrayAdapter<Feature>(mContext, 0, items as MutableList<Feature>) {

    override fun getContext(): Context {
        return mContext
    }

    fun setContext(context: Context) {
        this.mContext = context
    }

    fun getItems(): List<Feature>? {
        return items
    }

    fun setItems(items: MutableList<Feature>) {
        this.items = items
    }

    override fun clear() {
        items!!.clear()
    }

    override fun getCount(): Int {
        return items!!.size
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    @SuppressLint("ResourceAsColor")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = mContext!!.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.item_tracuu, null)
        }
        val item = items!![position]
        val txt_tracuu_id = convertView!!.findViewById<View>(R.id.txt_tracuu_id) as TextView
        val txt_tracuu_ngaycapnhat = convertView.findViewById<View>(R.id.txt_tracuu_ngaycapnhat) as TextView
        val txt_tracuu_diachi = convertView.findViewById<View>(R.id.txt_tracuu_diachi) as TextView

        val idDiemDanhGia = item.attributes[mContext!!.getString(R.string.IDDIEMDANHGIA)]
        if (idDiemDanhGia != null)
            txt_tracuu_id.text = idDiemDanhGia.toString()

        val ngayCapNhat = item.attributes[mContext!!.getString(R.string.NGAY_CAP_NHAT)]

        if (ngayCapNhat != null) {
            val format_date = Constant.DATE_FORMAT.format((ngayCapNhat as Calendar).time)
            txt_tracuu_ngaycapnhat.text = format_date
        }
        val diaChi = item.attributes[mContext!!.getString(R.string.DIACHI)]

        if (diaChi != null) {
            txt_tracuu_diachi.text = diaChi.toString()
        }

        return convertView
    }


}
