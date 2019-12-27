package vinhlong.ditagis.com.qlcln.async

import android.app.ProgressDialog
import android.content.Context
import android.os.AsyncTask
import com.esri.arcgisruntime.data.*
import vinhlong.ditagis.com.qlcln.adapter.FeatureViewMoreInfoAdapter
import vinhlong.ditagis.com.qlcln.utities.Constant
import java.text.ParseException
import java.util.*

/**
 * Created by ThanLe on 4/16/2018.
 */

class EditAsync(private val mContext: Context, private val mServiceFeatureTable: ServiceFeatureTable, selectedArcGISFeature: ArcGISFeature,
                private val mDelegate: AsyncResponse) : AsyncTask<FeatureViewMoreInfoAdapter, Any, Void?>() {
    private val dialog: ProgressDialog?
    private var mSelectedArcGISFeature: ArcGISFeature? = null

    interface AsyncResponse {
        fun processFinish(o: Any)
    }

    init {
        mSelectedArcGISFeature = selectedArcGISFeature
        dialog = ProgressDialog(mContext, android.R.style.Theme_Material_Dialog_Alert)
    }

    override fun onPreExecute() {
        super.onPreExecute()
        dialog!!.setMessage("Đang cập nhật...")
        dialog.setCancelable(false)

        dialog.show()

    }

    override fun doInBackground(vararg params: FeatureViewMoreInfoAdapter): Void? {
        val adapter = params[0]
        for (item in adapter.getItems()) {
            if (item.value == null) continue
            val domain = mSelectedArcGISFeature!!.featureTable.getField(item.fieldName!!).domain
            var codeDomain: Any? = null
            if (domain != null) {
                val codedValues = (this.mSelectedArcGISFeature!!.featureTable.getField(item.fieldName!!).domain as CodedValueDomain).codedValues
                codeDomain = getCodeDomain(codedValues, item.value)
            }
            if (item.fieldName == mSelectedArcGISFeature!!.featureTable.typeIdField) {
                val featureTypes = mSelectedArcGISFeature!!.featureTable.featureTypes
                val idFeatureTypes = getIdFeatureTypes(featureTypes, item.value)
                mSelectedArcGISFeature!!.attributes[item.fieldName] = java.lang.Short.parseShort(idFeatureTypes!!.toString())

            } else
                when (item.fieldType) {
                    Field.Type.DATE -> {
                        var date: Date? = null
                        try {
                            date = Constant.DATE_FORMAT.parse(item.value)
                            val c = Calendar.getInstance()
                            c.time = date
                            mSelectedArcGISFeature!!.attributes[item.fieldName] = c
                        } catch (e: ParseException) {
                        }

                    }

                    Field.Type.TEXT -> if (codeDomain != null) {
                        mSelectedArcGISFeature!!.attributes[item.fieldName] = codeDomain.toString()
                    } else
                        mSelectedArcGISFeature!!.attributes[item.fieldName] = item.value
                    Field.Type.SHORT -> if (codeDomain != null) {
                        mSelectedArcGISFeature!!.attributes[item.fieldName] = java.lang.Short.parseShort(codeDomain.toString())
                    } else
                        mSelectedArcGISFeature!!.attributes[item.fieldName] = java.lang.Short.parseShort(item.value)
                }
        }
        val currentTime = Calendar.getInstance()
        mSelectedArcGISFeature!!.attributes["NgayCapNhat"] = currentTime
        mServiceFeatureTable.loadAsync()
        mServiceFeatureTable.addDoneLoadingListener {
            try {
                // update feature in the feature table
                mServiceFeatureTable.updateFeatureAsync(mSelectedArcGISFeature!!).addDoneListener {
                    val applyEditsAsync = mServiceFeatureTable.applyEditsAsync()
                    applyEditsAsync.addDoneListener {
                        try {
                            val featureEditResults = applyEditsAsync.get()
                            if (featureEditResults.size > 0 && !featureEditResults[0].hasCompletedWithErrors()) {
                                publishProgress(featureEditResults[0].objectId)
                            } else
                                publishProgress()
                        } catch (e: Exception) {
                            publishProgress()
                        }
                    }
                }

            } catch (e: Exception) {
            }
        }
        return null
    }

    private fun getIdFeatureTypes(featureTypes: List<FeatureType>, value: String?): Any? {
        var code: Any? = null
        for (featureType in featureTypes) {
            if (featureType.name == value) {
                code = featureType.id
                break
            }
        }
        return code
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

    override fun onProgressUpdate(vararg values: Any) {
        super.onProgressUpdate(*values)
        if (values.isNotEmpty()) {
            mDelegate.processFinish(values[0])
        } else {
            mDelegate.processFinish(false)
        }
        if (dialog != null && dialog.isShowing) {
            dialog.dismiss()
        }
    }


}

