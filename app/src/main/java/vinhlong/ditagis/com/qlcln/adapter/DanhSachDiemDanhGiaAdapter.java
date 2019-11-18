
package vinhlong.ditagis.com.qlcln.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.esri.arcgisruntime.data.Feature;

import java.util.Calendar;
import java.util.List;

import vinhlong.ditagis.com.qlcln.R;
import vinhlong.ditagis.com.qlcln.utities.Constant;

public class DanhSachDiemDanhGiaAdapter extends ArrayAdapter<Feature> {
    private Context mContext;
    private List<Feature> items;


    public DanhSachDiemDanhGiaAdapter(Context context, List<Feature> items) {
        super(context, 0, items);
        this.mContext = context;
        this.items = items;
    }

    @NonNull
    @Override
    public Context getContext() {
        return mContext;
    }

    public void setContext(Context context) {
        this.mContext = context;
    }

    public List<Feature> getItems() {
        return items;
    }

    public void setItems(List<Feature> items) {
        this.items = items;
    }

    public void clear() {
        items.clear();
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @SuppressLint("ResourceAsColor")
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_tracuu, null);
        }
        Feature item = items.get(position);
        TextView txt_tracuu_id = (TextView) convertView.findViewById(R.id.txt_tracuu_id);
        TextView txt_tracuu_ngaycapnhat = (TextView) convertView.findViewById(R.id.txt_tracuu_ngaycapnhat);
        TextView txt_tracuu_diachi = (TextView) convertView.findViewById(R.id.txt_tracuu_diachi);

        Object idDiemDanhGia = item.getAttributes().get(mContext.getString(R.string.IDDIEMDANHGIA));
        if (idDiemDanhGia != null)
            txt_tracuu_id.setText(idDiemDanhGia.toString());

        Object ngayCapNhat = item.getAttributes().get(mContext.getString(R.string.NGAY_CAP_NHAT));

        if (ngayCapNhat != null) {
            String format_date = Constant.DATE_FORMAT.format(((Calendar) ngayCapNhat).getTime());
            txt_tracuu_ngaycapnhat.setText(format_date);
        }
        Object diaChi = item.getAttributes().get(mContext.getString(R.string.DIACHI));

        if (diaChi != null) {
            txt_tracuu_diachi.setText(diaChi.toString());
        }

        return convertView;
    }


}
