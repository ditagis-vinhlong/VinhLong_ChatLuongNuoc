package vinhlong.ditagis.com.qlcln

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.widget.CompoundButtonCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.darsh.multipleimageselect.activities.AlbumSelectActivity
import com.darsh.multipleimageselect.helpers.Constants
import com.darsh.multipleimageselect.models.Image
import com.esri.arcgisruntime.ArcGISRuntimeEnvironment
import com.esri.arcgisruntime.data.ArcGISFeature
import com.esri.arcgisruntime.data.Feature
import com.esri.arcgisruntime.data.ServiceFeatureTable
import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.SpatialReferences
import com.esri.arcgisruntime.layers.ArcGISMapImageLayer
import com.esri.arcgisruntime.layers.ArcGISMapImageSublayer
import com.esri.arcgisruntime.layers.FeatureLayer
import com.esri.arcgisruntime.loadable.LoadStatus
import com.esri.arcgisruntime.mapping.ArcGISMap
import com.esri.arcgisruntime.mapping.Basemap
import com.esri.arcgisruntime.mapping.Viewpoint
import com.esri.arcgisruntime.mapping.view.Callout
import com.esri.arcgisruntime.mapping.view.LocationDisplay
import com.esri.arcgisruntime.mapping.view.MapView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import kotlinx.android.synthetic.main.activity_quan_ly_chat_luong_nuoc.*
import ru.whalemare.sheetmenu.SheetMenu
import ru.whalemare.sheetmenu.layout.LinearLayoutProvider
import vinhlong.ditagis.com.qlcln.adapter.DanhSachDiemDanhGiaAdapter
import vinhlong.ditagis.com.qlcln.async.PreparingAsycn
import vinhlong.ditagis.com.qlcln.entities.DApplication
import vinhlong.ditagis.com.qlcln.entities.entitiesDB.ListObjectDB
import vinhlong.ditagis.com.qlcln.libs.Action
import vinhlong.ditagis.com.qlcln.libs.FeatureLayerDTG
import vinhlong.ditagis.com.qlcln.tools.TraCuu
import vinhlong.ditagis.com.qlcln.utities.*
import java.io.File
import java.io.IOException
import java.io.InputStream

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    private var mUri: Uri? = null
    private var mPopup: Popup? = null
    private var mMapView: MapView? = null
    private var mMap: ArcGISMap? = null
    private var mCallout: Callout? = null
    private var mFeatureLayerDTGS: MutableList<FeatureLayerDTG>? = null
    private var mMapViewHandler: MapViewHandler? = null
    private var mTxtSearch: SearchView? = null
    private var mListViewSearch: ListView? = null
    private var danhSachDiemDanhGiaAdapter: DanhSachDiemDanhGiaAdapter? = null
    private var taiSanImageLayers: ArcGISMapImageLayer? = null
    private var hanhChinhImageLayers: ArcGISMapImageLayer? = null
    private var mLinnearDisplayLayerTaiSan: LinearLayout? = null
    private var mLinnearDisplayLayerBaseMap: LinearLayout? = null
    private var mFloatButtonLayer: FloatingActionButton? = null
    private var mFloatButtonLocation: FloatingActionButton? = null
    private var cb_Layer_HanhChinh: CheckBox? = null
    private var cb_Layer_TaiSan: CheckBox? = null
    private var traCuu: TraCuu? = null
    private var states: Array<IntArray>? = null
    private var colors: IntArray? = null

    private var mLocationDisplay: LocationDisplay? = null
    private val requestCode = 2
    internal var reqPermissions = arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)

    private var mLocationHelper: LocationHelper? = null
    private val table_thoigiancln: ServiceFeatureTable? = null
    private var mLocation: Location? = null
    private var mApplication: DApplication? = null
    fun getMapViewHandler(): MapViewHandler? {
        return this.mMapViewHandler
    }

    fun getPopUp(): Popup? {
        return this.mPopup
    }
    fun isAddingFeatureOrChangingGeometry(): Boolean {
        return mApplication?.statusCode == Constant.StatusCode.IS_ADDING.value ||
                mApplication?.statusCode == Constant.StatusCode.IS_CHANGING_GEOMETRY.value
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quan_ly_chat_luong_nuoc)
        setLicense()
        mApplication = application as DApplication
        setUp()
        initListViewSearch()

        initLayerListView()


        setOnClickListener()
        startGPS()
        startSignIn()

    }

    private fun setLoginInfos() {
        val application = application as DApplication
        val displayName = application.user!!.displayName
        val navigationView = findViewById<NavigationView>(R.id.nav_view)
        navigationView.setNavigationItemSelectedListener(this)
        val headerLayout = navigationView.getHeaderView(0)
        val namenv = headerLayout.findViewById<TextView>(R.id.namenv)
        namenv.text = displayName
    }

    private fun startGPS() {

        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        mLocationHelper = LocationHelper(this, object : LocationHelper.AsyncResponse {
            override fun processFinish(longtitude: Double, latitude: Double) {
            }
        })
        if (!mLocationHelper!!.checkPlayServices()) {
            mLocationHelper!!.buildGoogleApiClient()
        }
        val listener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                mLocation = location
                mApplication!!.setmLocation(mLocation!!)
            }

            override fun onStatusChanged(s: String, i: Int, bundle: Bundle) {}

            override fun onProviderEnabled(s: String) {

            }

            override fun onProviderDisabled(s: String) {
                //                Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                //                startActivity(i);
                if (!mLocationHelper!!.checkPlayServices()) {
                    mLocationHelper!!.buildGoogleApiClient()
                }
            }
        }
        if (ActivityCompat.checkSelfPermission(this,
                        android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return
        }
        assert(locationManager != null)
        locationManager.requestLocationUpdates("gps", 5000, 0f, listener)
    }

    private fun startSignIn() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivityForResult(intent, Constant.REQUEST_LOGIN)
    }

    private fun setOnClickListener() {
        findViewById<View>(R.id.layout_layer_open_street_map).setOnClickListener(this)
        findViewById<View>(R.id.layout_layer_street_map).setOnClickListener(this)
        findViewById<View>(R.id.layout_layer_topo).setOnClickListener(this)
        findViewById<View>(R.id.floatBtnLayer).setOnClickListener(this)
        findViewById<View>(R.id.floatBtnAdd).setOnClickListener(this)
        findViewById<View>(R.id.btn_add_feature_close).setOnClickListener(this)
        findViewById<View>(R.id.btn_layer_close).setOnClickListener(this)
        findViewById<View>(R.id.img_layvitri).setOnClickListener(this)
        findViewById<View>(R.id.floatBtnLocation).setOnClickListener(this)
        findViewById<View>(R.id.floatBtnHome).setOnClickListener(this)
    }

    private fun initListViewSearch() {
        this.mListViewSearch = findViewById(R.id.lstview_search)
        //đưa listview search ra phía sau
        this.mListViewSearch!!.invalidate()
        val items = ArrayList<Feature>()
        this.danhSachDiemDanhGiaAdapter = DanhSachDiemDanhGiaAdapter(this@MainActivity, items)
        this.mListViewSearch!!.adapter = danhSachDiemDanhGiaAdapter
        this.mListViewSearch!!.onItemClickListener = AdapterView.OnItemClickListener { parent, view, position, id ->
            val feature = parent.getItemAtPosition(position) as Feature
            mPopup!!.showPopup(feature as ArcGISFeature)
            danhSachDiemDanhGiaAdapter!!.clear()
            danhSachDiemDanhGiaAdapter!!.notifyDataSetChanged()
        }
    }

    private fun setUp() {
        states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
        colors = intArrayOf(R.color.colorTextColor_1, R.color.colorTextColor_1)
        val builder = StrictMode.VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        val toolbar = findViewById<View>(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        requestPermisson()
        val drawer = container_main
        val toggle = ActionBarDrawerToggle(this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer.addDrawerListener(toggle)
        toggle.syncState()

        val navigationView = findViewById<View>(R.id.nav_view) as NavigationView
        navigationView.setNavigationItemSelectedListener(this)


    }

    private fun initMapView() {
        mMapView = findViewById(R.id.mapView)
        mMap = ArcGISMap(Basemap.Type.OPEN_STREET_MAP, LATITUDE, LONGTITUDE, LEVEL_OF_DETAIL)
        mMapView!!.map = mMap
        mCallout = mMapView!!.callout

        val preparingAsycn = PreparingAsycn(this, object : PreparingAsycn.AsyncResponse {
            override fun processFinish(output: Void?) {

                ListObjectDB.getInstance().lstFeatureLayerDTG
                setFeatureService()
            }
        })
        if (CheckConnectInternet.isOnline(this))
            preparingAsycn.execute()
        val edit_latitude = findViewById<View>(R.id.edit_latitude) as EditText
        val edit_longtitude = findViewById<View>(R.id.edit_longtitude) as EditText

        changeStatusOfLocationDataSource()
        mLocationDisplay!!.addLocationChangedListener { locationChangedEvent ->
            val position = locationChangedEvent.location.position
            edit_longtitude.setText(position.x.toString() + "")
            edit_latitude.setText(position.y.toString() + "")
            val geometry = GeometryEngine.project(position, SpatialReferences.getWebMercator())
            mMapView!!.setViewpointCenterAsync(geometry.extent.center)
        }

    }

    private fun initLayerListView() {
        findViewById<View>(R.id.layout_layer_open_street_map).setOnClickListener(this)
        findViewById<View>(R.id.layout_layer_street_map).setOnClickListener(this)
        findViewById<View>(R.id.layout_layer_topo).setOnClickListener(this)
        mFloatButtonLayer = findViewById(R.id.floatBtnLayer)
        mFloatButtonLayer!!.setOnClickListener(this)
        findViewById<View>(R.id.btn_layer_close).setOnClickListener(this)
        mFloatButtonLocation = findViewById(R.id.floatBtnLocation)
        mFloatButtonLocation!!.setOnClickListener(this)

        cb_Layer_HanhChinh = findViewById(R.id.cb_Layer_HanhChinh)
        cb_Layer_TaiSan = findViewById(R.id.cb_Layer_TaiSan)
        cb_Layer_TaiSan!!.setOnCheckedChangeListener { buttonView, isChecked ->
            for (i in 0 until mLinnearDisplayLayerTaiSan!!.childCount) {
                val view = mLinnearDisplayLayerTaiSan!!.getChildAt(i)
                if (view is CheckBox) {
                    if (isChecked)
                        view.isChecked = true
                    else
                        view.isChecked = false
                }
            }
        }
        cb_Layer_HanhChinh!!.setOnCheckedChangeListener { buttonView, isChecked ->
            for (i in 0 until mLinnearDisplayLayerBaseMap!!.childCount) {
                val view = mLinnearDisplayLayerBaseMap!!.getChildAt(i)
                if (view is CheckBox) {
                    if (isChecked)
                        view.isChecked = true
                    else
                        view.isChecked = false
                }
            }
        }
    }

    private fun setFeatureService() {
        if (ListObjectDB.getInstance().lstFeatureLayerDTG!!.isEmpty()) return
        mFeatureLayerDTGS = ArrayList()
        for (layerInfoDTG in ListObjectDB.getInstance().lstFeatureLayerDTG!!) {
            var url = layerInfoDTG.url
            if (!layerInfoDTG.url!!.startsWith("http"))
                url = "http:" + layerInfoDTG.url!!
            val serviceFeatureTable = ServiceFeatureTable(url!!)
            val featureLayer = FeatureLayer(serviceFeatureTable)
            featureLayer.name = layerInfoDTG.titleLayer
            featureLayer.maxScale = 0.0
            featureLayer.minScale = 1000000.0
            featureLayer.id = layerInfoDTG.id!!
            val action = Action(layerInfoDTG.isView, layerInfoDTG.isCreate, layerInfoDTG.isEdit, layerInfoDTG.isDelete)
            val featureLayerDTG = FeatureLayerDTG(featureLayer, layerInfoDTG.titleLayer, action)
            featureLayerDTG.outFields = getFieldsDTG(layerInfoDTG.outField)
            featureLayerDTG.queryFields = getFieldsDTG(layerInfoDTG.outField)
            featureLayerDTG.updateFields = getFieldsDTG(layerInfoDTG.outField)
            if (layerInfoDTG.id != null && layerInfoDTG.id == getString(R.string.id_diemdanhgianuoc)) {
                featureLayer.isPopupEnabled = true
                mMapViewHandler = MapViewHandler(featureLayerDTG, mMapView!!, this@MainActivity)
                traCuu = TraCuu(featureLayerDTG, this@MainActivity)
                mApplication?.featureLayerDiemDanhGia = featureLayer
                mFeatureLayerDTGS!!.add(featureLayerDTG)
                mMap!!.operationalLayers.add(featureLayer)
            }
            if (layerInfoDTG.id != null && layerInfoDTG.id == getString(R.string.id_maudanhgia)) {
                mFeatureLayerDTGS!!.add(featureLayerDTG)
            }
            if (layerInfoDTG.id!!.toUpperCase() == getString(R.string.IDLayer_Basemap)) {
                hanhChinhImageLayers = ArcGISMapImageLayer(url)
                hanhChinhImageLayers!!.id = layerInfoDTG.id!!
                mMapView!!.map.operationalLayers.add(hanhChinhImageLayers)
                hanhChinhImageLayers!!.addDoneLoadingListener {
                    if (hanhChinhImageLayers!!.loadStatus == LoadStatus.LOADED) {
                        val sublayerList = hanhChinhImageLayers!!.sublayers
                        for (sublayer in sublayerList) {
                            addCheckBox_SubLayer(sublayer as ArcGISMapImageSublayer, mLinnearDisplayLayerBaseMap!!)
                        }
                    }
                }
                hanhChinhImageLayers!!.loadAsync()

            } else if (taiSanImageLayers == null && layerInfoDTG.id == "truhongLYR") {
                taiSanImageLayers = ArcGISMapImageLayer(url.replaceFirst("FeatureServer(.*)".toRegex(), "MapServer"))
                taiSanImageLayers!!.name = layerInfoDTG.titleLayer
                taiSanImageLayers!!.id = layerInfoDTG.id!!
                //                    mArcGISMapImageLayerThematic.setMaxScale(0);
                //                    mArcGISMapImageLayerThematic.setMinScale(10000000);
                mMapView!!.map.operationalLayers.add(taiSanImageLayers)
                taiSanImageLayers!!.addDoneLoadingListener {
                    if (taiSanImageLayers!!.loadStatus == LoadStatus.LOADED) {
                        val sublayerList = taiSanImageLayers!!.sublayers
                        for (sublayer in sublayerList) {
                            addCheckBox_SubLayer(sublayer as ArcGISMapImageSublayer, mLinnearDisplayLayerTaiSan!!)
                        }
                    }
                }
                taiSanImageLayers!!.loadAsync()
            }


        }
        if (mFeatureLayerDTGS!!.size == 0) {
            mMapView?.let { MySnackBar.make(it, getString(R.string.no_access_permissions), true) }
            return
        }
        mPopup = Popup(this@MainActivity, mMapView!!, mFeatureLayerDTGS as ArrayList<FeatureLayerDTG>, mCallout!!)

        mMapViewHandler!!.popupInfos = mPopup
        traCuu!!.setPopupInfos(mPopup!!)
        mMap!!.addDoneLoadingListener {
            mLinnearDisplayLayerTaiSan = findViewById(R.id.linnearDisplayLayerTaiSan)
            mLinnearDisplayLayerBaseMap = findViewById(R.id.linnearDisplayLayerBaseMap)
            val linnearDisplayLayer = findViewById<View>(R.id.linnearDisplayLayer) as LinearLayout
            val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf())
            val colors = intArrayOf(R.color.colorTextColor_1, R.color.colorTextColor_1)
            for (layer in mFeatureLayerDTGS!!) {
                if (layer.featureLayer.id != null && layer.featureLayer.id == getString(R.string.id_diemdanhgianuoc)) {
                    val checkBox = CheckBox(linnearDisplayLayer.context)
                    checkBox.text = layer.titleLayer
                    checkBox.isChecked = true
                    CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList(states, colors))
                    linnearDisplayLayer.addView(checkBox)
                    checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
                        layer.featureLayer.isVisible = buttonView.isChecked
                    }
                }
            }
            for (i in 0 until linnearDisplayLayer.childCount) {
                val v = linnearDisplayLayer.getChildAt(i)
                if (v is CheckBox) {
                    v.isChecked = v.text == getString(R.string.alias_diemdanhgianuoc)
                }
            }
        }
    }

    private fun addCheckBox_SubLayer(layer: ArcGISMapImageSublayer, linearLayout: LinearLayout) {
        val checkBox = CheckBox(linearLayout.context)
        checkBox.text = layer.name
        checkBox.isChecked = false
        layer.isVisible = false
        CompoundButtonCompat.setButtonTintList(checkBox, ColorStateList(states, colors))
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (checkBox.isChecked) {
                if (buttonView.text == layer.name)
                    layer.isVisible = true


            } else {
                if (checkBox.text == layer.name)
                    layer.isVisible = false
            }
        }
        linearLayout.addView(checkBox)
    }

    private fun getFieldsDTG(stringFields: String?): Array<String>? {
        var returnFields: Array<String>? = null
        if (stringFields != null) {
            if (stringFields == "*" || stringFields == "") {
                returnFields = arrayOf("*")
            } else {
                returnFields = stringFields.split(",".toRegex()).dropLastWhile({ it.isEmpty() }).toTypedArray()
            }

        }
        return returnFields
    }

    private fun setLicense() {
        //way 1
        ArcGISRuntimeEnvironment.setLicense(getString(R.string.license))
    }

    private fun changeStatusOfLocationDataSource() {
        mLocationDisplay = mMapView!!.locationDisplay
        //        changeStatusOfLocationDataSource();
        mLocationDisplay!!.addDataSourceStatusChangedListener(LocationDisplay.DataSourceStatusChangedListener { dataSourceStatusChangedEvent ->
            // If LocationDisplay started OK, then continue.
            if (dataSourceStatusChangedEvent.isStarted) return@DataSourceStatusChangedListener

            // No error is reported, then continue.
            if (dataSourceStatusChangedEvent.error == null) return@DataSourceStatusChangedListener

            // If an error is found, handle the failure to start.
            // Check permissions to see if failure may be due to lack of permissions.
            val permissionCheck1 = ContextCompat.checkSelfPermission(this@MainActivity, reqPermissions[0]) == PackageManager.PERMISSION_GRANTED
            val permissionCheck2 = ContextCompat.checkSelfPermission(this@MainActivity, reqPermissions[1]) == PackageManager.PERMISSION_GRANTED

            if (!(permissionCheck1 && permissionCheck2)) {
                // If permissions are not already granted, request permission from the user.
                ActivityCompat.requestPermissions(this@MainActivity, reqPermissions, requestCode)
            } else {
                // Report other unknown failure types to the user - for example, location services may not
                // be enabled on the device.
                //                    String message = String.format("Error in DataSourceStatusChangedListener: %s", dataSourceStatusChangedEvent
                //                            .getSource().getLocationDataSource().getError().getMessage());
                //                    Toast.makeText(MainActivity.this, message, Toast.LENGTH_LONG).show();
            }
        })
    }

    override fun onBackPressed() {
        val drawer = findViewById<View>(R.id.container_main) as DrawerLayout
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.quan_ly_su_co, menu)
        mTxtSearch = menu.findItem(R.id.action_search).actionView as SearchView
        mTxtSearch!!.queryHint = getString(R.string.title_search)
        mTxtSearch!!.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                mListViewSearch?.let { mMapViewHandler!!.querySearch(query, it, danhSachDiemDanhGiaAdapter!!) }
                return false
            }

            override fun onQueryTextChange(newText: String): Boolean {
                if (newText.length == 0) {
                    danhSachDiemDanhGiaAdapter!!.clear()
                    danhSachDiemDanhGiaAdapter!!.notifyDataSetChanged()
                }
                return false
            }
        })
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId


        if (id == R.id.action_search) {
            this@MainActivity.mListViewSearch!!.visibility = View.VISIBLE
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        // Handle navigation view item clicks here.
        val id = item.itemId
        if (id == R.id.nav_thongke) {
            val intent = Intent(this, ThongKeActivity::class.java)
            this.startActivityForResult(intent, requestCode)
        } else if (id == R.id.nav_tracuu) {
            //            final Intent intent = new Intent(this, TraCuuActivity.class);
            //            this.startActivityForResult(intent, 1);
            traCuu!!.start()
        } else if (id == R.id.nav_logOut) {
            startSignIn()
        }
        val drawer = findViewById<DrawerLayout>(R.id.container_main)
        drawer.closeDrawer(GravityCompat.START)
        return true
    }

    private fun requestPermisson(): Boolean {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CALL_PHONE, Manifest.permission.READ_PHONE_STATE), REQUEST_ID_IMAGE_CAPTURE)
        }
        return !(Build.VERSION.SDK_INT >= 23 && ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED)
    }

    private fun goHome() {}

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            mLocationDisplay!!.startAsync()

        } else {
            Toast.makeText(this@MainActivity, resources.getString(R.string.location_permission_denied), Toast.LENGTH_SHORT).show()
        }
    }


    @SuppressLint("RestrictedApi")
    override fun onClick(v: View) {
        when (v.id) {
            R.id.floatBtnLayer -> {
                v.visibility = View.INVISIBLE
                (findViewById<View>(R.id.layout_layer) as LinearLayout).visibility = View.VISIBLE
            }
            R.id.layout_layer_open_street_map -> {
                mMapView!!.map.maxScale = 1128.497175
                mMapView!!.map.basemap = Basemap.createOpenStreetMap()
                handlingColorBackgroundLayerSelected(R.id.layout_layer_open_street_map)
            }
            R.id.layout_layer_street_map -> {
                mMapView!!.map.maxScale = 1128.497176
                mMapView!!.map.basemap = Basemap.createStreets()
                handlingColorBackgroundLayerSelected(R.id.layout_layer_street_map)
            }
            R.id.layout_layer_topo -> {
                mMapView!!.map.maxScale = 5.0
                mMapView!!.map.basemap = Basemap.createImageryWithLabels()
                handlingColorBackgroundLayerSelected(R.id.layout_layer_topo)
            }
            R.id.btn_layer_close -> {
                (findViewById<View>(R.id.layout_layer) as LinearLayout).visibility = View.INVISIBLE
                (findViewById<View>(R.id.floatBtnLayer) as FloatingActionButton).visibility = View.VISIBLE
            }

            R.id.floatBtnAdd -> {
                mApplication?.statusCode = Constant.StatusCode.IS_ADDING.value
                mApplication?.center = mMapView?.getCurrentViewpoint(Viewpoint.Type.CENTER_AND_SCALE)?.targetGeometry?.extent?.center
                mMapViewHandler?.addGraphic(mApplication?.center!!)

                SheetMenu(
                        context = this,
                        title = "Thêm ảnh",
                        menu = R.menu.selection_method_add_attachment, // you can just pass menu resource if you need static items
//  actions = listOf(ActionItem(id = 0, title = "Send mail", image = getDrawableIcon())), // or create ActionItem when you need dynamic titles of icons
//  actions = listOf("Send mail", "Send telegram", "Receive parcel"), // also, you can simplify it by passing strings for showing only text of items
                        layoutProvider = LinearLayoutProvider(), // linear layout enabled by default
//  layoutProvider = GridLayoutProvider() // but if you need grid, you can do it
//  layoutProvider = object: LayoutProvider { ... } // also, you can define your own layout
                        onClick = { item ->
                            run {
                                when (item.id) {
                                    R.id.selection_method_capture -> {
                                        mMapViewHandler!!.setClickBtnAdd(true)
                                        capture()
                                    }
                                    R.id.selection_method_pick -> {
                                        mMapViewHandler!!.setClickBtnAdd(true)
                                        pick()
                                    }
                                }
                            }
                        }
,
                        onCancel = { cancelAdd()}
                ).show(this)

            }
            R.id.btn_add_feature_close -> {
                (findViewById<View>(R.id.linear_addfeature) as LinearLayout).visibility = View.GONE
                (findViewById<View>(R.id.img_map_pin) as ImageView).visibility = View.GONE
                (findViewById<View>(R.id.floatBtnAdd) as FloatingActionButton).visibility = View.VISIBLE
                mMapViewHandler!!.setClickBtnAdd(false)
            }
            R.id.floatBtnLocation -> if (!mLocationDisplay!!.isStarted)
                mLocationDisplay!!.startAsync()
            else
                mLocationDisplay!!.stop()
            R.id.floatBtnHome -> goHome()
        }
    }

    fun capture() {
        mApplication?.bitmaps = null
        val cameraIntent = Intent(this, CameraActivity::class.java)
        startActivityForResult(cameraIntent, Constant.Request.CAMERA)


    }

    fun pick() {
        mApplication?.bitmaps = null
        val intent = Intent(this, AlbumSelectActivity::class.java)
//set limit on number of images that can be selected, default is 10
        intent.putExtra(Constants.INTENT_EXTRA_LIMIT, 3)
        startActivityForResult(intent, Constants.REQUEST_CODE);
    }

    private fun getBitmap(path: String?): Bitmap? {

        val uri = Uri.fromFile(File(path!!))
        var `in`: InputStream? = null
        try {
            val IMAGE_MAX_SIZE = 1200000 // 1.2MP
            `in` = contentResolver.openInputStream(uri)

            // Decode image size
            var o = BitmapFactory.Options()
            o.inJustDecodeBounds = true
            BitmapFactory.decodeStream(`in`, null, o)
            `in`!!.close()


            var scale = 1
            while (o.outWidth * o.outHeight * (1 / Math.pow(scale.toDouble(), 2.0)) > IMAGE_MAX_SIZE) {
                scale++
            }
            Log.d("", "scale = " + scale + ", orig-width: " + o.outWidth + ", orig-height: " + o.outHeight)

            var b: Bitmap? = null
            `in` = contentResolver.openInputStream(uri)
            if (scale > 1) {
                scale--
                // scale to max possible inSampleSize that still yields an image
                // larger than target
                o = BitmapFactory.Options()
                o.inSampleSize = scale
                b = BitmapFactory.decodeStream(`in`, null, o)

                // resize to desired dimensions
                val height = b!!.height
                val width = b.width
                Log.d("", "1th scale operation dimenions - width: $width, height: $height")

                val y = Math.sqrt(IMAGE_MAX_SIZE / (width.toDouble() / height))
                val x = y / height * width

                val scaledBitmap = Bitmap.createScaledBitmap(b, x.toInt(), y.toInt(), true)
                b.recycle()
                b = scaledBitmap

                System.gc()
            } else {
                b = BitmapFactory.decodeStream(`in`)
            }
            `in`!!.close()

            Log.d("", "bitmaps size - width: " + b!!.width + ", height: " + b.height)
            return b
        } catch (e: IOException) {
            Log.e("", e.message, e)
            return null
        }

    }

    @SuppressLint("ResourceAsColor")
    private fun handlingColorBackgroundLayerSelected(id: Int) {
        when (id) {
            R.id.layout_layer_open_street_map -> {
                (findViewById<View>(R.id.img_layer_open_street_map) as ImageView).setBackgroundResource(R.drawable.layout_shape_basemap)
                (findViewById<View>(R.id.txt_layer_open_street_map) as TextView).setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                (findViewById<View>(R.id.img_layer_street_map) as ImageView).setBackgroundResource(R.drawable.layout_shape_basemap_none)
                (findViewById<View>(R.id.txt_layer_street_map) as TextView).setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1))
                (findViewById<View>(R.id.img_layer_topo) as ImageView).setBackgroundResource(R.drawable.layout_shape_basemap_none)
                (findViewById<View>(R.id.txt_layer_topo) as TextView).setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1))
            }
            R.id.layout_layer_street_map -> {
                (findViewById<View>(R.id.img_layer_street_map) as ImageView).setBackgroundResource(R.drawable.layout_shape_basemap)
                (findViewById<View>(R.id.txt_layer_street_map) as TextView).setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                (findViewById<View>(R.id.img_layer_open_street_map) as ImageView).setBackgroundResource(R.drawable.layout_shape_basemap_none)
                (findViewById<View>(R.id.txt_layer_open_street_map) as TextView).setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1))
                (findViewById<View>(R.id.img_layer_topo) as ImageView).setBackgroundResource(R.drawable.layout_shape_basemap_none)
                (findViewById<View>(R.id.txt_layer_topo) as TextView).setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1))
            }
            R.id.layout_layer_topo -> {
                (findViewById<View>(R.id.img_layer_topo) as ImageView).setBackgroundResource(R.drawable.layout_shape_basemap)
                (findViewById<View>(R.id.txt_layer_topo) as TextView).setTextColor(ContextCompat.getColor(this, R.color.colorPrimary))
                (findViewById<View>(R.id.img_layer_open_street_map) as ImageView).setBackgroundResource(R.drawable.layout_shape_basemap_none)
                (findViewById<View>(R.id.txt_layer_open_street_map) as TextView).setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1))
                (findViewById<View>(R.id.img_layer_street_map) as ImageView).setBackgroundResource(R.drawable.layout_shape_basemap_none)
                (findViewById<View>(R.id.txt_layer_street_map) as TextView).setTextColor(ContextCompat.getColor(this, R.color.colorTextColor_1))
            }
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        try {
            val returnedResult = data?.extras!!.get(getString(R.string.ket_qua_objectid))!!.toString()
            if (resultCode == Activity.RESULT_OK) {
                mMapViewHandler!!.queryByObjectID(returnedResult)
            }
        } catch (e: Exception) {
        }

        when (requestCode) {
            Constants.REQUEST_CODE -> if (resultCode == RESULT_OK && data != null) {
                //The array list has the image paths of the selected images
                val images: ArrayList<Image> = data.getParcelableArrayListExtra(Constants.INTENT_EXTRA_IMAGES)
                var bitmaps = arrayListOf<Bitmap>()
                images.forEach { image ->
                    run {
                        val imgFile = File(image.path);

                        if (imgFile.exists()) {

                            val myBitmap = BitmapFactory.decodeFile(imgFile.absolutePath)
                            bitmaps.add(DBitmap().getDecreaseSizeBitmap(myBitmap))

                        }
                    }
                }
                mApplication?.bitmaps = bitmaps
                try {
                    if (mApplication?.featureLayerDiemDanhGia != null){
                        mPopup!!.showPopupAddFeatureOrChangeGeometry(mApplication?.center!!, mApplication?.selectedFeature,
                                mApplication?.featureLayerDiemDanhGia!!.featureTable as ServiceFeatureTable)
                    }
                    else{

                    }
                } catch (e: Exception) {
                    DAlertDialog().show(this@MainActivity, e)
                }
            }
            Constant.Request.CAMERA -> if (resultCode == Activity.RESULT_OK) {
//            }
                if (mApplication!!.bitmaps != null) {
                    try {
                        if (mApplication?.featureLayerDiemDanhGia != null){
                            mPopup!!.showPopupAddFeatureOrChangeGeometry(mApplication?.center!!, mApplication?.selectedFeature,
                                    mApplication?.featureLayerDiemDanhGia!!.featureTable as ServiceFeatureTable)
                        }
                        else{

                        }
                    } catch (e: Exception) {
                        DAlertDialog().show(this@MainActivity, e)
                    }
                }
//                    }
            } else {
                //todo: clear graphic
                cancelAdd()

                Snackbar.make(container_main, "Hủy chụp ảnh", Snackbar.LENGTH_LONG).show()
            }
            Constant.REQUEST_LOGIN -> if (Activity.RESULT_OK != resultCode) {
                finish()
                return
            } else {
                initMapView()
                setLoginInfos()
            }
        }
    }

    fun cancelAdd() {
        mApplication?.statusCode = Constant.StatusCode.CANCEL_ADD.value
        mMapViewHandler?.clearGraphics()
    }
    companion object {
        private val LATITUDE = 10.10299
        private val LONGTITUDE = 105.9295304
        private val LEVEL_OF_DETAIL = 12
        private val REQUEST_ID_IMAGE_CAPTURE = 55
    }
}