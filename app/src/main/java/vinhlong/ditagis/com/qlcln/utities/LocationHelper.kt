package vinhlong.ditagis.com.qlcln.utities

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.content.IntentSender
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.AsyncTask
import android.os.Bundle
import android.util.Log
import android.widget.Toast


import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.GoogleApiAvailability
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.common.api.PendingResult
import com.google.android.gms.common.api.ResultCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResult
import com.google.android.gms.location.LocationSettingsStatusCodes

import java.io.IOException
import java.util.ArrayList
import java.util.Locale

/**
 * Created by ThanLe on 4/16/2018.
 */

class LocationHelper(private val mContext: Context, delegate: AsyncResponse) : AsyncTask<Void, Any, Void>(), GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {
    private val current_activity: Activity

    private val isPermissionGranted: Boolean = false

    private var mLastLocation: Location? = null

    // Google client to interact with Google API

    var googleApiClient: GoogleApiClient? = null
        private set
    // list of permissions

    private val permissions = ArrayList<String>()
    private val permissionUtils: PermissionUtils? = null
    private val mDialog: ProgressDialog? = null

    var delegate: AsyncResponse? = null

    /**
     * Method to display the location on UI
     */

    val location: Location?
        get() {

            if (isPermissionGranted) {

                try {
                    mLastLocation = LocationServices.FusedLocationApi
                            .getLastLocation(googleApiClient)

                    return mLastLocation
                } catch (e: SecurityException) {
                    e.printStackTrace()

                }

            }

            return null

        }

    // All location settings are satisfied. The client can initialize location requests here
    // Show the dialog by calling startResolutionForResult(),
    // and check the result in onActivityResult().
    // Ignore the error.
    val stateLocation: Boolean
        get() {
            @SuppressLint("RestrictedApi") val mLocationRequest = LocationRequest()
            mLocationRequest.interval = 10000
            mLocationRequest.fastestInterval = 5000
            mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

            val builder = LocationSettingsRequest.Builder()
                    .addLocationRequest(mLocationRequest)
            val result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())

            result.setResultCallback { locationSettingsResult ->
                val status = locationSettingsResult.status

                when (status.statusCode) {
                    LocationSettingsStatusCodes.SUCCESS -> mLastLocation = location
                    LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                        status.startResolutionForResult(current_activity, REQUEST_CHECK_SETTINGS)

                    } catch (e: IntentSender.SendIntentException) {
                    }

                    LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                    }
                }
            }
            return false
        }

    override fun onConnected(bundle: Bundle?) {

    }

    override fun onConnectionSuspended(i: Int) {

    }

    override fun onConnectionFailed(connectionResult: ConnectionResult) {

    }

    interface AsyncResponse {
        fun processFinish(longtitude: Double, latitude: Double)
    }

    init {
        this.delegate = delegate
        this.current_activity = mContext as Activity

        //        permissionUtils = new PermissionUtils(context, this);

        permissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        permissions.add(Manifest.permission.ACCESS_COARSE_LOCATION)

    }

    /**
     * Method to check the availability of location permissions
     */

    fun checkpermission() {
        //        permissionUtils.check_permission(permissions, "Need GPS permission for getting your location", 1);
    }

    /**
     * Method to verify google play services on the device
     */

    fun checkPlayServices(): Boolean {

        val googleApiAvailability = GoogleApiAvailability.getInstance()

        val resultCode = googleApiAvailability.isGooglePlayServicesAvailable(mContext)

        if (resultCode != ConnectionResult.SUCCESS) {
            if (googleApiAvailability.isUserResolvableError(resultCode)) {
                googleApiAvailability.getErrorDialog(current_activity, resultCode,
                        PLAY_SERVICES_REQUEST).show()
            } else {
                showToast("This device is not supported.")
            }
            return false
        }
        return true
    }

    fun getAddress(latitude: Double, longitude: Double): Address? {
        val geocoder: Geocoder
        val addresses: List<Address>
        geocoder = Geocoder(mContext, Locale.getDefault())

        try {
            addresses = geocoder.getFromLocation(latitude, longitude, 1) // Here 1 represent max location result to returned, by documents it recommended 1 to 5
            return addresses[0]

        } catch (e: IOException) {
            e.printStackTrace()
        }

        return null

    }


    /**
     * Method used to build GoogleApiClient
     */

    fun buildGoogleApiClient() {
        googleApiClient = GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(current_activity as GoogleApiClient.ConnectionCallbacks)
                .addOnConnectionFailedListener(current_activity as GoogleApiClient.OnConnectionFailedListener)
                .addApi(LocationServices.API).build()

        googleApiClient!!.connect()

        @SuppressLint("RestrictedApi") val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 10000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest)

        val result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())

        result.setResultCallback { locationSettingsResult ->
            val status = locationSettingsResult.status

            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS ->
                    // All location settings are satisfied. The client can initialize location requests here
                    mLastLocation = location
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    status.startResolutionForResult(current_activity, REQUEST_CHECK_SETTINGS)

                } catch (e: IntentSender.SendIntentException) {
                    // Ignore the error.
                }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                }
            }
        }


    }

    /**
     * Method used to connect GoogleApiClient
     */
    fun connectApiClient() {
        googleApiClient!!.connect()
    }

    /**
     * Method used to get the GoogleApiClient
     */
    fun getGoogleApiCLient(): GoogleApiClient? {
        return googleApiClient
    }


    /**
     * Handles the permission results
     */
    fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        permissionUtils!!.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    /**
     * Handles the activity results
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        when (requestCode) {
            REQUEST_CHECK_SETTINGS -> when (resultCode) {
                Activity.RESULT_OK ->
                    // All required changes were successfully made
                    mLastLocation = location
                Activity.RESULT_CANCELED -> {
                }
                else -> {
                }
            }// The user was asked to change settings, but chose not to
        }
    }

    //
    //    @Override
    //    public void PermissionGranted(int request_code) {
    //        Log.i("PERMISSION", "GRANTED");
    //        isPermissionGranted = true;
    //    }
    //
    //    @Override
    //    public void PartialPermissionGranted(int request_code, ArrayList<String> granted_permissions) {
    //        Log.i("PERMISSION PARTIALLY", "GRANTED");
    //    }
    //
    //    @Override
    //    public void PermissionDenied(int request_code) {
    //        Log.i("PERMISSION", "DENIED");
    //    }
    //
    //    @Override
    //    public void NeverAskAgain(int request_code) {
    //        Log.i("PERMISSION", "NEVER ASK AGAIN");
    //    }


    private fun showToast(message: String) {
        Toast.makeText(mContext, message, Toast.LENGTH_SHORT).show()
    }


    override fun doInBackground(vararg voids: Void): Void? {
        googleApiClient = GoogleApiClient.Builder(mContext)
                .addConnectionCallbacks(current_activity as GoogleApiClient.ConnectionCallbacks)
                .addOnConnectionFailedListener(current_activity as GoogleApiClient.OnConnectionFailedListener)
                .addApi(LocationServices.API).build()

        googleApiClient!!.connect()

        @SuppressLint("RestrictedApi") val mLocationRequest = LocationRequest()
        mLocationRequest.interval = 10000
        mLocationRequest.fastestInterval = 5000
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val builder = LocationSettingsRequest.Builder()
                .addLocationRequest(mLocationRequest)

        val result = LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())

        result.setResultCallback { locationSettingsResult ->
            val status = locationSettingsResult.status

            when (status.statusCode) {
                LocationSettingsStatusCodes.SUCCESS -> {
                    // All location settings are satisfied. The client can initialize location requests here
                    mLastLocation = location
                    if (mLastLocation != null)
                        delegate!!.processFinish(mLastLocation!!.longitude, mLastLocation!!.latitude)
                }
                LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    status.startResolutionForResult(current_activity, REQUEST_CHECK_SETTINGS)

                } catch (e: IntentSender.SendIntentException) {
                    // Ignore the error.
                }

                LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                }
            }
            publishProgress()
        }
        return null
    }

    override fun onProgressUpdate(vararg values: Any) {
        super.onProgressUpdate(*values)
    }

    override fun onPreExecute() {
        super.onPreExecute()
    }

    override fun onPostExecute(aVoid: Void) {
        super.onPostExecute(aVoid)

    }

    companion object {

        private val PLAY_SERVICES_REQUEST = 1000
        private val REQUEST_CHECK_SETTINGS = 2000
    }
}

