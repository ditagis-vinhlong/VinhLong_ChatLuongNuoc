package vinhlong.ditagis.com.qlcln

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import android.text.format.DateFormat
import android.view.View
import android.view.Window
import android.widget.*
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.ServiceFeatureTable
import vinhlong.ditagis.com.qlcln.adapter.DanhSachDiemDanhGiaAdapter
import vinhlong.ditagis.com.qlcln.adapter.ThongKeAdapter
import vinhlong.ditagis.com.qlcln.async.QueryDiemDanhGiaAsync
import vinhlong.ditagis.com.qlcln.entities.entitiesDB.ListObjectDB
import vinhlong.ditagis.com.qlcln.utities.TimePeriodReport
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

class ThongKeActivity : AppCompatActivity() {
    private var txtTongItem: TextView? = null
    private var serviceFeatureTable: ServiceFeatureTable? = null
    private var thongKeAdapter: ThongKeAdapter? = null

    protected override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_thong_ke)
        var items: MutableList<ThongKeAdapter.Item>? = ArrayList<ThongKeAdapter.Item>()
        for (layerInfoDTG in ListObjectDB.getInstance().lstFeatureLayerDTG!!) {
            if (layerInfoDTG.id != null && layerInfoDTG.id == getString(R.string.id_diemdanhgianuoc)) {
                var url = layerInfoDTG.url
                if (!layerInfoDTG.url!!.startsWith("http"))
                    url = "http:" + layerInfoDTG.url!!
                serviceFeatureTable = ServiceFeatureTable(url!!)
            }
        }


        val timePeriodReport = TimePeriodReport(this)

        items = timePeriodReport.getItems() as MutableList<ThongKeAdapter.Item>?
        thongKeAdapter = ThongKeAdapter(this, items!!)

        this.txtTongItem = this.findViewById<TextView>(R.id.txtTongItem)
        (this@ThongKeActivity.findViewById<View>(R.id.layout_thongke_thoigian) as LinearLayout).setOnClickListener(object : View.OnClickListener {
            public override fun onClick(v: View) {
                showDialogSelectTime()
            }
        })
        query(items!!.get(0))
    }

    private fun showDialogSelectTime() {
        val builder = AlertDialog.Builder(this, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen)
        val layout = getLayoutInflater().inflate(R.layout.layout_listview_thongketheothoigian, null)
        val layoutDateTimePicker = View.inflate(this, R.layout.date_time_picker, null)
        val listView = layout.findViewById<View>(R.id.lstView_thongketheothoigian) as ListView
        listView.setAdapter(thongKeAdapter)
        builder.setView(layout)
        val selectTimeDialog = builder.create()
        selectTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        selectTimeDialog.show()
        val finalItems = thongKeAdapter!!.getItems()
        listView.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            public override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val itemAtPosition = parent.getItemAtPosition(position) as ThongKeAdapter.Item
                selectTimeDialog.dismiss()
                if (itemAtPosition.id == finalItems.size) {
                    val builder = AlertDialog.Builder(this@ThongKeActivity, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen)
                    val layout = getLayoutInflater().inflate(R.layout.layout_thongke_thoigiantuychinh, null)
                    builder.setView(layout)
                    val tuychinhDateDialog = builder.create()
                    tuychinhDateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
                    tuychinhDateDialog.show()
                    val edit_thongke_tuychinh_ngaybatdau = layout.findViewById<View>(R.id.edit_thongke_tuychinh_ngaybatdau) as EditText
                    val edit_thongke_tuychinh_ngayketthuc = layout.findViewById<View>(R.id.edit_thongke_tuychinh_ngayketthuc) as EditText
                    if (itemAtPosition.thoigianbatdau != null)
                        edit_thongke_tuychinh_ngaybatdau.setText(itemAtPosition.thoigianbatdau)
                    if (itemAtPosition.thoigianketthuc != null)
                        edit_thongke_tuychinh_ngayketthuc.setText(itemAtPosition.thoigianketthuc)

                    val finalThoigianbatdau = StringBuilder()
                    finalThoigianbatdau.append(itemAtPosition.thoigianbatdau)
                    edit_thongke_tuychinh_ngaybatdau.setOnClickListener(object : View.OnClickListener {
                        public override fun onClick(v: View) {
                            showDateTimePicker(edit_thongke_tuychinh_ngaybatdau, finalThoigianbatdau, "START")
                        }
                    })
                    val finalThoigianketthuc = StringBuilder()
                    finalThoigianketthuc.append(itemAtPosition.thoigianketthuc)
                    edit_thongke_tuychinh_ngayketthuc.setOnClickListener(object : View.OnClickListener {
                        public override fun onClick(v: View) {
                            showDateTimePicker(edit_thongke_tuychinh_ngayketthuc, finalThoigianketthuc, "FINISH")
                        }
                    })

                    layout.findViewById<View>(R.id.btn_layngaythongke).setOnClickListener(object : View.OnClickListener {
                        public override fun onClick(v: View) {
                            if (kiemTraThoiGianNhapVao(finalThoigianbatdau.toString(), finalThoigianketthuc.toString())) {
                                tuychinhDateDialog.dismiss()
                                itemAtPosition.thoigianbatdau = finalThoigianbatdau.toString()
                                itemAtPosition.thoigianketthuc = finalThoigianketthuc.toString()
                                itemAtPosition.thoigianhienthi = edit_thongke_tuychinh_ngaybatdau.getText().toString() + " - " + edit_thongke_tuychinh_ngayketthuc.getText()
                                thongKeAdapter!!.notifyDataSetChanged()
                                query(itemAtPosition)
                            }
                        }
                    })

                } else {
                    query(itemAtPosition)
                }
            }
        })
    }

    private fun kiemTraThoiGianNhapVao(startDate: String, endDate: String): Boolean {
        if (startDate === "" || endDate === "") return false
        val dateFormat = SimpleDateFormat("yyyy-MM-dd hh:mm:ss")
        try {
            val date1 = dateFormat.parse(startDate)
            val date2 = dateFormat.parse(endDate)
            return !date1.after(date2)
        } catch (e: ParseException) {
            e.printStackTrace()
        }

        return false
    }

    fun showDateTimePicker(editText: EditText, output: StringBuilder, typeInput: String) {
        output.delete(0, output.length)
        val dialogView = View.inflate(this, R.layout.date_time_picker, null)
        val alertDialog = android.app.AlertDialog.Builder(this).create()
        dialogView.findViewById<View>(R.id.date_time_set).setOnClickListener(object : View.OnClickListener {
            public override fun onClick(view: View) {
                val datePicker = dialogView.findViewById<View>(R.id.date_picker) as DatePicker
                val calendar = GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth())
                val displaytime = DateFormat.format(getString(R.string.format_time_day_month_year), calendar.getTime()) as String
                var format: String? = null
                if (typeInput == "START") {
                    calendar.set(Calendar.HOUR_OF_DAY, 0) // ! clear would not reset the hour of day !
                    calendar.clear(Calendar.MINUTE)
                    calendar.clear(Calendar.SECOND)
                    calendar.clear(Calendar.MILLISECOND)
                } else if (typeInput == "FINISH") {
                    calendar.set(Calendar.HOUR_OF_DAY, 23)
                    calendar.set(Calendar.MINUTE, 59)
                    calendar.set(Calendar.SECOND, 59)
                    calendar.set(Calendar.MILLISECOND, 999)
                }
                val dateFormatGmt = SimpleDateFormat(getString(R.string.format_day_yearfirst))
                dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"))
                format = dateFormatGmt.format(calendar.getTime())
                editText.setText(displaytime)
                output.append(format)
                alertDialog.dismiss()
            }
        })
        alertDialog.setView(dialogView)
        alertDialog.show()

    }

    private fun query(item: ThongKeAdapter.Item) {
        (this@ThongKeActivity.findViewById<View>(R.id.txt_thongke_mota) as TextView).setText(item.mota)
        val txtThoiGian = this@ThongKeActivity.findViewById<TextView>(R.id.txt_thongke_thoigian)
        if (item.thoigianhienthi == null)
            txtThoiGian.setVisibility(View.GONE)
        else {
            txtThoiGian.setText(item.thoigianhienthi)
            txtThoiGian.setVisibility(View.VISIBLE)
        }
        var whereClause = "1 = 1"
        if (item.thoigianbatdau == null || item.thoigianketthuc == null) {
            whereClause = "1 = 1"
        } else
            whereClause = "NgayCapNhat" + " >= date '" + item.thoigianbatdau + "' and " + "NgayCapNhat" + " <= date '" + item.thoigianketthuc + "'"
        getQueryDiemDanhGiaAsync(whereClause)


    }

    private fun getQueryDiemDanhGiaAsync(whereClause: String) {
        val listView = findViewById<View>(R.id.listview) as ListView
        val items = ArrayList<Feature>()
        val adapter = DanhSachDiemDanhGiaAdapter(this, items)
        listView.setAdapter(adapter)
        listView.setOnItemClickListener(object : AdapterView.OnItemClickListener {
            public override fun onItemClick(parent: AdapterView<*>, view: View, position: Int, id: Long) {
                val returnIntent = Intent()
                returnIntent.putExtra(getString(R.string.ket_qua_objectid), adapter.getItems()!!.get(position).getAttributes().get(this@ThongKeActivity.getString(R.string.OBJECTID)).toString())
                setResult(Activity.RESULT_OK, returnIntent)
                finish()
            }
        })
        if (serviceFeatureTable != null)
            QueryDiemDanhGiaAsync(this, serviceFeatureTable!!, txtTongItem!!, adapter, object : QueryDiemDanhGiaAsync.AsyncResponse {
                 override fun processFinish(features: List<Feature>) {}
            }).execute(whereClause)
    }


}
