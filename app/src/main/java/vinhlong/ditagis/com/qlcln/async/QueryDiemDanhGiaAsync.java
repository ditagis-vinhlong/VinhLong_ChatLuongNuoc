package vinhlong.ditagis.com.qlcln.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.TextView;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import vinhlong.ditagis.com.qlcln.MainActivity;
import vinhlong.ditagis.com.qlcln.R;
import vinhlong.ditagis.com.qlcln.ThongKeActivity;
import vinhlong.ditagis.com.qlcln.adapter.DanhSachDiemDanhGiaAdapter;
import vinhlong.ditagis.com.qlcln.utities.Constant;

/**
 * Created by ThanLe on 4/16/2018.
 */

public class QueryDiemDanhGiaAsync extends AsyncTask<String, List<Feature>, Void> {
    private ProgressDialog dialog;
    private Context mContext;
    private ServiceFeatureTable serviceFeatureTable;
    private DanhSachDiemDanhGiaAdapter danhSachDiemDanhGiaAdapter;
    private TextView txtTongItem;

    public QueryDiemDanhGiaAsync(ThongKeActivity thongKeActivity, ServiceFeatureTable serviceFeatureTable, TextView txtTongItem, DanhSachDiemDanhGiaAdapter adapter, AsyncResponse asyncResponse) {
        this.delegate = asyncResponse;
        mContext = thongKeActivity;
        this.serviceFeatureTable = serviceFeatureTable;
        this.danhSachDiemDanhGiaAdapter = adapter;
        this.txtTongItem = txtTongItem;
        dialog = new ProgressDialog(thongKeActivity, android.R.style.Theme_Material_Dialog_Alert);
    }

    public QueryDiemDanhGiaAsync(MainActivity mainActivity, ServiceFeatureTable serviceFeatureTable, TextView txtTongItem, DanhSachDiemDanhGiaAdapter adapter, AsyncResponse asyncResponse) {
        this.delegate = asyncResponse;
        mContext = mainActivity;
        this.serviceFeatureTable = serviceFeatureTable;
        this.danhSachDiemDanhGiaAdapter = adapter;
        this.txtTongItem = txtTongItem;
        dialog = new ProgressDialog(mainActivity, android.R.style.Theme_Material_Dialog_Alert);
    }

    public interface AsyncResponse {
        void processFinish(List<Feature> features);
    }

    private AsyncResponse delegate = null;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage(mContext.getString(R.string.async_dang_xu_ly));
        dialog.setCancelable(false);
        dialog.show();

    }

    @Override
    protected Void doInBackground(String... params) {
        final List<Feature> features = new ArrayList<>();
        QueryParameters queryParameters = new QueryParameters();
        String queryClause = params[0];
        queryParameters.setWhereClause(queryClause);
        final ListenableFuture<FeatureQueryResult> queryResultListenableFuture = serviceFeatureTable.queryFeaturesAsync(queryParameters, ServiceFeatureTable.QueryFeatureFields.LOAD_ALL);
        queryResultListenableFuture.addDoneListener(new Runnable() {
            @Override
            public void run() {
                try {
                    FeatureQueryResult result = queryResultListenableFuture.get();
                    Iterator iterator = result.iterator();

                    while (iterator.hasNext()) {
                        Feature feature = (Feature) iterator.next();
                        features.add(feature);
                    }
                    publishProgress(features);

                } catch (Exception e) {
                   publishProgress(features);
                }
            }
        });
        return null;
    }

    @Override
    protected void onProgressUpdate(List<Feature>... values) {
        danhSachDiemDanhGiaAdapter.clear();
        danhSachDiemDanhGiaAdapter.setItems(values[0]);
        danhSachDiemDanhGiaAdapter.notifyDataSetChanged();
        if (txtTongItem != null)
            txtTongItem.setText(mContext.getString(R.string.nav_thong_ke_tong_diem) + values[0].size());
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
        super.onProgressUpdate(values);

    }


}

