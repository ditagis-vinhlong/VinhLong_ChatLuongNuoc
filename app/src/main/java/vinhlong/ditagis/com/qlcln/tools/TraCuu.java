package vinhlong.ditagis.com.qlcln.tools;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.InputFilter;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.CodedValue;
import com.esri.arcgisruntime.data.CodedValueDomain;
import com.esri.arcgisruntime.data.Domain;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.Field;
import com.esri.arcgisruntime.data.ServiceFeatureTable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import vinhlong.ditagis.com.qlcln.MainActivity;
import vinhlong.ditagis.com.qlcln.R;
import vinhlong.ditagis.com.qlcln.adapter.DanhSachDiemDanhGiaAdapter;
import vinhlong.ditagis.com.qlcln.adapter.TraCuuChiTietDiemDanhGiaAdapter;
import vinhlong.ditagis.com.qlcln.async.QueryDiemDanhGiaAsync;
import vinhlong.ditagis.com.qlcln.libs.FeatureLayerDTG;
import vinhlong.ditagis.com.qlcln.utities.Popup;

/**
 * Created by NGUYEN HONG on 5/14/2018.
 */

public class TraCuu {
    private ServiceFeatureTable serviceFeatureTable;
    private MainActivity mainActivity;
    private FeatureLayerDTG featureLayerDTG;
    private TraCuuChiTietDiemDanhGiaAdapter traCuuChiTietDiemDanhGiaAdapter;
    private List<Feature> table_feature;
    private Popup popupInfos;


    public TraCuu(FeatureLayerDTG featureLayerDTG, MainActivity mainActivity) {
        this.featureLayerDTG = featureLayerDTG;
        serviceFeatureTable = (ServiceFeatureTable) featureLayerDTG.getFeatureLayer().getFeatureTable();
        this.mainActivity = mainActivity;
    }

    public void setPopupInfos(Popup popupInfos) {
        this.popupInfos = popupInfos;
    }

    public void start() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
        final View layout_table_tracuu = mainActivity.getLayoutInflater().inflate(R.layout.layout_title_listview_button, null);
        ListView listView = (ListView) layout_table_tracuu.findViewById(R.id.listview);
        ((TextView) layout_table_tracuu.findViewById(R.id.txtTitlePopup)).setText(mainActivity.getString(R.string.nav_tra_cuu));
        List<TraCuuChiTietDiemDanhGiaAdapter.Item> items = new ArrayList<>();
        traCuuChiTietDiemDanhGiaAdapter = new TraCuuChiTietDiemDanhGiaAdapter(mainActivity, items);
        insertQueryField();
        listView.setAdapter(traCuuChiTietDiemDanhGiaAdapter);
        listView.setOnItemClickListener((parent, view, position, id) -> editValueAttribute(parent, view, position, id));
        builder.setView(layout_table_tracuu);
        final AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
        Button btnTraCuu = (Button) layout_table_tracuu.findViewById(R.id.btnAdd);
        btnTraCuu.setText(mainActivity.getString(R.string.nav_tra_cuu));
        btnTraCuu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                query();
            }
        });


    }

    private void query() {
        List<String> params = new ArrayList<>();
        List<TraCuuChiTietDiemDanhGiaAdapter.Item> items = traCuuChiTietDiemDanhGiaAdapter.getItems();
        for (TraCuuChiTietDiemDanhGiaAdapter.Item item : items) {
            if (item.getValue() != null) {
                String whereClause = null;
                Domain domain = serviceFeatureTable.getField(item.getFieldName()).getDomain();
                Object codeDomain = null;
                if (domain != null) {
                    List<CodedValue> codedValues = ((CodedValueDomain) domain).getCodedValues();
                    codeDomain = getCodeDomain(codedValues, item.getValue());
                }
                switch (item.getFieldType()) {
                    case DATE:
                        SimpleDateFormat dateFormatGmt = new SimpleDateFormat(mainActivity.getString(R.string.format_day_yearfirst));
                        dateFormatGmt.setTimeZone(TimeZone.getTimeZone("GMT"));
                        String format1 = null,format2 = null;
                        if (item.getCalendarBegin() != null) {
                            item.getCalendarBegin().set(Calendar.HOUR_OF_DAY, 0); // ! clear would not reset the hour of day !
                            item.getCalendarBegin().clear(Calendar.MINUTE);
                            item.getCalendarBegin().clear(Calendar.SECOND);
                            item.getCalendarBegin().clear(Calendar.MILLISECOND);
                            format1 = dateFormatGmt.format(item.getCalendarBegin().getTime());
                        }
                        if (item.getCalendarEnd() != null) {
                            item.getCalendarEnd().set(Calendar.HOUR_OF_DAY, 23);
                            item.getCalendarEnd().set(Calendar.MINUTE, 59);
                            item.getCalendarEnd().set(Calendar.SECOND, 59);
                            item.getCalendarEnd().set(Calendar.MILLISECOND, 999);
                            format2 = dateFormatGmt.format(item.getCalendarEnd().getTime());
                        }
                        whereClause = item.getFieldName() + " >= date '" + format1 + "' and " + item.getFieldName() + " <= date '" + format2 + "'";
                        break;
                    case DOUBLE:
                        if (item.getValue() != null)
                            whereClause = item.getFieldName() + " = " + Double.parseDouble(item.getValue());
                        break;
                    case SHORT:
                        if (codeDomain != null) {
                            whereClause = item.getFieldName() + " = " + Short.parseShort(codeDomain.toString());
                        } else if (item.getValue() != null)
                            whereClause = item.getFieldName() + " = " + item.getValue();
                        break;
                    case TEXT:
                        if (codeDomain != null) {
                            whereClause = item.getFieldName() + " = '" + codeDomain.toString() + "'";
                        } else if (item.getValue() != null)
                            whereClause = item.getFieldName() + " like N'%" + item.getValue() + "%'";
                        break;
                    case OID:
                        whereClause = item.getFieldName() + " like '%" + item.getValue() + "%'";
                        break;
                }
                if (whereClause != null) {
                    params.add(whereClause);
                }
            }

        }
        StringBuilder builder = new StringBuilder();
        for (String param : params) {
            builder.append(param);
            builder.append(" and ");
        }
        if (!builder.toString().isEmpty()) builder.append(" 1 = 1 ");
        getQueryDiemDanhGiaAsync(builder.toString());
    }

    private void getQueryDiemDanhGiaAsync(String whereClause) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mainActivity, android.R.style.Theme_Holo_Light_NoActionBar_Fullscreen);
        final View layout_table_tracuu = mainActivity.getLayoutInflater().inflate(R.layout.layout_title_listview, null);
        ListView listView = (ListView) layout_table_tracuu.findViewById(R.id.listview);
        final List<Feature> items = new ArrayList<>();
        final DanhSachDiemDanhGiaAdapter adapter = new DanhSachDiemDanhGiaAdapter(mainActivity, items);
        listView.setAdapter(adapter);

        builder.setView(layout_table_tracuu);
        final AlertDialog dialog = builder.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, final long id) {
                Feature selectedFeature = adapter.getItems().get(position);
                if (selectedFeature != null) {
                    dialog.dismiss();
                    popupInfos.setFeatureLayerDTG(featureLayerDTG);
                    popupInfos.showPopup((ArcGISFeature) selectedFeature);
                }
            }
        });
        TextView txtTongItem = (TextView) layout_table_tracuu.findViewById(R.id.txtTongItem);
        new QueryDiemDanhGiaAsync(mainActivity, serviceFeatureTable,txtTongItem, adapter, new QueryDiemDanhGiaAsync.AsyncResponse() {
            public void processFinish(List<Feature> features) {
                table_feature = features;
            }
        }).execute(whereClause);

    }

    private Feature getSelectedFeature(String OBJECTID) {
        Feature rt_feature = null;
        for (Feature feature : table_feature) {
            Object objectID = feature.getAttributes().get(mainActivity.getString(R.string.OBJECTID));
            if (objectID != null && objectID.toString().equals(OBJECTID)) {
                rt_feature = feature;
            }
        }
        return rt_feature;
    }

    private Object getCodeDomain(List<CodedValue> codedValues, String value) {
        Object code = null;
        for (CodedValue codedValue : codedValues) {
            if (codedValue.getName().equals(value)) {
                code = codedValue.getCode();
                break;
            }

        }
        return code;
    }

    private void editValueAttribute(final AdapterView<?> parent, View view, int position, final long id) {
        final TraCuuChiTietDiemDanhGiaAdapter.Item item = traCuuChiTietDiemDanhGiaAdapter.getItems().get(position);
        final Calendar[] calendar = new Calendar[2];
        final AlertDialog.Builder builder_editValue = new AlertDialog.Builder(mainActivity, android.R.style.Theme_Material_Light_Dialog_Alert);
        builder_editValue.setTitle("Tra cứu theo thuộc tính");
        builder_editValue.setMessage(item.getAlias());
        builder_editValue.setCancelable(false).setNegativeButton("Hủy", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        LayoutInflater inflater = mainActivity.getLayoutInflater();
        final LinearLayout layout = (LinearLayout) inflater.inflate(R.layout.layout_dialog_update_feature_listview, null);
        final LinearLayout layoutTextView = layout.findViewById(R.id.layout_edit_queryDateTime);
        final TextView txt_edit_viewmoreinfoBegin = layout.findViewById(R.id.txt_edit_viewmoreinfoBegin);
        final TextView txt_edit_viewmoreinfoEnd = layout.findViewById(R.id.txt_edit_viewmoreinfoEnd);
        ImageView img_selectTimeBegin = (ImageView) layout.findViewById(R.id.img_selectTimeBegin);
        ImageView img_selectTimeEnd = (ImageView) layout.findViewById(R.id.img_selectTimeEnd);
        final LinearLayout layoutEditText = layout.findViewById(R.id.layout_edit_viewmoreinfo_Editext);
        final EditText editText = layout.findViewById(R.id.etxt_edit_viewmoreinfo);
        final LinearLayout layoutSpin = layout.findViewById(R.id.layout_edit_viewmoreinfo_Spinner);
        final Spinner spin = layout.findViewById(R.id.spin_edit_viewmoreinfo);

        final Domain domain = serviceFeatureTable.getField(item.getFieldName()).getDomain();
        if (domain != null) {
            layoutSpin.setVisibility(View.VISIBLE);
            List<CodedValue> codedValues = ((CodedValueDomain) domain).getCodedValues();
            if (codedValues != null) {
                List<String> codes = new ArrayList<>();
                for (CodedValue codedValue : codedValues)
                    codes.add(codedValue.getName());
                ArrayAdapter<String> adapter = new ArrayAdapter<String>(layout.getContext(), android.R.layout.simple_list_item_1, codes);
                spin.setAdapter(adapter);
                if (item.getValue() != null) spin.setSelection(codes.indexOf(item.getValue()));

            }
        } else switch (item.getFieldType()) {
            case DATE:
                layoutTextView.setVisibility(View.VISIBLE);
                img_selectTimeBegin.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final View dialogView = View.inflate(mainActivity, R.layout.date_time_picker, null);
                        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(mainActivity).create();
                        dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
                                calendar[0] = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                                String date = String.format("%02d/%02d/%d", datePicker.getDayOfMonth(), datePicker.getMonth() + 1, datePicker.getYear());
                                txt_edit_viewmoreinfoBegin.setText(date);
                                alertDialog.dismiss();
                            }
                        });
                        alertDialog.setView(dialogView);
                        alertDialog.show();
                    }
                });
                img_selectTimeEnd.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final View dialogView = View.inflate(mainActivity, R.layout.date_time_picker, null);
                        final android.app.AlertDialog alertDialog = new android.app.AlertDialog.Builder(mainActivity).create();
                        dialogView.findViewById(R.id.date_time_set).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                DatePicker datePicker = (DatePicker) dialogView.findViewById(R.id.date_picker);
                                calendar[1] = new GregorianCalendar(datePicker.getYear(), datePicker.getMonth(), datePicker.getDayOfMonth());
                                String date = String.format("%02d/%02d/%d", datePicker.getDayOfMonth(), datePicker.getMonth() + 1, datePicker.getYear());
                                txt_edit_viewmoreinfoEnd.setText(date);
                                alertDialog.dismiss();
                            }
                        });
                        alertDialog.setView(dialogView);
                        alertDialog.show();
                    }
                });
                break;
            case OID:
                layoutEditText.setVisibility(View.VISIBLE);
                editText.setText(item.getValue());
                break;
            case TEXT:
                layoutEditText.setVisibility(View.VISIBLE);
                editText.setText(item.getValue());
                break;
            case SHORT:
                layoutEditText.setVisibility(View.VISIBLE);
                editText.setInputType(InputType.TYPE_CLASS_NUMBER);
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
                editText.setText(item.getValue());
                break;
            case DOUBLE:
                layoutEditText.setVisibility(View.VISIBLE);
                editText.setInputType(InputType.TYPE_NUMBER_FLAG_DECIMAL);
                editText.setFilters(new InputFilter[]{new InputFilter.LengthFilter(8)});
                editText.setText(item.getValue());
                break;
        }
        builder_editValue.setPositiveButton("Cập nhật", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (domain != null) {
                    item.setValue(spin.getSelectedItem().toString());
                } else {
                    switch (item.getFieldType()) {
                        case DATE:
                            item.setValue(txt_edit_viewmoreinfoBegin.getText().toString() + "-" + txt_edit_viewmoreinfoEnd.getText().toString());
                            item.setCalendarBegin(calendar[0]);
                            item.setCalendarEnd(calendar[1]);
                            break;
                        case DOUBLE:
                            try {
                                double x = Double.parseDouble(editText.getText().toString());
                                item.setValue(editText.getText().toString());
                            } catch (Exception e) {
                                Toast.makeText(mainActivity, mainActivity.getString(R.string.INCORRECT_INPUT_FORMAT), Toast.LENGTH_LONG).show();
                            }
                            break;
                        case OID:
                            item.setValue(editText.getText().toString());
                            break;
                        case TEXT:
                            item.setValue(editText.getText().toString());
                            break;
                        case SHORT:
                            try {
                                short x = Short.parseShort(editText.getText().toString());
                                item.setValue(editText.getText().toString());
                            } catch (Exception e) {
                                Toast.makeText(mainActivity, mainActivity.getString(R.string.INCORRECT_INPUT_FORMAT), Toast.LENGTH_LONG).show();
                            }
                            break;
                    }
                }
                TraCuuChiTietDiemDanhGiaAdapter adapter = (TraCuuChiTietDiemDanhGiaAdapter) parent.getAdapter();
                adapter.notifyDataSetChanged();
                dialog.dismiss();
            }
        });
        builder_editValue.setView(layout);
        AlertDialog dialog = builder_editValue.create();
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.show();
    }

    private void insertQueryField() {
        List<TraCuuChiTietDiemDanhGiaAdapter.Item> items = new ArrayList<>();
        String[] queryFields = featureLayerDTG.getQueryFields();
        List<Field> fields = serviceFeatureTable.getFields();
        for (Field field : fields) {
            if (queryFields[0].equals("*") || queryFields[0].equals("")) {
                TraCuuChiTietDiemDanhGiaAdapter.Item item = new TraCuuChiTietDiemDanhGiaAdapter.Item();
                item.setFieldName(field.getName());
                item.setAlias(field.getAlias());
                item.setFieldType(field.getFieldType());
                items.add(item);
            } else {
                for (String queryField : queryFields) {
                    if (field.getName().equals(queryField)) {
                        TraCuuChiTietDiemDanhGiaAdapter.Item item = new TraCuuChiTietDiemDanhGiaAdapter.Item();
                        item.setFieldName(field.getName());
                        item.setAlias(field.getAlias());
                        item.setFieldType(field.getFieldType());
                        items.add(item);
                        break;
                    }
                }
            }
        }

        traCuuChiTietDiemDanhGiaAdapter.clear();
        traCuuChiTietDiemDanhGiaAdapter.setItems(items);
        traCuuChiTietDiemDanhGiaAdapter.notifyDataSetChanged();
    }
}
