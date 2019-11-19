package vinhlong.ditagis.com.qlcln.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import vinhlong.ditagis.com.qlcln.R

import vinhlong.ditagis.com.qlcln.entities.DAddress

/**
 * Created by ThanLe on 04/10/2017.
 */

class DiaChiAdapter(private val mContext: Context, private val items: MutableList<DAddress>) : ArrayAdapter<DAddress>(mContext, 0, items) {

    fun getItems(): List<DAddress> {
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


    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.item_diachi, null)
        }
        val item = items[position]
        val txtDiaChi = convertView!!.findViewById<TextView>(R.id.txt_diachi)
        if (item.adminArea != null) {
            txtDiaChi.text = item.location
        }
        return convertView
    }
}
