package vinhlong.ditagis.com.qlcln.Editing

import androidx.appcompat.app.AlertDialog
import android.text.InputFilter
import android.text.InputType
import android.view.View
import android.view.Window
import android.widget.*
import com.esri.arcgisruntime.data.*
import vinhlong.ditagis.com.qlcln.MainActivity
import vinhlong.ditagis.com.qlcln.R
import vinhlong.ditagis.com.qlcln.adapter.ChiTietMauKiemNghiemAdapter
import vinhlong.ditagis.com.qlcln.adapter.MauKiemNghiemApdapter
import vinhlong.ditagis.com.qlcln.async.RefreshTableMauKiemNghiemAsync
import vinhlong.ditagis.com.qlcln.libs.FeatureLayerDTG
import vinhlong.ditagis.com.qlcln.utities.Constant
import java.util.*
import java.util.concurrent.ExecutionException

/**
 * Created by NGUYEN HONG on 5/7/2018.
 */

class EditingMauKiemNghiem(private val mainActivity: MainActivity, private val featureLayerDTG_MauDanhGia: FeatureLayerDTG, private val mServiceFeatureTable: ServiceFeatureTable) : RefreshTableMauKiemNghiemAsync.AsyncResponse {
    private val table_maudanhgia: ServiceFeatureTable
    private var mauKiemNghiemApdapter: MauKiemNghiemApdapter? = null
    private var table_feature: List<Feature>? = null
    private var mSelectedArcGISFeature: ArcGISFeature? = null


    init {
        table_maudanhgia = featureLayerDTG_MauDanhGia.featureLayer.featureTable as ServiceFeatureTable
    }


    fun deleteDanhSachMauDanhGia(mSelectedArcGISFeature: ArcGISFeature) {
        this.mSelectedArcGISFeature = mSelectedArcGISFeature
        val attributes = mSelectedArcGISFeature.attributes
        val idDiemDanhGia = attributes[mainActivity.getString(R.string.IDDIEMDANHGIA)].toString()
        if (idDiemDanhGia != null) {
            val mauKiemNghiems = ArrayList<MauKiemNghiemApdapter.MauKiemNghiem>()
            mauKiemNghiemApdapter = MauKiemNghiemApdapter(mainActivity, mauKiemNghiems)
            getRefreshTableThoiGianCLNAsync()
            if (table_feature != null && table_feature!!.size > 0) {
                for (feature in table_feature!!) {
                    deleteFeature(feature)
                }
            }
        }
    }

    fun showDanhSachMauDanhGia(mSelectedArcGISFeature: ArcGISFeature) {
        this.mSelectedArcGISFeature = mSelectedArcGISFeature
        val attributes = mSelectedArcGISFeature.attributes
        val idDiemDanhGia = attributes[mainActivity.getString(R.string.IDDIEMDANHGIA)].toString()
        if (idDiemDanhGia != null) {
            val builder = AlertDialog.Builder(mainActivity, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen)
            val layout_table_maudanhgia = mainActivity.layoutInflater.inflate(R.layout.layout_title_listview_button, null)
            val listView = layout_table_maudanhgia.findViewById<View>(R.id.listview) as ListView

            (layout_table_maudanhgia.findViewById<View>(R.id.txtTitlePopup) as TextView).text = mainActivity.getString(R.string.title_danhsachmaukiemnghiem)
            val btnAdd = layout_table_maudanhgia.findViewById<View>(R.id.btnAdd) as Button
            if (this.featureLayerDTG_MauDanhGia.action!!.isCreate == false) {
                btnAdd.visibility = View.INVISIBLE
            }
            btnAdd.text = "Thêm dữ liệu"
            btnAdd.setOnClickListener { addTableLayerMauDanhGia() }
            listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
                if (featureLayerDTG_MauDanhGia.action!!.isView) {
                    val itemAtPosition = mauKiemNghiemApdapter!!.getMauKiemNghiems()!![position]
                    val objectid = itemAtPosition.objectid
                    val queryParameters = QueryParameters()
                    val queryClause = mainActivity.getString(R.string.OBJECTID) + " = " + objectid
                    queryParameters.whereClause = queryClause
                    val queryResultListenableFuture = table_maudanhgia.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL)
                    queryResultListenableFuture.addDoneListener {
                        try {
                            val result = queryResultListenableFuture.get()
                            val iterator = result.iterator()

                            if (iterator.hasNext()) {
                                val feature = iterator.next() as Feature
                                showInfosSelectedItem(feature)
                            }

                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        } catch (e: ExecutionException) {
                            e.printStackTrace()
                        }
                    }
                }
            }
            val mauKiemNghiems = ArrayList<MauKiemNghiemApdapter.MauKiemNghiem>()
            mauKiemNghiemApdapter = MauKiemNghiemApdapter(mainActivity, mauKiemNghiems)
            listView.adapter = mauKiemNghiemApdapter
            getRefreshTableThoiGianCLNAsync()
            builder.setView(layout_table_maudanhgia)
            val dialog = builder.create()
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
            dialog.show()
        }
    }

    private fun showInfosSelectedItem(feature: Feature) {
        val attributes = feature.attributes
        val layout_chitiet_maudanhgia = mainActivity.layoutInflater.inflate(R.layout.layout_title_listview, null)
        val listview_chitiet_maudanhgia = layout_chitiet_maudanhgia.findViewById<View>(R.id.listview) as ListView
        if (attributes["IDMauKiemNghiem"] != null) {
            (layout_chitiet_maudanhgia.findViewById<View>(R.id.txtTongItem) as TextView).text = attributes["IDMauKiemNghiem"].toString()
        }
        val items = ArrayList<ChiTietMauKiemNghiemAdapter.Item>()
        val fields = table_maudanhgia.fields
        val updateFields = featureLayerDTG_MauDanhGia.updateFields
        val unedit_Fields = mainActivity.resources.getStringArray(R.array.unedit_Fields)
        for (field in fields) {
            val item = ChiTietMauKiemNghiemAdapter.Item()
            item.alias = field.alias
            item.fieldName = field.name
            item.fieldType = field.fieldType
            val value = attributes[field.name]
            if (value != null) {
                if (field.domain != null) {
                    val codedValues = (field.domain as CodedValueDomain).codedValues
                    val valueDomain = getValueDomain(codedValues, value.toString())!!.toString()
                    if (valueDomain != null) item.value = valueDomain
                } else
                    when (field.fieldType) {
                        Field.Type.DATE -> item.value = Constant.DATE_FORMAT.format((value as Calendar).time)
                        else -> if (attributes[field.name] != null)
                            item.value = attributes[field.name].toString()
                    }
            }
            if (this.featureLayerDTG_MauDanhGia.action!!.isEdit) {
                if (updateFields!!.size > 0) {
                    if (updateFields[0] == "*" || updateFields[0] == "") {
                        item.isEdit = true
                    } else {
                        for (updateField in updateFields) {
                            if (item.fieldName == updateField) {
                                item.isEdit = true
                                break
                            }
                        }
                    }
                }
                for (unedit_Field in unedit_Fields) {
                    if (unedit_Field.toUpperCase() == item.fieldName!!.toUpperCase()) {
                        item.isEdit = false
                        break
                    }
                }
            }
            items.add(item)
        }
        val chiTietMauKiemNghiemAdapter = ChiTietMauKiemNghiemAdapter(mainActivity, items)
        if (items != null) listview_chitiet_maudanhgia.adapter = chiTietMauKiemNghiemAdapter
        val builder = AlertDialog.Builder(mainActivity, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen)
        builder.setView(layout_chitiet_maudanhgia)
        if (this.featureLayerDTG_MauDanhGia.action!!.isEdit) {
            builder.setPositiveButton(mainActivity.getString(R.string.btn_Accept), null)
        }
        if (this.featureLayerDTG_MauDanhGia.action!!.isDelete) {
            builder.setNegativeButton(mainActivity.getString(R.string.btn_Delete), null)
        }
        builder.setNeutralButton(mainActivity.getString(R.string.btn_Esc), null)
        listview_chitiet_maudanhgia.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            if (featureLayerDTG_MauDanhGia.action!!.isEdit) {
                editValueAttribute(parent, view, position, id)
            }
        }
        val dialog = builder.create()
        builder.setPositiveButton(android.R.string.ok, null)
        dialog.show()
        // Chỉnh sửa
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val selectedFeature = getSelectedFeature(items[0].value)
            for (item in items) {
                val domain = table_maudanhgia.getField(item.fieldName!!).domain
                var codeDomain: Any? = null
                if (item.fieldName == "NgayCapNhat") {
                    val currentTime = Calendar.getInstance()
                    item.value = Constant.DATE_FORMAT.format(currentTime.time)
                }
                if (domain != null) {
                    val codedValues = (domain as CodedValueDomain).codedValues
                    codeDomain = getCodeDomain(codedValues, item.value)
                }
                when (item.fieldType) {
                    Field.Type.DATE -> if (item.calendar != null)
                        selectedFeature!!.attributes[item.fieldName] = item.calendar
                    Field.Type.DOUBLE -> if (item.value != null)
                        selectedFeature!!.attributes[item.fieldName] = java.lang.Double.parseDouble(item.value!!)
                    Field.Type.SHORT -> if (codeDomain != null) {
                        selectedFeature!!.attributes[item.fieldName] = java.lang.Short.parseShort(codeDomain.toString())
                    } else if (item.value != null)
                        selectedFeature!!.attributes[item.fieldName] = java.lang.Short.parseShort(item.value)
                    Field.Type.TEXT -> if (codeDomain != null) {
                        selectedFeature!!.attributes[item.fieldName] = codeDomain.toString()
                    } else if (item.value != null)
                        selectedFeature!!.attributes[item.fieldName] = item.value
                }
            }
            chiTietMauKiemNghiemAdapter.notifyDataSetChanged()
            val currentTime = Calendar.getInstance()
            selectedFeature!!.attributes["NgayCapNhat"] = currentTime
            updateFeature(selectedFeature)
        }
        // Xóa
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setOnClickListener {
            val selectedFeature = getSelectedFeature(items[0].value)
            deleteFeature(selectedFeature)
            dialog.dismiss()
        }
        // Thoát
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener { dialog.dismiss() }
    }

    private fun getRefreshTableThoiGianCLNAsync() {
        val attributes = this.mSelectedArcGISFeature!!.attributes
        val idDiemDanhGia = attributes[mainActivity.getString(R.string.IDDIEMDANHGIA)].toString()
        RefreshTableMauKiemNghiemAsync(mainActivity, table_maudanhgia,
                mauKiemNghiemApdapter!!, this.featureLayerDTG_MauDanhGia.action!!, object: RefreshTableMauKiemNghiemAsync.AsyncResponse
        {
            override fun processFinish(features: List<Feature>, thoiGianChatLuongNuocs: List<MauKiemNghiemApdapter.MauKiemNghiem>) {
                table_feature = features
                kiemtraDanhSachVuotChiTieu()
            }
        }

        ).execute(idDiemDanhGia)
    }

    private fun kiemtraDanhSachVuotChiTieu() {
        var vuotChiTieu = false
        for (feature in table_feature!!) {
            vuotChiTieu = kiemtraVuotChiTieu(feature)
            if (vuotChiTieu) break
        }
        updateSelectedArcGISFeature(vuotChiTieu)
    }

    private fun kiemtraVuotChiTieu(table_maudanhgiaFeature: Feature): Boolean {
        var isOver = false
        val doDuc = table_maudanhgiaFeature.attributes["DoDuc"]
        val PH = table_maudanhgiaFeature.attributes["PH"]
        val CloDu = table_maudanhgiaFeature.attributes["CloDu"]
        if (doDuc != null) {
            val doduc = java.lang.Double.parseDouble(doDuc.toString())
            if (doduc > 2) isOver = true
        }
        if (PH != null) {
            val ph = java.lang.Double.parseDouble(PH.toString())
            if (ph < 6.5 || ph > 8.5) isOver = true
        }
        if (CloDu != null) {
            val clodu = java.lang.Double.parseDouble(CloDu.toString())
            if (clodu < 0.3 || clodu > 0.5) isOver = true
        }
        return isOver
    }

    private fun getSelectedFeature(OBJECTID: String?): Feature? {
        var rt_feature: Feature? = null
        for (feature in table_feature!!) {
            val objectID = feature.attributes[mainActivity.getString(R.string.OBJECTID)]
            if (objectID != null && objectID.toString() == OBJECTID) {
                rt_feature = feature
            }
        }
        return rt_feature
    }

    private fun getValueAttributes(feature: Feature, fieldName: String): String? {
        return if (feature.attributes[fieldName] != null) feature.attributes[fieldName].toString() else null
    }

    private fun getValueDomain(codedValues: List<CodedValue>, code: String): Any? {
        var value: Any? = null
        for (codedValue in codedValues) {
            if (codedValue.code.toString() == code) {
                value = codedValue.name
                break
            }

        }
        return value
    }

    private fun addTableLayerMauDanhGia() {
        val attributes = this.mSelectedArcGISFeature!!.attributes
        val idDiemDanhGia = attributes[mainActivity.getString(R.string.IDDIEMDANHGIA)].toString()
        val table_maudanhgiaFeature = table_maudanhgia.createFeature()
        val builder = AlertDialog.Builder(mainActivity, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen)
        val layout_add_maudanhgia = mainActivity.layoutInflater.inflate(R.layout.layout_title_listview_button, null)
        val listView = layout_add_maudanhgia.findViewById<View>(R.id.listview) as ListView
        val items = ArrayList<ChiTietMauKiemNghiemAdapter.Item>()
        val chiTietMauKiemNghiemAdapter = ChiTietMauKiemNghiemAdapter(mainActivity, items)
        if (items != null) listView.adapter = chiTietMauKiemNghiemAdapter
        builder.setView(layout_add_maudanhgia)
        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.show()
        listView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id -> editValueAttribute(parent, view, position, id) }
        val fields = table_maudanhgia.fields
        val updateFields = featureLayerDTG_MauDanhGia.updateFields
        val unedit_Fields = mainActivity.resources.getStringArray(R.array.unedit_Fields)
        for (field in fields) {
            if (field.name != Constant.OBJECTID) {
                val item = ChiTietMauKiemNghiemAdapter.Item()
                item.alias = field.alias
                item.fieldName = field.name
                item.fieldType = field.fieldType
                if (updateFields!!.size > 0) {
                    if (updateFields[0] == "*" || updateFields[0] == "") {
                        item.isEdit = true
                    } else {
                        for (updateField in updateFields) {
                            if (item.fieldName == updateField) {
                                item.isEdit = true
                                break
                            }
                        }
                    }
                }
                for (unedit_Field in unedit_Fields) {
                    if (unedit_Field.toUpperCase() == item.fieldName!!.toUpperCase()) {
                        item.isEdit = false
                        break
                    }
                }
                if (field.name == mainActivity.getString(R.string.IDDIEMDANHGIA)) {
                    item.value = idDiemDanhGia
                }
                if (field.name == mainActivity.getString(R.string.IDMAUKIEMNGHIEM)) {
                    if (table_feature!!.size < 9) {
                        item.value = "0" + (table_feature!!.size + 1) + "_" + idDiemDanhGia
                    } else
                        item.value = (table_feature!!.size + 1).toString() + "_" + idDiemDanhGia
                }
                if (field.name == mainActivity.getString(R.string.NGAY_CAP_NHAT)) {
                    item.value = Constant.DATE_FORMAT.format(Calendar.getInstance().time)
                    item.calendar = Calendar.getInstance()
                }
                items.add(item)
            }
        }

        table_maudanhgiaFeature.attributes[Constant.IDDIEM_DANH_GIA] = attributes[Constant.IDDIEM_DANH_GIA].toString()
        val btnAdd = layout_add_maudanhgia.findViewById<View>(R.id.btnAdd) as Button
        btnAdd.text = mainActivity.getString(R.string.title_add)
        btnAdd.setOnClickListener {
            dialog.dismiss()
            for (item in items) {
                val domain = table_maudanhgia.getField(item.fieldName!!).domain
                var codeDomain: Any? = null
                if (domain != null) {
                    val codedValues = (domain as CodedValueDomain).codedValues
                    codeDomain = getCodeDomain(codedValues, item.value)
                    table_maudanhgiaFeature.attributes[item.fieldName] = item.value

                }
                when (item.fieldType) {
                    Field.Type.DATE -> if (item.calendar != null)
                        table_maudanhgiaFeature.attributes[item.fieldName] = item.calendar
                    Field.Type.DOUBLE -> if (item.value != null)
                        table_maudanhgiaFeature.attributes[item.fieldName] = java.lang.Double.parseDouble(item.value!!)
                    Field.Type.SHORT -> if (codeDomain != null) {
                        table_maudanhgiaFeature.attributes[item.fieldName] = java.lang.Short.parseShort(codeDomain.toString())
                    } else if (item.value != null)
                        table_maudanhgiaFeature.attributes[item.fieldName] = java.lang.Short.parseShort(item.value)
                    Field.Type.TEXT -> if (codeDomain != null) {
                        table_maudanhgiaFeature.attributes[item.fieldName] = codeDomain.toString()
                    } else if (item.value != null)
                        table_maudanhgiaFeature.attributes[item.fieldName] = item.value
                }
            }

            addFeature(table_maudanhgiaFeature)
        }
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

    /**
     * @param canhBaoVuotNguong của mẫu đánh giá
     */
    private fun updateSelectedArcGISFeature(canhBaoVuotNguong: Boolean) {
        val currentTime = Calendar.getInstance()
        mSelectedArcGISFeature!!.attributes["NgayCapNhat"] = currentTime
        if (canhBaoVuotNguong)
            mSelectedArcGISFeature!!.attributes[Constant.FIELD_DIEM_DANH_GIA.CANH_BAO_VUOT_NGUONG] = Constant.VALUE_CANH_BAO_VUOT_NGUONG.VUOT
        else
            mSelectedArcGISFeature!!.attributes[Constant.FIELD_DIEM_DANH_GIA.CANH_BAO_VUOT_NGUONG] = Constant.VALUE_CANH_BAO_VUOT_NGUONG.KHONG_VUOT

        mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature!!).addDoneListener { mServiceFeatureTable.applyEditsAsync().addDoneListener { } }
    }

    private fun addFeature(table_maudanhgiaFeature: Feature) {
        val mapViewResult = table_maudanhgia.addFeatureAsync(table_maudanhgiaFeature)
        mapViewResult.addDoneListener {
            val listListenableEditAsync = table_maudanhgia.applyEditsAsync()
            listListenableEditAsync.addDoneListener {
                try {
                    val featureEditResults = listListenableEditAsync.get()
                    if (featureEditResults.size > 0) {
                        Toast.makeText(mainActivity.applicationContext, mainActivity.getString(R.string.DATA_SUCCESSFULLY_INSERTED), Toast.LENGTH_SHORT).show()
                        getRefreshTableThoiGianCLNAsync()
                    } else {
                        Toast.makeText(mainActivity.applicationContext, mainActivity.getString(R.string.FAILED_TO_INSERT_DATA), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }
            }
        }
    }


    private fun deleteFeature(table_maudanhgiaFeature: Feature?) {
        val mapViewResult = table_maudanhgia.deleteFeatureAsync(table_maudanhgiaFeature!!)
        mapViewResult.addDoneListener {
            val listListenableEditAsync = table_maudanhgia.applyEditsAsync()
            listListenableEditAsync.addDoneListener {
                try {
                    val featureEditResults = listListenableEditAsync.get()
                    if (featureEditResults.size > 0) {
                        Toast.makeText(mainActivity.applicationContext, mainActivity.getString(R.string.DATA_SUCCESSFULLY_DELETED), Toast.LENGTH_SHORT).show()
                        getRefreshTableThoiGianCLNAsync()
                    } else {
                        Toast.makeText(mainActivity.applicationContext, mainActivity.getString(R.string.FAILED_TO_DELETE_DATA), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun updateFeature(table_maudanhgiaFeature: Feature?) {
        val mapViewResult = table_maudanhgia.updateFeatureAsync(table_maudanhgiaFeature!!)
        mapViewResult.addDoneListener {
            val listListenableEditAsync = table_maudanhgia.applyEditsAsync()
            listListenableEditAsync.addDoneListener {
                try {
                    val featureEditResults = listListenableEditAsync.get()
                    if (featureEditResults.size > 0) {
                        Toast.makeText(mainActivity.applicationContext, mainActivity.getString(R.string.DATA_SUCCESSFULLY_UPDATED), Toast.LENGTH_SHORT).show()
                        getRefreshTableThoiGianCLNAsync()
                    } else {
                        Toast.makeText(mainActivity.applicationContext, mainActivity.getString(R.string.FAILED_TO_UPDATE_DATA), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun editValueAttribute(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        val item = parent.getItemAtPosition(position) as ChiTietMauKiemNghiemAdapter.Item
        if (item.isEdit) {
            val calendar = arrayOfNulls<Calendar>(1)
            val builder = AlertDialog.Builder(mainActivity, android.R.style.Theme_Material_Light_Dialog_Alert)
            builder.setTitle("Cập nhật thuộc tính")
            builder.setMessage(item.alias)
            builder.setCancelable(false).setNegativeButton("Hủy") { dialog, which -> dialog.dismiss() }
            val layout = mainActivity.layoutInflater.inflate(R.layout.layout_dialog_update_feature_listview, null) as LinearLayout
            builder.setView(layout)
            val layoutTextView = layout.findViewById<FrameLayout>(R.id.layout_edit_viewmoreinfo_TextView)
            val textView = layout.findViewById<TextView>(R.id.txt_edit_viewmoreinfo)
            val img_selectTime = layout.findViewById<View>(R.id.img_selectTime) as ImageView
            val layoutEditText = layout.findViewById<LinearLayout>(R.id.layout_edit_viewmoreinfo_Editext)
            val editText = layout.findViewById<EditText>(R.id.etxt_edit_viewmoreinfo)
            val layoutSpin = layout.findViewById<LinearLayout>(R.id.layout_edit_viewmoreinfo_Spinner)
            val spin = layout.findViewById<Spinner>(R.id.spin_edit_viewmoreinfo)

            val domain = table_maudanhgia.getField(item.fieldName!!).domain
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
                        textView.text = item.value
                        img_selectTime.setOnClickListener {
                            val dialogView = View.inflate(mainActivity, R.layout.date_time_picker, null)
                            val alertDialog = android.app.AlertDialog.Builder(mainActivity).create()
                            dialogView.findViewById<View>(R.id.date_time_set).setOnClickListener {
                                val datePicker = dialogView.findViewById<View>(R.id.date_picker) as DatePicker
                                calendar[0] = GregorianCalendar(datePicker.year, datePicker.month, datePicker.dayOfMonth)
                                val date = String.format("%02d/%02d/%d", datePicker.dayOfMonth, datePicker.month + 1, datePicker.year)
                                textView.setText(date)
                                alertDialog.dismiss()
                            }
                            alertDialog.setView(dialogView)
                            alertDialog.show()
                        }
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
            builder.setPositiveButton("Cập nhật") { dialog, which ->
                if (domain != null) {
                    item.value = spin.selectedItem.toString()
                } else {
                    when (item.fieldType) {
                        Field.Type.DATE -> {
                            item.value = textView.text.toString()
                            item.calendar = calendar[0]
                        }
                        Field.Type.DOUBLE -> try {
                            val x = java.lang.Double.parseDouble(editText.text.toString())
                            item.value = editText.text.toString()
                        } catch (e: Exception) {
                            Toast.makeText(mainActivity, mainActivity.getString(R.string.INCORRECT_INPUT_FORMAT), Toast.LENGTH_LONG).show()
                        }

                        Field.Type.TEXT -> item.value = editText.text.toString()
                        Field.Type.SHORT -> try {
                            val x = java.lang.Short.parseShort(editText.text.toString())
                            item.value = editText.text.toString()
                        } catch (e: Exception) {
                            Toast.makeText(mainActivity, mainActivity.getString(R.string.INCORRECT_INPUT_FORMAT), Toast.LENGTH_LONG).show()
                        }

                    }
                }
                val adapter = parent.adapter as ChiTietMauKiemNghiemAdapter
                adapter.notifyDataSetChanged()
                //                    dialog.dismiss();
            }
            builder.setView(layout)
            val dialog = builder.create()
            //            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            dialog.show()

        }
    }

    override fun processFinish(features: List<Feature>, mauKiemNghiems: List<MauKiemNghiemApdapter.MauKiemNghiem>) {

    }
}
