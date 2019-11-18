package vinhlong.ditagis.com.qlcln.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView

import vinhlong.ditagis.com.qlcln.R

/**
 * Created by ThanLe on 04/10/2017.
 */
class ThongKeAdapter(private val mContext: Context, private val items: MutableList<Item>) : ArrayAdapter<ThongKeAdapter.Item>(mContext, 0, items) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.item_thoigian_thongke, null)
        }
        val item = items[position]
        val layout = convertView!!.findViewById<View>(R.id.layout_tracuu) as LinearLayout
        val txt_thongke_mota = convertView.findViewById<View>(R.id.txt_thongke_mota) as TextView
        //todo
        txt_thongke_mota.text = item.mota
        val txt_thongke_thoigian = convertView.findViewById<View>(R.id.txt_thongke_thoigian) as TextView
        //todo
        if (item.thoigianhienthi != null) {
            txt_thongke_thoigian.text = item.thoigianhienthi
        }
        val imageView = convertView.findViewById<View>(R.id.img_selectTime) as ImageView
        if (item.isChecked) {
            imageView.visibility = View.VISIBLE
        } else {
            imageView.visibility = View.INVISIBLE
        }
        return convertView
    }

    fun getItems(): List<Item> {
        return items
    }

    override fun clear() {
        items.clear()
    }

    override fun getCount(): Int {
        return items.size
    }

    override fun getItemId(position: Int): Long {
        return 0
    }


    class Item {
        var id: Int = 0
        var mota: String? = null
        var thoigianbatdau: String? = null
        var thoigianketthuc: String? = null
        var thoigianhienthi: String? = null
        var isChecked = false

        constructor() {}

        constructor(id: Int, mota: String, thoigianbatdau: String, thoigianketthuc: String, thoigianhienthi: String) {
            this.id = id
            this.mota = mota
            this.thoigianbatdau = thoigianbatdau
            this.thoigianketthuc = thoigianketthuc
            this.thoigianhienthi = thoigianhienthi
        }
    }
}
