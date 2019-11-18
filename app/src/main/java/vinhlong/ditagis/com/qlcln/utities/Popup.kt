package vinhlong.ditagis.com.qlcln.utities

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.text.InputType
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.*
import com.esri.arcgisruntime.data.*
import com.esri.arcgisruntime.geometry.Envelope
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.MapView
import com.google.android.material.snackbar.Snackbar
import vinhlong.ditagis.com.qlcln.Editing.EditingMauKiemNghiem
import vinhlong.ditagis.com.qlcln.MainActivity
import vinhlong.ditagis.com.qlcln.R
import vinhlong.ditagis.com.qlcln.adapter.FeatureViewMoreInfoAdapter
import vinhlong.ditagis.com.qlcln.async.EditAsync
import vinhlong.ditagis.com.qlcln.async.QueryFeatureAsync
import vinhlong.ditagis.com.qlcln.libs.FeatureLayerDTG
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutionException

class Popup(private val mainActivity: MainActivity, private val mMapView: MapView, layerDTGS: List<FeatureLayerDTG>, private val mCallout: Callout?) : AppCompatActivity(), View.OnClickListener {
    private var mSelectedArcGISFeature: ArcGISFeature? = null
    private val mServiceFeatureTable: ServiceFeatureTable?
    private var mFeatureLayerDTG: FeatureLayerDTG? = null
    private var lstFeatureType: MutableList<String>? = null
    private var linearLayout: LinearLayout? = null
    private val table_thoigiancln: FeatureTable?
    private val featureLayerDTG_MauDanhGia: FeatureLayerDTG?
    private val editingMauKiemNghiem: EditingMauKiemNghiem
    private val format_yearfirst = SimpleDateFormat("yyyy/MM/dd ")

    init {
        this.mServiceFeatureTable = getServiceFeatureTable(layerDTGS, mainActivity.resources.getString(R.string.id_diemdanhgianuoc))
        this.table_thoigiancln = getServiceFeatureTable(layerDTGS, mainActivity.resources.getString(R.string.name_maudanhgia))
        this.featureLayerDTG_MauDanhGia = getFeatureLayerDTG(layerDTGS, mainActivity.resources.getString(R.string.id_maudanhgia))
        this.editingMauKiemNghiem = this.mServiceFeatureTable?.let { EditingMauKiemNghiem(mainActivity, featureLayerDTG_MauDanhGia!!, it) }!!

    }


    fun getServiceFeatureTable(layerDTGS: List<FeatureLayerDTG>, id: String): ServiceFeatureTable? {
        for (layerDTG in layerDTGS) {
            if (layerDTG.featureLayer.id == id) {
                return layerDTG.featureLayer.featureTable as ServiceFeatureTable
            }
        }
        return null
    }

    fun getFeatureLayerDTG(layerDTGS: List<FeatureLayerDTG>, id: String): FeatureLayerDTG? {
        for (layerDTG in layerDTGS) {
            if (layerDTG.featureLayer.id == id) {
                return layerDTG
            }
        }
        return null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    fun setFeatureLayerDTG(layerDTG: FeatureLayerDTG) {
        this.mFeatureLayerDTG = layerDTG
    }

    private fun refressPopup() {
        val attributes = mSelectedArcGISFeature!!.attributes
        for (field in this.mSelectedArcGISFeature!!.featureTable.fields) {
            val value = attributes[field.name]
            when (field.name) {
                Constant.IDDIEM_DANH_GIA -> if (value != null)
                    (linearLayout!!.findViewById<View>(R.id.txt_id_su_co) as TextView).text = value.toString()
                Constant.DIACHI -> if (value != null)
                    (linearLayout!!.findViewById<View>(R.id.txt_vi_tri_su_co) as TextView).text = value.toString()
            }
        }
    }

    fun dimissCallout() {
        val featureLayer = mFeatureLayerDTG!!.featureLayer
        featureLayer.clearSelection()
        if (mCallout != null && mCallout.isShowing) {
            mCallout.dismiss()
        }
    }

    fun showPopup(mSelectedArcGISFeature: ArcGISFeature) {
        dimissCallout()
        this.mSelectedArcGISFeature = mSelectedArcGISFeature
        val featureLayer = mFeatureLayerDTG!!.featureLayer
        featureLayer.selectFeature(mSelectedArcGISFeature)
        lstFeatureType = ArrayList()
        for (i in 0 until mSelectedArcGISFeature.featureTable.featureTypes.size) {
            lstFeatureType!!.add(mSelectedArcGISFeature.featureTable.featureTypes[i].name)
        }
        val inflater = LayoutInflater.from(this.mainActivity.applicationContext)
        linearLayout = inflater.inflate(R.layout.popup_diemdanhgianuoc, null) as LinearLayout
        linearLayout!!.findViewById<View>(R.id.imgbtn_popup_diem_danh_gia_nuoc_cancel)
                .setOnClickListener { view -> if (mCallout != null && mCallout.isShowing) mCallout.dismiss() }
        refressPopup()


        if (mFeatureLayerDTG!!.action!!.isEdit) {
            val imgBtn_ViewMoreInfo = linearLayout!!.findViewById<View>(R.id.imgBtn_ViewMoreInfo) as ImageButton
            imgBtn_ViewMoreInfo.visibility = View.VISIBLE
            imgBtn_ViewMoreInfo.setOnClickListener { viewMoreInfo() }

            val imgBtn_viewtablethoigian = linearLayout!!.findViewById<View>(R.id.imgBtn_viewtablethoigian) as ImageButton
            imgBtn_viewtablethoigian.visibility = View.VISIBLE
            (linearLayout!!.findViewById<View>(R.id.imgBtn_viewtablethoigian) as ImageButton).setOnClickListener { editingMauKiemNghiem.showDanhSachMauDanhGia(mSelectedArcGISFeature) }
        }
        if (mFeatureLayerDTG!!.action!!.isDelete) {
            val imgBtn_delete = linearLayout!!.findViewById<View>(R.id.imgBtn_delete) as ImageButton
            imgBtn_delete.visibility = View.VISIBLE
            imgBtn_delete.setOnClickListener {
                mSelectedArcGISFeature.featureTable.featureLayer.clearSelection()
                deleteFeature()
            }
        }
        linearLayout!!.layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        val envelope = mSelectedArcGISFeature.geometry.extent
        val envelope1 = Envelope(Point(envelope.xMin, envelope.yMin + DELTA_MOVE_Y), Point(envelope.xMax, envelope.yMax + DELTA_MOVE_Y))
        mMapView.setViewpointGeometryAsync(envelope1, 0.0)
        // show CallOut
        mCallout!!.location = envelope.center
        mCallout.content = linearLayout!!
        this.runOnUiThread {
            mCallout.refresh()
            mCallout.show()
        }
    }


    private fun viewMoreInfo() {
        val attr = mSelectedArcGISFeature!!.attributes
        val builder = AlertDialog.Builder(mainActivity, android.R.style.Theme_Material_Light_NoActionBar_Fullscreen)
        val layout = mainActivity.layoutInflater.inflate(R.layout.layout_viewmoreinfo_feature, null)
        val adapter = FeatureViewMoreInfoAdapter(mainActivity, ArrayList())
        val lstView = layout.findViewById<ListView>(R.id.lstView_alertdialog_info)
        lstView.adapter = adapter
        lstView.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id -> edit(parent, view, position, id) }
        val updateFields = mFeatureLayerDTG!!.updateFields
        val typeIdField = mSelectedArcGISFeature!!.featureTable.typeIdField
        for (field in this.mSelectedArcGISFeature!!.featureTable.fields) {
            val value = attr[field.name]
            if (field.name == Constant.IDDIEM_DANH_GIA) {
                if (value != null)
                    (layout.findViewById<View>(R.id.txt_alertdialog_id_su_co) as TextView).text = value.toString()
            } else {
                val item = FeatureViewMoreInfoAdapter.Item()
                item.alias = field.alias
                item.fieldName = field.name
                if (value != null) {
                    if (item.fieldName == typeIdField) {
                        val featureTypes = mSelectedArcGISFeature!!.featureTable.featureTypes
                        val valueFeatureType = getValueFeatureType(featureTypes, value.toString())!!.toString()
                        if (valueFeatureType != null) item.value = valueFeatureType
                    } else if (field.domain != null) {
                        val codedValues = (this.mSelectedArcGISFeature!!.featureTable.getField(item.fieldName!!).domain as CodedValueDomain).codedValues
                        val valueDomain = getValueDomain(codedValues, value.toString())
                        if (valueDomain != null) item.value = valueDomain.toString()
                    } else
                        when (field.fieldType) {
                            Field.Type.DATE -> item.value = Constant.DATE_FORMAT.format((value as Calendar).time)
                            Field.Type.OID, Field.Type.TEXT -> item.value = value.toString()
                            Field.Type.SHORT -> item.value = value.toString()
                        }
                }
                item.isEdit = false
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
                item.fieldType = field.fieldType
                adapter.add(item)
                adapter.notifyDataSetChanged()
            }
        }
        builder.setView(layout)
        builder.setCancelable(false)
        builder.setPositiveButton(mainActivity.getString(R.string.btn_Accept), null)
        builder.setNeutralButton(mainActivity.getString(R.string.btn_Esc), null)
        val dialog = builder.create()

        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        builder.setPositiveButton(android.R.string.ok, null)
        dialog.show()
        dialog.getButton(AlertDialog.BUTTON_NEUTRAL).setOnClickListener { dialog.dismiss() }
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val editAsync = EditAsync(mainActivity, mServiceFeatureTable!!, mSelectedArcGISFeature!!,
                    object : EditAsync.AsyncResponse {
                        override fun processFinish(o: Any) {
                            if (o is Long) {
                                Snackbar.make(layout, "Cập nhật thành công!", Snackbar.LENGTH_SHORT).show()
                                dialog.dismiss()

                                //query lại điểm để hiển thị lên popup
                                val parameters = QueryParameters()
                                parameters.isReturnGeometry = true
                                parameters.whereClause = String.format("OBJECTID = %d", o)
                                QueryFeatureAsync(mainActivity, mSelectedArcGISFeature!!.featureTable as ServiceFeatureTable,
                                        object: QueryFeatureAsync.AsyncResponse {
                                            override fun processFinish(o: List<Feature>) {

                                    if (o.isNotEmpty()) {
                                        showPopup(o.first() as ArcGISFeature)
                                        dialog.dismiss()
                                    } else {
                                        Snackbar.make(layout, "Có lỗi xảy ra", Snackbar.LENGTH_SHORT).show()
                                    }
                                }}).execute(parameters)
                            } else {
                                Snackbar.make(layout, "Cập nhật thất bại!", Snackbar.LENGTH_SHORT).show()
                            }
                        }
                    })
            try {
                editAsync.execute(adapter).get()
                refressPopup()
            } catch (e: InterruptedException) {
                e.printStackTrace()
            } catch (e: ExecutionException) {
                e.printStackTrace()
            }
        }

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

    private fun getValueFeatureType(featureTypes: List<FeatureType>, code: String): Any? {
        var value: Any? = null
        for (featureType in featureTypes) {
            if (featureType.id.toString() == code) {
                value = featureType.name
                break
            }
        }
        return value
    }

    private fun edit(parent: AdapterView<*>, view: View, position: Int, id: Long) {
        if (parent.getItemAtPosition(position) is FeatureViewMoreInfoAdapter.Item) {
            val item = parent.getItemAtPosition(position) as FeatureViewMoreInfoAdapter.Item
            if (item.isEdit) {
                val builder = AlertDialog.Builder(mainActivity, android.R.style.Theme_Material_Light_Dialog_Alert)
                builder.setTitle("Cập nhật thuộc tính")
                builder.setMessage(item.alias)
                builder.setCancelable(false).setNegativeButton("Hủy") { dialog, which -> dialog.dismiss() }
                val layout = mainActivity.layoutInflater.inflate(R.layout.layout_dialog_update_feature_listview, null) as LinearLayout

                val layoutTextView = layout.findViewById<FrameLayout>(R.id.layout_edit_viewmoreinfo_TextView)
                val textView = layout.findViewById<TextView>(R.id.txt_edit_viewmoreinfo)
                val img_selectTime = layout.findViewById<View>(R.id.img_selectTime) as ImageView
                val layoutEditText = layout.findViewById<LinearLayout>(R.id.layout_edit_viewmoreinfo_Editext)
                val editText = layout.findViewById<EditText>(R.id.etxt_edit_viewmoreinfo)
                val layoutSpin = layout.findViewById<LinearLayout>(R.id.layout_edit_viewmoreinfo_Spinner)
                val spin = layout.findViewById<Spinner>(R.id.spin_edit_viewmoreinfo)

                val domain = mSelectedArcGISFeature!!.featureTable.getField(item.fieldName!!).domain
                if (item.fieldName == mSelectedArcGISFeature!!.featureTable.typeIdField) {
                    layoutSpin.visibility = View.VISIBLE
                    val adapter = ArrayAdapter(layout.context, android.R.layout.simple_list_item_1, lstFeatureType!!)
                    spin.adapter = adapter
                    if (item.value != null)
                        spin.setSelection(lstFeatureType!!.indexOf(item.value!!))
                } else if (domain != null) {
                    layoutSpin.visibility = View.VISIBLE
                    val codedValues = (domain as CodedValueDomain).codedValues
                    if (codedValues != null) {
                        val codes = ArrayList<String>()
                        for (codedValue in codedValues)
                            codes.add(codedValue.name)
                        val adapter = ArrayAdapter(layout.context, android.R.layout.simple_list_item_1, codes)
                        spin.adapter = adapter
                        if (item.value != null)
                            spin.setSelection(codes.indexOf(item.value!!))

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
                                    val calendar = GregorianCalendar(datePicker.year, datePicker.month, datePicker.dayOfMonth)
                                    val s = String.format("%02d/%02d/%d", datePicker.dayOfMonth, datePicker.month + 1, datePicker.year)

                                    textView.setText(s)
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
                            editText.setText(item.value)
                        }
                        Field.Type.DOUBLE -> {
                            layoutEditText.visibility = View.VISIBLE
                            editText.inputType = InputType.TYPE_NUMBER_FLAG_DECIMAL
                            editText.setText(item.value)
                        }
                    }
                builder.setPositiveButton("Cập nhật") { dialog, which ->
                    if (item.fieldName == mSelectedArcGISFeature!!.featureTable.typeIdField || domain != null) {
                        item.value = spin.selectedItem.toString()
                    } else {
                        when (item.fieldType) {
                            Field.Type.DATE -> item.value = textView.text.toString()
                            Field.Type.DOUBLE -> try {
                                val x = java.lang.Double.parseDouble(editText.text.toString())
                                item.value = editText.text.toString()
                            } catch (e: Exception) {
                                Toast.makeText(mainActivity, R.string.input_format_incorrect, Toast.LENGTH_LONG).show()
                            }

                            Field.Type.TEXT -> item.value = editText.text.toString()
                            Field.Type.SHORT -> try {
                                val x = java.lang.Short.parseShort(editText.text.toString())
                                item.value = editText.text.toString()
                            } catch (e: Exception) {
                                Toast.makeText(mainActivity, R.string.input_format_incorrect, Toast.LENGTH_LONG).show()
                            }

                        }
                    }
                    dialog.dismiss()
                    val adapter = parent.adapter as FeatureViewMoreInfoAdapter
                    adapter.notifyDataSetChanged()
                }
                builder.setView(layout)
                val dialog = builder.create()
                dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                dialog.show()

            }
        }

    }

    private fun deleteFeature() {
        val builder = AlertDialog.Builder(mainActivity, android.R.style.Theme_Material_Light_Dialog_Alert)
        builder.setTitle("Xác nhận")
        builder.setMessage(R.string.question_delete_point)
        builder.setPositiveButton("Có") { dialog, which ->
            dialog.dismiss()
            mSelectedArcGISFeature!!.loadAsync()

            // update the selected feature
            mSelectedArcGISFeature!!.addDoneLoadingListener {
                if (mSelectedArcGISFeature!!.loadStatus == LoadStatus.FAILED_TO_LOAD) {
                    Log.d(resources.getString(R.string.app_name), "Error while loading feature")
                }
                try {
                    // update feature in the feature table
                    val mapViewResult = mServiceFeatureTable!!.deleteFeatureAsync(mSelectedArcGISFeature!!)
                    mapViewResult.addDoneListener {
                        // apply change to the server
                        val serverResult = mServiceFeatureTable.applyEditsAsync()
                        serverResult.addDoneListener {
                            var edits: List<FeatureEditResult>? = null
                            try {
                                edits = serverResult.get()
                                if (edits!!.size > 0) {
                                    editingMauKiemNghiem.deleteDanhSachMauDanhGia(mSelectedArcGISFeature!!)
                                    if (!edits[0].hasCompletedWithErrors()) {
                                        Log.e("", "Feature successfully updated")
                                    }
                                }
                            } catch (e: InterruptedException) {
                                e.printStackTrace()
                            } catch (e: ExecutionException) {
                                e.printStackTrace()
                            }
                        }
                    }

                } catch (e: Exception) {
                    Log.e(resources.getString(R.string.app_name), "deteting feature in the feature table failed: " + e.message)
                }
            }
            mCallout?.dismiss()
        }.setNegativeButton("Không") { dialog, which -> dialog.dismiss() }.setCancelable(false)
        val dialog = builder.create()
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.show()


    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.btnAdd -> {
            }
        }//            @Override
        //
    }

    companion object {
        private val DELTA_MOVE_Y = 0.0//7000;
    }
}
