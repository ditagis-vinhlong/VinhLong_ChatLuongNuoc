package vinhlong.ditagis.com.qlcln.async

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.os.AsyncTask
import android.util.Log
import android.widget.Toast
import vinhlong.ditagis.com.qlcln.entities.DAddress
import java.io.IOException
import java.util.*

class FindLocationTask(@field:SuppressLint("StaticFieldLeak")
                        private val mContext: Context, private val mIsFromLocationName: Boolean,
                       private val mDelegate: AsyncResponse) : AsyncTask<String, Void, List<DAddress>?>() {
    private val mGeocoder: Geocoder = Geocoder(mContext, Locale.getDefault())
    private var mLongtitude: Double = 0.toDouble()
    private var mLatitude: Double = 0.toDouble()

    interface AsyncResponse {
        fun processFinish(output: List<DAddress>?)
    }

    fun setLongtitude(mLongtitude: Double) {
        this.mLongtitude = mLongtitude
    }

    fun setLatitude(mLatitude: Double) {
        this.mLatitude = mLatitude
    }

    override fun doInBackground(vararg params: String): List<DAddress>? {
        if (!Geocoder.isPresent())
            return null
        val lstLocation = ArrayList<DAddress>()
        if (mIsFromLocationName) {
            val text = params[0]
            try {
                val addressList = mGeocoder.getFromLocationName(text, 5)

                for (address in addressList)
                    lstLocation.add(DAddress(address.longitude, address.latitude,
                            address.subAdminArea, address.adminArea, address.getAddressLine(0)))
            } catch (ignored: IOException) {
               return null
            }

        } else {
            try {
                val addressList = mGeocoder.getFromLocation(mLatitude, mLongtitude, 1)
                for (address in addressList)
                    lstLocation.add(DAddress(address.longitude, address.latitude,
                            address.subAdminArea, address.adminArea, address.getAddressLine(0)))
            } catch (ignored: IOException) {
                return null
            }

        }


        return lstLocation
    }

    override fun onPostExecute(addressList: List<DAddress>?) {
        super.onPostExecute(addressList)
        if (addressList == null)
            Toast.makeText(mContext,"Cần cập nhật phiên bản Google+ mới nhất để dùng tính năng này", Toast.LENGTH_LONG).show()
        assert(addressList != null)
        this.mDelegate.processFinish(addressList)
    }
}
