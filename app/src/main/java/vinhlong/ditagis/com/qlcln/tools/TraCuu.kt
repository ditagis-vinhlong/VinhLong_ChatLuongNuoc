package vinhlong.ditagis.com.qlcln.tools

import androidx.appcompat.app.AlertDialog
import android.text.InputFilter
import android.text.InputType
import android.view.View
import android.view.Window
import android.widget.*
import com.esri.arcgisruntime.data.*
import vinhlong.ditagis.com.qlcln.MainActivity
import vinhlong.ditagis.com.qlcln.R
import vinhlong.ditagis.com.qlcln.adapter.DanhSachDiemDanhGiaAdapter
import vinhlong.ditagis.com.qlcln.adapter.TraCuuChiTietDiemDanhGiaAdapter
import vinhlong.ditagis.com.qlcln.async.QueryDiemDanhGiaAsync
import vinhlong.ditagis.com.qlcln.libs.FeatureLayerDTG
import vinhlong.ditagis.com.qlcln.utities.Popup
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created by NGUYEN HONG on 5/14/2018.
 */

class TraCuu(private val featureLayerDTG: FeatureLayerDTG, private val mainActivity: MainActivity) {
    private val serviceFeatureTable: ServiceFeatureTable
    private var traCuuChiTietDiemDanhGiaAdapter: TraCuuChiTietDiemDanhGiaAdapter? = null
    private var table_feature: List<Feature>? = null
    private var popupInfos: Popup? = null


    init {
        serviceFeatureTable = featureLayerDTG.featureLayer.featureTable as ServiceFeatureTable
    }

    fun setPopupInfos(popupInfos: Popup) {
        this.popupInfos = popupInfos
    }

    fun start() {
        val builder = AlertDialog.Builder(mainActivity, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen)
        val layout_table_tracuu = mainActivity.layoutInflater.inflate(R.layout.layout_title_listview_button, null)
        val listView = layout_table_tracuu.findViewById<View>(R.id.listview) as ListView
        (layout_table_tracuu.findViewById<View>(R.id.txtTitlePopup) as TextView).text = mainActivity.getString(R.string.nav_tra_cuu)
        val items = ArrayList<TraCuuChiTietDiemDanhGiaAdapter.Item>()
        traCuuChiTietDiemDanhGiaAdapter = TraCuuChiTietDiemDanhGiaAdapter(mainActivity, items)
        insertQueryField()
        listView.adapter = traCuuChiTietDiemDanhGiaAdapter
        listView.setOnItemClickListener { parent, view, position, id -> editValueAttribute(parent, view, position, id) }
        builder.setView(layout_table_tracuu)
        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.show()
        val btnTraCuu = layout_table_tracuu.findViewById<View>(R.id.btnAdd) as Button
        btnTraCuu.text = mainActivity.getString(R.string.nav_tra_cuu)
        btnTraCuu.setOnClickListener {
            dialog.dismiss()
            query()
        }


    }

    private fun query() {
        val params = ArrayList<String>()
        val items = traCuuChiTietDiemDanhGiaAdapter!!.getItems()
        for (item in items!!) {
            if (item.value != null) {
                var whereClause: String? = null
                val domain = serviceFeatureTable.getField(item.fieldName!!).domain
                var codeDomain: Any? = null
                if (domain != null) {
                    val codedValues = (domain as CodedValueDomain).codedValues
                    codeDomain = getCodeDomain(codedValues, item.value)
                }
                when (item.fieldType) {
                    Field.Type.DATE -> {
                        val dateFormatGmt = SimpleDateFormat(mainActivity.getString(R.string.format_day_yearfirst))
                        dateFormatGmt.timeZone = TimeZone.getTimeZone("GMT")
                        var format1: String? = null
                        var format2: String? = null
                        if (item.calendarBegin != null) {
                            item.calendarBegin!!.set(Calendar.HOUR_OF_DAY, 0) // ! clear would not reset the hour of day !
                            item.calendarBegin!!.clear(Calendar.MINUTE)
                            item.calendarBegin!!.clear(Calendar.SECOND)
                            item.calendarBegin!!.clear(Calendar.MILLISECOND)
                            format1 = dateFormatGmt.format(item.calendarBegin!!.time)
                        }
                        if (item.calendarEnd != null) {
                            item.calendarEnd!!.set(Calendar.HOUR_OF_DAY, 23)
                            item.calendarEnd!!.set(Calendar.MINUTE, 59)
                            item.calendarEnd!!.set(Calendar.SECOND, 59)
                            item.calendarEnd!!.set(Calendar.MILLISECOND, 999)
                            format2 = dateFormatGmt.format(item.calendarEnd!!.time)
                        }
                        whereClause = item.fieldName + " >= date '" + format1 + "' and " + item.fieldName + " <= date '" + format2 + "'"
                    }
                    Field.Type.DOUBLE -> if (item.value != null)
                        whereClause = item.fieldName + " = " + java.lang.Double.parseDouble(item.value!!)
                    Field.Type.SHORT -> if (codeDomain != null) {
                        whereClause = item.fieldName + " = " + java.lang.Short.parseShort(codeDomain.toString())
                    } else if (item.value != null)
                        whereClause = item.fieldName + " = " + item.value
                    Field.Type.TEXT -> if (codeDomain != null) {
                        whereClause = item.fieldName + " = '" + codeDomain.toString() + "'"
                    } else if (item.value != null)
                        whereClause = item.fieldName + " like N'%" + item.value + "%'"
                    Field.Type.OID -> whereClause = item.fieldName + " like '%" + item.value + "%'"
                }
                if (whereClause != null) {
                    params.add(whereClause)
                }
            }

        }
        val builder = StringBuilder()
        for (param in params) {
            builder.append(param)
            builder.append(" and ")
        }
        if (!builder.toString().isEmpty()) builder.append(" 1 = 1 ")
        getQueryDiemDanhGiaAsync(builder.toString())
    }

    private fun getQueryDiemDanhGiaAsync(whereClause: String) {
        val builder = AlertDialog.Builder(mainActivity, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen)
        val layout_table_tracuu = mainActivity.layoutInflater.inflate(R.layout.layout_title_listview, null)
        val listView = layout_table_tracuu.findViewById<View>(R.id.listview) as ListView
        val items = ArrayList<Feature>()
        val adapter = DanhSachDiemDanhGiaAdapter(mainActivity, items)
        listView.adapter = adapter

        builder.setView(layout_table_tracuu)
        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.show()
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val selectedFeature = adapter.getItems()!![position]
            if (selectedFeature != null) {
                dialog.dismiss()
                popupInfos!!.showPopup(selectedFeature as ArcGISFeature)
            }
        }
        val txtTongItem = layout_table_tracuu.findViewById<View>(R.id.txtTongItem) as TextView
        QueryDiemDanhGiaAsync(mainActivity, serviceFeatureTable, txtTongItem, adapter,object: QueryDiemDanhGiaAsync.AsyncResponse{
            override fun processFinish(features: List<Feature>) {
                table_feature = features
            }
        } ).execute(whereClause)

    }

    private fun getSelectedFeature(OBJECTID: String): Feature? {
        var rt_feature: Feature? = null
        for (feature in table_feature!!) {
            val objectID = feature.attributes[mainActivity.getString(R.string.OBJECTID)]
            if (objectID != null && objectID.toString() == OBJECTID) {
                rt_feature = feature
            }
        }
        return rt_feature
    }

    private fun getCodeDomain(codedValues: List<CodedValue>, value: String?): Any? {
        var code: Any? = null
        for (codedValue in codedValues) {
            if (codedValue.name == value) {
                code = codedValue.code
                break
            }

        }
        return code
    }

    private fun editValueAttribute(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val item = traCuuChiTietDiemDanhGiaAdapter!!.getItems()!![position]
        val calendar = arrayOfNulls<Calendar>(2)
        val builder_editValue = AlertDialog.Builder(mainActivity, android.R.style.Theme_Material_Light_Dialog_Alert)
        builder_editValue.setTitle("Tra cứu theo thuộc tính")
        builder_editValue.setMessage(item.alias)
        builder_editValue.setCancelable(false).setNegativeButton("Hủy") { dialog, which -> dialog.dismiss() }
        val inflater = mainActivity.layoutInflater
        val layout = inflater.inflate(R.layout.layout_dialog_update_feature_listview, null) as LinearLayout
        val layoutTextView = layout.findViewById<LinearLayout>(R.id.layout_edit_queryDateTime)
        val txt_edit_viewmoreinfoBegin = layout.findViewById<TextView>(R.id.txt_edit_viewmoreinfoBegin)
        val txt_edit_viewmoreinfoEnd = layout.findViewById<TextView>(R.id.txt_edit_viewmoreinfoEnd)
        val img_selectTimeBegin = layout.findViewById<View>(R.id.img_selectTimeBegin) as ImageView
        val img_selectTimeEnd = layout.findViewById<View>(R.id.img_selectTimeEnd) as ImageView
        val layoutEditText = layout.findViewById<LinearLayout>(R.id.layout_edit_viewmoreinfo_Editext)
        val editText = layout.findViewById<EditText>(R.id.etxt_edit_viewmoreinfo)
        val layoutSpin = layout.findViewById<LinearLayout>(R.id.layout_edit_viewmoreinfo_Spinner)
        val spin = layout.findViewById<Spinner>(R.id.spin_edit_viewmoreinfo)

        val domain = serviceFeatureTable.getField(item.fieldName!!).domain
        if (domain != null) {
            layoutSpin.visibility = View.VISIBLE
            val codedValues = (domain as CodedValueDomain).codedValues
            if (codedValues != null) {
                val codes = ArrayList<String>()
                for (codedValue in codedValues)
                    codes.add(codedValue.name)
                val adapter = ArrayAdapter(layout.context, android.R.layout.simple_list_item_1, codes)
                spin.adapter = adapter
                if (item.value != null) spin.setSelection(codes.indexOf(item.value!!))

            }
        } else
            when (item.fieldType) {
                Field.Type.DATE -> {
                    layoutTextView.visibility = View.VISIBLE
                    img_selectTimeBegin.setOnClickListener {
                        val dialogView = View.inflate(mainActivity, R.layout.date_time_picker, null)
                        val alertDialog = android.app.AlertDialog.Builder(mainActivity).create()
                        dialogView.findViewById<View>(R.id.date_time_set).setOnClickListener {
                            val datePicker = dialogView.findViewById<View>(R.id.date_picker) as DatePicker
                            calendar[0] = GregorianCalendar(datePicker.year, datePicker.month, datePicker.dayOfMonth)
                            val date = String.format("%02d/%02d/%d", datePicker.dayOfMonth, datePicker.month + 1, datePicker.year)
                            txt_edit_viewmoreinfoBegin.setText(date)
                            alertDialog.dismiss()
                        }
                        alertDialog.setView(dialogView)
                        alertDialog.show()
                    }
                    img_selectTimeEnd.setOnClickListener {
                        val dialogView = View.inflate(mainActivity, R.layout.date_time_picker, null)
                        val alertDialog = android.app.AlertDialog.Builder(mainActivity).create()
                        dialogView.findViewById<View>(R.id.date_time_set).setOnClickListener {
                            val datePicker = dialogView.findViewById<View>(R.id.date_picker) as DatePicker
                            calendar[1] = GregorianCalendar(datePicker.year, datePicker.month, datePicker.dayOfMonth)
                            val date = String.format("%02d/%02d/%d", datePicker.dayOfMonth, datePicker.month + 1, datePicker.year)
                            txt_edit_viewmoreinfoEnd.setText(date)
                            alertDialog.dismiss()
                        }
                        alertDialog.setView(dialogView)
                        alertDialog.show()
                    }
                }
                Field.Type.OID -> {
                    layoutEditText.visibility = View.VISIBLE
                    editText.setText(item.value)
                }
                Field.Type.TEXT -> {
                    layoutEditText.visibility = View.VISIBLE
                    editText.setText(item.value)
                }
                Field.Type.SHORT -> {
                    layoutEditText.visibility = View.VISIBLE
                    editText.inputType = InputType.TYPE_CLASS_NUMBER
                    editText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(8))
                    editText.setText(item.value)
                }
                Field.Type.DOUBLE -> {
                    layoutEditText.visibility = View.VISIBLE
                    editText.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
                    editText.filters = arrayOf<InputFilter>(InputFilter.LengthFilter(8))
                    editText.setText(item.value)
                }
            }
        builder_editValue.setPositiveButton("Cập nhật") { dialog, which ->
            if (domain != null) {
                item.value = spin.selectedItem.toString()
            } else {
                when (item.fieldType) {
                    Field.Type.DATE -> {
                        item.value = txt_edit_viewmoreinfoBegin.text.toString() + "-" + txt_edit_viewmoreinfoEnd.text.toString()
                        item.calendarBegin = calendar[0]
                        item.calendarEnd = calendar[1]
                    }
                    Field.Type.DOUBLE -> try {
                        val x = java.lang.Double.parseDouble(editText.text.toString())
                        item.value = editText.text.toString()
                    } catch (e: Exception) {
                        Toast.makeText(mainActivity, mainActivity.getString(R.string.INCORRECT_INPUT_FORMAT), Toast.LENGTH_LONG).show()
                    }

                    Field.Type.OID -> item.value = editText.text.toString()
                    Field.Type.TEXT -> item.value = editText.text.toString()
                    Field.Type.SHORT -> try {
                        val x = java.lang.Short.parseShort(editText.text.toString())
                        item.value = editText.text.toString()
                    } catch (e: Exception) {
                        Toast.makeText(mainActivity, mainActivity.getString(R.string.INCORRECT_INPUT_FORMAT), Toast.LENGTH_LONG).show()
                    }

                }
            }
            val adapter = parent.adapter as TraCuuChiTietDiemDanhGiaAdapter
            adapter.notifyDataSetChanged()
            dialog.dismiss()
        }
        builder_editValue.setView(layout)
        val dialog = builder_editValue.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.show()
    }

    private fun insertQueryField() {
        val items = ArrayList<TraCuuChiTietDiemDanhGiaAdapter.Item>()
        val queryFields = featureLayerDTG.queryFields
        val fields = serviceFeatureTable.fields
        for (field in fields) {
            if (queryFields == null || (queryFields.isNotEmpty() && (queryFields[0] == "*" || queryFields[0] == ""))) {

                val item = TraCuuChiTietDiemDanhGiaAdapter.Item()
                item.fieldName = field.name
                item.alias = field.alias
                item.fieldType = field.fieldType
                items.add(item)
            } else {
                for (queryField in queryFields) {
                    if (field.name == queryField) {
                        val item = TraCuuChiTietDiemDanhGiaAdapter.Item()
                        item.fieldName = field.name
                        item.alias = field.alias
                        item.fieldType = field.fieldType
                        items.add(item)
                        break
                    }
                }
            }
        }

        traCuuChiTietDiemDanhGiaAdapter!!.clear()
        traCuuChiTietDiemDanhGiaAdapter!!.setItems(items)
        traCuuChiTietDiemDanhGiaAdapter!!.notifyDataSetChanged()
    }
}
