
package vinhlong.ditagis.com.qlcln.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.esri.arcgisruntime.data.Field;

import java.util.Calendar;
import java.util.List;

import vinhlong.ditagis.com.qlcln.R;
public class TraCuuChiTietDiemDanhGiaAdapter extends ArrayAdapter<TraCuuChiTietDiemDanhGiaAdapter.Item> {
    private Context context;
    private List<TraCuuChiTietDiemDanhGiaAdapter.Item> items;


    public TraCuuChiTietDiemDanhGiaAdapter(Context context, List<TraCuuChiTietDiemDanhGiaAdapter.Item> items) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public Context getContext() {
        return context;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
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
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.item_text_text_image, null);
        }
        Item item = items.get(position);
        TextView textViewItem1 = (TextView) convertView.findViewById(R.id.txtItem1);
        TextView textViewItem2 = (TextView) convertView.findViewById(R.id.txtItem2);
        textViewItem1.setText(item.getAlias());
        textViewItem2.setText(item.getValue());
        return convertView;
    }

    public static class Item{
        private String fieldName;
        private String alias;
        private String value;
        private Field.Type fieldType;
        private Calendar calendarBegin;
        private Calendar calendarEnd;

        public Item() {
        }

        public Item(String fieldName, String alias, String value) {
            this.fieldName = fieldName;
            this.alias = alias;
            this.value = value;
        }

        public Calendar getCalendarEnd() {
            return calendarEnd;
        }

        public void setCalendarEnd(Calendar calendarEnd) {
            this.calendarEnd = calendarEnd;
        }

        public String getFieldName() {
            return fieldName;
        }

        public void setFieldName(String fieldName) {
            this.fieldName = fieldName;
        }

        public String getAlias() {
            return alias;
        }

        public void setAlias(String alias) {
            this.alias = alias;
        }

        public String getValue() {
            return value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public Field.Type getFieldType() {
            return fieldType;
        }

        public void setFieldType(Field.Type fieldType) {
            this.fieldType = fieldType;
        }

        public Calendar getCalendarBegin() {
            return calendarBegin;
        }

        public void setCalendarBegin(Calendar calendarBegin) {
            this.calendarBegin = calendarBegin;
        }
    }

}
