package vinhlong.ditagis.com.qlcln.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.Feature;
import com.esri.arcgisruntime.data.FeatureQueryResult;
import com.esri.arcgisruntime.data.QueryParameters;
import com.esri.arcgisruntime.data.ServiceFeatureTable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import vinhlong.ditagis.com.qlcln.MainActivity;

/**
 * Created by ThanLe on 4/16/2018.
 */

public class QueryFeatureAsync extends AsyncTask<QueryParameters, List<Feature>, Void> {
    private ProgressDialog dialog;
    private Context mContext;
    private ServiceFeatureTable serviceFeatureTable;


    public QueryFeatureAsync(MainActivity mainActivity, ServiceFeatureTable serviceFeatureTable, AsyncResponse asyncResponse) {
        this.mDelegate = asyncResponse;
        mContext = mainActivity;
        this.serviceFeatureTable = serviceFeatureTable;
        dialog = new ProgressDialog(mainActivity, android.R.style.Theme_Material_Dialog_Alert);
    }

    public interface AsyncResponse {
        void processFinish(List<Feature> o);
    }

    private AsyncResponse mDelegate = null;


    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage("Đang truy vấn...");
        dialog.setCancelable(false);
        dialog.show();

    }

    @Override
    protected Void doInBackground(QueryParameters... params) {
        final List<Feature> features = new ArrayList<>();
        QueryParameters queryParameters = params[0];
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
        super.onProgressUpdate(values);
        if (dialog != null && dialog.isShowing()) dialog.dismiss();
        if (values != null ) {
            mDelegate.processFinish(values[0]);

        } else {
            mDelegate.processFinish(new ArrayList<>());
        }
    }

}

