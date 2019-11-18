package vinhlong.ditagis.com.qlcln.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View

import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

import vinhlong.ditagis.com.qlcln.R

class MauKiemNghiemApdapter(private val mContext: Context, private var mauKiemNghiems: MutableList<MauKiemNghiemApdapter.MauKiemNghiem>?) : ArrayAdapter<MauKiemNghiemApdapter.MauKiemNghiem>(mContext, 0, mauKiemNghiems as MutableList<MauKiemNghiem>) {

    fun getMauKiemNghiems(): List<MauKiemNghiemApdapter.MauKiemNghiem>? {
        return mauKiemNghiems
    }

    fun setMauKiemNghiems(mauKiemNghiems: MutableList<MauKiemNghiemApdapter.MauKiemNghiem>) {
        this.mauKiemNghiems = mauKiemNghiems
    }

    override fun clear() {
        mauKiemNghiems!!.clear()
    }

    override fun getCount(): Int {
        return mauKiemNghiems!!.size
    }

    override fun getItemId(position: Int): Long {
        return 0
    }

    @SuppressLint("ResourceAsColor")
    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.item_text_text_image, null)
        }
        val mauKiemNghiem = mauKiemNghiems!![position]
        val textViewItem1 = convertView!!.findViewById<View>(R.id.txtItem1) as TextView
        val textViewItem2 = convertView.findViewById<View>(R.id.txtItem2) as TextView
        val imageView = convertView.findViewById<View>(R.id.img_Item) as ImageView
        textViewItem1.text = mauKiemNghiem.idMauKiemNghiem
        textViewItem2.text = mauKiemNghiem.tenMau
        if (mauKiemNghiem.isView!!) {
            imageView.visibility = View.VISIBLE
        } else
            imageView.visibility = View.GONE
        return convertView
    }

    class MauKiemNghiem {
        var objectid: String? = null
        var idMauKiemNghiem: String? = null
        var tenMau: String? = null
        var isView: Boolean? = null
    }

}
