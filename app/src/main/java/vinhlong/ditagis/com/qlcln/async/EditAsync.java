package vinhlong.ditagis.com.qlcln.async;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;

import com.esri.arcgisruntime.concurrent.ListenableFuture;
import com.esri.arcgisruntime.data.ArcGISFeature;
import com.esri.arcgisruntime.data.CodedValue;
import com.esri.arcgisruntime.data.CodedValueDomain;
import com.esri.arcgisruntime.data.Domain;
import com.esri.arcgisruntime.data.FeatureEditResult;
import com.esri.arcgisruntime.data.FeatureType;
import com.esri.arcgisruntime.data.ServiceFeatureTable;

import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

import vinhlong.ditagis.com.qlcln.R;
import vinhlong.ditagis.com.qlcln.adapter.FeatureViewMoreInfoAdapter;
import vinhlong.ditagis.com.qlcln.utities.Constant;

/**
 * Created by ThanLe on 4/16/2018.
 */

public class EditAsync extends AsyncTask<FeatureViewMoreInfoAdapter, Object, Void> {
    private ProgressDialog dialog;
    private Context mContext;
    private ServiceFeatureTable mServiceFeatureTable;
    private ArcGISFeature mSelectedArcGISFeature = null;
    private AsyncResponse mDelegate;

   public interface AsyncResponse {
        void processFinish(Object o);
    }

    public EditAsync(Context context, ServiceFeatureTable serviceFeatureTable, ArcGISFeature selectedArcGISFeature,
                     AsyncResponse response) {
        mContext = context;
        mServiceFeatureTable = serviceFeatureTable;
        mSelectedArcGISFeature = selectedArcGISFeature;
        dialog = new ProgressDialog(context, android.R.style.Theme_Material_Dialog_Alert);
        mDelegate = response;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        dialog.setMessage("Đang cập nhật...");
        dialog.setCancelable(false);

        dialog.show();

    }

    @Override
    protected Void doInBackground(FeatureViewMoreInfoAdapter... params) {
        FeatureViewMoreInfoAdapter adapter = params[0];
        for (FeatureViewMoreInfoAdapter.Item item : adapter.getItems()) {
            if (item.getValue() == null) continue;
            Domain domain = mSelectedArcGISFeature.getFeatureTable().getField(item.getFieldName()).getDomain();
            Object codeDomain = null;
            if (domain != null) {
                List<CodedValue> codedValues = ((CodedValueDomain) this.mSelectedArcGISFeature.getFeatureTable().getField(item.getFieldName()).getDomain()).getCodedValues();
                codeDomain = getCodeDomain(codedValues, item.getValue());
            }
            if (item.getFieldName().equals(mSelectedArcGISFeature.getFeatureTable().getTypeIdField())) {
                List<FeatureType> featureTypes = mSelectedArcGISFeature.getFeatureTable().getFeatureTypes();
                Object idFeatureTypes = getIdFeatureTypes(featureTypes, item.getValue());
                mSelectedArcGISFeature.getAttributes().put(item.getFieldName(), Short.parseShort(idFeatureTypes.toString()));

            } else switch (item.getFieldType()) {
                case DATE:
                    Date date = null;
                    try {
                        date = Constant.DATE_FORMAT.parse(item.getValue());
                        Calendar c = Calendar.getInstance();
                        c.setTime(date);
                        mSelectedArcGISFeature.getAttributes().put(item.getFieldName(), c);
                    } catch (ParseException e) {
                    }
                    break;

                case TEXT:
                    if (codeDomain != null) {
                        mSelectedArcGISFeature.getAttributes().put(item.getFieldName(), codeDomain.toString());
                    } else
                        mSelectedArcGISFeature.getAttributes().put(item.getFieldName(), item.getValue());
                    break;
                case SHORT:
                    if (codeDomain != null) {
                        mSelectedArcGISFeature.getAttributes().put(item.getFieldName(), Short.parseShort(codeDomain.toString()));
                    } else
                        mSelectedArcGISFeature.getAttributes().put(item.getFieldName(), Short.parseShort(item.getValue()));
                    break;
            }
        }
        Calendar currentTime = Calendar.getInstance();
        mSelectedArcGISFeature.getAttributes().put("NgayCapNhat", currentTime);
        mServiceFeatureTable.loadAsync();
        mServiceFeatureTable.addDoneLoadingListener(new Runnable() {
            @Override
            public void run() {
                try {
                    // update feature in the feature table
                    mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature).addDoneListener(new Runnable() {
                        @Override
                        public void run() {
                            ListenableFuture<List<FeatureEditResult>> applyEditsAsync = mServiceFeatureTable.applyEditsAsync();
                            applyEditsAsync.addDoneListener(new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        List<FeatureEditResult> featureEditResults = applyEditsAsync.get();
                                        if (featureEditResults.size() > 0 && !featureEditResults.get(0).hasCompletedWithErrors()){
                                            publishProgress(featureEditResults.get(0).getObjectId());
                                        }
                                        else publishProgress();
                                    } catch (Exception e) {
                                        publishProgress();
                                    }

                                }
                            });
                        }
                    });

                } catch (Exception e) {
                }
            }
        });
        return null;
    }

    private Object getIdFeatureTypes(List<FeatureType> featureTypes, String value) {
        Object code = null;
        for (FeatureType featureType : featureTypes) {
            if (featureType.getName().equals(value)) {
                code = featureType.getId();
                break;
            }
        }
        return code;
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

    @Override
    protected void onProgressUpdate(Object... values) {
        super.onProgressUpdate(values);
        if (values != null && values.length > 0) {
            mDelegate.processFinish(values[0]);
        } else {
            mDelegate.processFinish(false);
        }
        if (dialog != null && dialog.isShowing()) {
            dialog.dismiss();
        }
    }


}

