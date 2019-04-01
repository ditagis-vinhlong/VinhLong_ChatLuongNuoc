package vinhlong.ditagis.com.qlcln;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.ServiceFeatureTable;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import vinhlong.ditagis.com.qlcln.adapter.DanhSachDiemDanhGiaAdapter;
import vinhlong.ditagis.com.qlcln.adapter.ThongKeAdapter;
import vinhlong.ditagis.com.qlcln.async.QueryDiemDanhGiaAsync;
import vinhlong.ditagis.com.qlcln.entities.entitiesDB.LayerInfoDTG;
import vinhlong.ditagis.com.qlcln.entities.entitiesDB.ListObjectDB;
import vinhlong.ditagis.com.qlcln.utities.TimePeriodReport;

public class ThongKeActivity extends AppCompatActivity {
    private TextView txtTongItem;
    private ServiceFeatureTable serviceFeatureTable;
    private ThongKeAdapter thongKeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thong_ke);
        List<ThongKeAdapter.Item> items = new ArrayList<>();
        for (final LayerInfoDTG layerInfoDTG : ListObjectDB.getInstance().getLstFeatureLayerDTG()) {
            if (layerInfoDTG.getId() != null && layerInfoDTG.getId().equals(getString(R.string.id_diemdanhgianuoc))) {
                String url = layerInfoDTG.getUrl();
                if (!layerInfoDTG.getUrl().startsWith("http"))
                    url = "http:" + layerInfoDTG.getUrl();
                serviceFeatureTable = new ServiceFeatureTable(url);
            }
        }


        TimePeriodReport timePeriodReport = new TimePeriodReport(this);

        items = timePeriodReport.getItems();
        thongKeAdapter = new ThongKeAdapter(this, items);

        this.txtTongItem = this.findViewById(R.id.txtTongItem);
        ((LinearLayout) ThongKeActivity.this.findViewById(R.id.layout_thongke_thoigian)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDialogSelectTime();
            }
        });
        query(items.get(0));
    }

    private void showDialogSelectTime() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
        View layout = getLayoutInflater().inflate(R.layout.layout_listview_thongketheothoigian, null);
        final View layoutDateTimePicker = View.inflate(this, R.layout.date_time_picker, null);
        ListView listView = (ListView) layout.findViewById(R.id.lstView_thongketheothoigian);
        listView.setAdapter(thongKeAdapter);
        builder.setView(layout);
        final AlertDialog selectTimeDialog = builder.create();
        selectTimeDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        selectTimeDialog.show();
        final List<ThongKeAdapter.Item> finalItems = thongKeAdapter.getItems();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                final ThongKeAdapter.Item itemAtPosition = (ThongKeAdapter.Item) parent.getItemAtPosition(position);
                selectTimeDialog.dismiss();
                if (itemAtPosition.getId() == finalItems.size()) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(ThongKeActivity.this, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
                    View layout = getLayoutInflater().inflate(R.layout.layout_thongke_thoigiantuychinh, null);
                    builder.setView(layout);
                    final AlertDialog tuychinhDateDialog = builder.create();
                    tuychinhDateDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
                    tuychinhDateDialog.show();
                    final EditText edit_thongke_tuychinh_ngaybatdau = (EditText) layout.findViewById(R.id.edit_thongke_tuychinh_ngaybatdau);
                    final EditText edit_thongke_tuychinh_ngayketthuc = (EditText) layout.findViewById(R.id.edit_thongke_tuychinh_ngayketthuc);
                    if (itemAtPosition.getThoigianbatdau() != null)
                        edit_thongke_tuychinh_ngaybatdau.setText(itemAtPosition.getThoigianbatdau());
                    if (itemAtPosition.getThoigianketthuc() != null)
                        edit_thongke_tuychinh_ngayketthuc.setText(itemAtPosition.getThoigianketthuc());

                    final StringBuilder finalThoigianbatdau = new StringBuilder();
                    finalThoigianbatdau.append(itemAtPosition.getThoigianbatdau());
                    edit_thongke_tuychinh_ngaybatdau.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDateTimePicker(edit_thongke_tuychinh_ngaybatdau, finalThoigianbatdau, "START");
                        }
                    });
                    final StringBuilder finalThoigianketthuc = new StringBuilder();
                    finalThoigianketthuc.append(itemAtPosition.getThoigianketthuc());
                    edit_thongke_tuychinh_ngayketthuc.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            showDateTimePicker(edit_thongke_tuychinh_ngayketthuc, finalThoigianketthuc, "FINISH");
                        }
                    });

                    layout.findViewById(R.id.btn_layngaythongke).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (kiemTraThoiGianNhapVao(finalThoigianbatdau.toString(), finalThoigianketthuc.toString())) {
                                tuychinhDateDialog.dismiss();
                                itemAtPosition.setThoigianbatdau(finalThoigianbatdau.toString());
                                itemAtPosition.setThoigianketthuc(finalThoigianketthuc.toString());
                                itemAtPosition.setThoigianhienthi(edit_thongke_tuychinh_ngaybatdau.getText() + " - " + edit_thongke_tuychinh_ngayketthuc.getText());
                                thongKeAdapter.notifyDataSetChanged();
                                query(itemAtPosition);
                            }
                        }
                    });

                } else {
                    query(itemAtPosition);
                }
            }
        });
    }

    private boolean kiemTraThoiGianNhapVao(String startDate, String endDate) {
        if (startDate == "" || endDate == "") return false;
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        try {
            Date date1 = dateFormat.parse(startDate);
            Date date2 = dateFormat.parse(endDate);
            if (date1.after(date2)) {
                return false;
            } else return true;
        } catch (ParseException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return false;
    }

    public void showDateTimePicker(final EditText editText, final StringBuilder output, final String typeInput) {
        output.delete(0, output.length());
        final View dialogView = View.inflate(this, R.layout.date_time_picker, null);
        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(this).create();
        dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
                Calendar calendar = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                String displaytime = (String) DateFormat.format(getString(R.string.format_time_day_month_year), calendar.getTime());
                String format = null;
                if (typeInput.equals("START")) {
                    calendar.set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
                    calendar.clear(Calendar.MINUTE);
                    calendar.clear(Calendar.SECOND);
                    calendar.clear(Calendar.MILLISECOND);
                } else if (typeInput.equals("FINISH")) {
                    calendar.set(Calendar.HOUR_OF_DAY, 23);
                    calendar.set(Calendar.MINUTE, 59);
                    calendar.set(Calendar.SECOND, 59);
                    calendar.set(Calendar.MILLISECOND, 999);
                }
                SimpleDateFormat dateFormatGmt = new SimpleDateFormat(getString(R.string.format_day_yearfirst));
                dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
                format = dateFormatGmt.format(calendar.getTime());
                editText.setText(displaytime);
                output.append(format);
                alertDialog.dismiss();
            }
        });
        alertDialog.setView(dialogView);
        alertDialog.show();

    }

    private void query(ThongKeAdapter.Item item) {
        ((TextView) ThongKeActivity.this.findViewById(R.id.txt_thongke_mota)).setText(item.getMota());
        TextView txtThoiGian = ThongKeActivity.this.findViewById(R.id.txt_thongke_thoigian);
        if (item.getThoigianhienthi() == null) txtThoiGian.setVisibility(View.GONE);
        else {
            txtThoiGian.setText(item.getThoigianhienthi());
            txtThoiGian.setVisibility(View.VISIBLE);
        }
        String whereClause = "1 = 1";
        if (item.getThoigianbatdau() == null || item.getThoigianketthuc() == null) {
            whereClause = "1 = 1";
        } else
            whereClause = "NgayCapNhat" + " >= date '" + item.getThoigianbatdau() + "' and " + "NgayCapNhat" + " <= date '" + item.getThoigianketthuc() + "'";
        getQueryDiemDanhGiaAsync(whereClause);


    }

    private void getQueryDiemDanhGiaAsync(String whereClause) {
        ListView listView = (ListView) findViewById(R.id.listview);
        final List<DanhSachDiemDanhGiaAdapter.Item> items = new ArrayList<>();
        final DanhSachDiemDanhGiaAdapter adapter = new DanhSachDiemDanhGiaAdapter(this, items);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(getString(R.string.ket_qua_objectid), adapter.getItems().get(position).getObjectID());
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
            }
        });
        if (serviceFeatureTable != null)
            new QueryDiemDanhGiaAsync(this, serviceFeatureTable, txtTongItem, adapter, new QueryDiemDanhGiaAsync.AsyncResponse() {
                public void processFinish(List<Feature> features) {
                }
            }).execute(whereClause);
    }



}
