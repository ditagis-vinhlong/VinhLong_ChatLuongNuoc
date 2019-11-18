package vinhlong.ditagis.com.qlcln.adapter

import android.annotation.SuppressLint
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import com.esri.arcgisruntime.data.Field
import vinhlong.ditagis.com.qlcln.R
import java.util.*

class TraCuuChiTietDiemDanhGiaAdapter(private var mContext: Context, private var items: MutableList<TraCuuChiTietDiemDanhGiaAdapter.Item>?) : ArrayAdapter<TraCuuChiTietDiemDanhGiaAdapter.Item>(mContext, 0, items as MutableList<Item>) {

    override fun getContext(): Context {
        return mContext
    }

    fun setContext(context: Context) {
        this.mContext = context
    }

    fun getItems(): List<Item>? {
        return items
    }

    fun setItems(items: MutableList<Item>) {
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
            convertView = inflater.inflate(R.layout.item_text_text_image, null)
        }
        val item = items!![position]
        val textViewItem1 = convertView!!.findViewById<View>(R.id.txtItem1) as TextView
        val textViewItem2 = convertView.findViewById<View>(R.id.txtItem2) as TextView
        textViewItem1.text = item.alias
        textViewItem2.text = item.value
        return convertView
    }

    class Item {
        var fieldName: String? = null
        var alias: String? = null
        var value: String? = null
        var fieldType: Field.Type? = null
        var calendarBegin: Calendar? = null
        var calendarEnd: Calendar? = null

        constructor() {}

        constructor(fieldName: String, alias: String, value: String) {
            this.fieldName = fieldName
            this.alias = alias
            this.value = value
        }
    }

}
