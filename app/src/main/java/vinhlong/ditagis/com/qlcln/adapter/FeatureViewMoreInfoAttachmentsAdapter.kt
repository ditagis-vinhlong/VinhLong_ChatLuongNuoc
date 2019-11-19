package vinhlong.ditagis.com.qlcln.adapter

import android.content.Context
import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import com.esri.arcgisruntime.data.Attachment
import kotlinx.android.synthetic.main.item_viewmoreinfo_attachment.view.*
import vinhlong.ditagis.com.qlcln.R


/**
 * Created by ThanLe on 04/10/2017.
 */

class FeatureViewMoreInfoAttachmentsAdapter(private val mContext: Context, private val items: MutableList<Item>) : ArrayAdapter<FeatureViewMoreInfoAttachmentsAdapter.Item>(mContext, 0, items) {

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

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var convertView = convertView
        if (convertView == null) {
            val inflater = mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            convertView = inflater.inflate(R.layout.item_viewmoreinfo_attachment, null)
        }
        val item = items[position]

        val txtValue = convertView!!.txt_viewmoreinfo_attachment_name
        if (item.bitmap != null) {
            val imageView = convertView.img_viewmoreinfo_attachment
            imageView.setImageBitmap(Bitmap.createScaledBitmap(item.bitmap!!, item.bitmap!!.width,
                    item.bitmap!!.height, false))
        }
        txtValue.text = item.attachment?.name

        return convertView
    }

    class Item {
        var attachment: Attachment? = null
        var bitmap: Bitmap? = null
    }
}
