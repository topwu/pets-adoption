package com.topwu.petsadopt

import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.ContextCompat
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.karumi.dexter.Dexter
import com.karumi.dexter.listener.single.DialogOnDeniedPermissionListener
import com.topwu.petsadopt.BaliPlacesAdapter.OnPlaceClickListener
import com.topwu.petsadopt.anim.ScaleDownImageTransition
import com.topwu.petsadopt.model.pet.Pet
import com.topwu.petsadopt.model.pet.Shelter
import com.topwu.petsadopt.util.MapBitmapCache
import com.topwu.petsadopt.widgets.HorizontalRecyclerViewScrollListener
import com.topwu.petsadopt.widgets.HorizontalRecyclerViewScrollListener.OnItemCoverListener
import com.topwu.petsadopt.widgets.TranslateItemAnimator
import kotlinx.android.synthetic.main.fragment_map.*


class MapFragment : Fragment(), MapFragmentView, OnMapReadyCallback, OnPlaceClickListener, OnItemCoverListener {

    private val LOGTAG = MapFragment::class.java.simpleName
    private val MIN_TIME_BW_UPDATES = 1000 * 60 * 1L // 1 minute
    private val MIN_DISTANCE_CHANGE_FOR_UPDATES = 10f // 10 meters
    private val PERMISSIONS_REQUEST_FINE_LOCATION = 1024

    companion object {
        fun newInstance(context: Context): MapFragment {
            val fragment = MapFragment()
            val transition = ScaleDownImageTransition(context, MapBitmapCache.instance.getBitmap())
            transition.addTarget(context.getString(R.string.mapPlaceholderTransition))
            transition.duration = 600
            fragment.enterTransition = transition
            return fragment
        }
    }

    private lateinit var presenter: MapFragmentPresenter
    private lateinit var baliAdapter: BaliPlacesAdapter
    private lateinit var locationManager: LocationManager
    private var currentLatLng: LatLng? = null
    private var showRoute = false

    private val shelters = ArrayList<Shelter>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val dialogPermissionListener = DialogOnDeniedPermissionListener.Builder
                .withContext(context)
                .withTitle("GPS permission")
                .withMessage("GPS permission is needed to get your current position")
                .withButtonText(android.R.string.ok)
                .build()

        Dexter.withActivity(activity)
                .withPermission(ACCESS_FINE_LOCATION)
                .withListener(dialogPermissionListener)
                .check()

        locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fragment_map, container, false)
        return view
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        presenter = MapFragmentPresenterImpl()
        presenter.attachView(this)
        presenter.provideBaliData()

        recyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        baliAdapter = BaliPlacesAdapter(this, context)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        val fragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        fragment.getMapAsync(this)
    }

    override fun onResume() {
        super.onResume()

        if (ContextCompat.checkSelfPermission(context, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(context, ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(ACCESS_COARSE_LOCATION, ACCESS_FINE_LOCATION), PERMISSIONS_REQUEST_FINE_LOCATION)
        } else {
            val isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
            val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

            if (isNetworkEnabled) {
                locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
                        MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener)
            } else if (isGPSEnabled) {
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                        MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, locationListener)
            }
        }
    }

    override fun onPause() {
        super.onPause()

        locationManager.removeUpdates(locationListener)
    }

    private val locationListener: LocationListener = object : LocationListener {
        override fun onLocationChanged(location: Location) {
            Log.e(LOGTAG, "location changed: ${location.latitude}, ${location.longitude}")
            currentLatLng = LatLng(location.latitude, location.longitude)
        }

        override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
            Log.e(LOGTAG, "onStatusChanged")
        }

        override fun onProviderEnabled(provider: String) {
            Log.e(LOGTAG, "onProviderEnabled")
        }

        override fun onProviderDisabled(provider: String) {
            Log.e(LOGTAG, "onProviderDisabled")
        }
    }

    override fun onDestroy() {
        presenter.detachView()
        super.onDestroy()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        if (googleMap != null) {
            val childFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
            childFragment.view?.viewTreeObserver?.addOnGlobalLayoutListener(OnMapGlobalLayoutListener(this, childFragment, googleMap))

            presenter.getPets()
        }
    }

    class OnMapGlobalLayoutListener(private val fragment: MapFragment,
                                    private val childFragment: SupportMapFragment,
                                    private val googleMap: GoogleMap) : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            childFragment.view?.viewTreeObserver?.removeOnGlobalLayoutListener(this)
            fragment.mapOverlayLayout.setupMap(googleMap)

            fragment.presenter.moveMapAndAddMarker()
            fragment.addDataToRecyclerView()
        }
    }

    private fun addDataToRecyclerView() {
        recyclerView.itemAnimator = TranslateItemAnimator()
        recyclerView.adapter = baliAdapter
        recyclerView.addOnScrollListener(HorizontalRecyclerViewScrollListener(this))
    }

    override fun onPlaceClicked(sharedView: View, transitionName: String, position: Int) {
        presenter.clearRoute()

        if (currentLatLng != null) {
            val shelter = baliAdapter.getPetList()[position].shelter
            val shelterLatLng = LatLng(shelter.latitude, shelter.longitude)
            presenter.drawRoute(currentLatLng as LatLng, shelterLatLng)
            hideAllMarkers()

            showRoute = true
        }
    }

    private fun hideAllMarkers() {
        mapOverlayLayout.setOnCameraIdleListener(null)
        mapOverlayLayout.hideAllMarkers()
    }

    override fun onItemCover(position: Int) {
        val pets = baliAdapter.getPetList()

        shelters.find { it.shelterId == pets[position].shelter.shelterId } ?.let {
            mapOverlayLayout.showMarker(shelters.indexOf(it))
        }
    }

    override fun clearPolylinesOnMap() {
        activity.runOnUiThread {
            mapOverlayLayout.removeCurrentPolyline()
        }
    }

    override fun drawPolylinesOnMap(polylines: ArrayList<LatLng>) {
        activity.runOnUiThread {
            mapOverlayLayout.addPolyline(polylines)
        }
    }

    override fun provideShelters(shelters: List<Shelter>) {
        this.shelters.clear()
        this.shelters.addAll(shelters)
    }

    override fun providePets(pets: List<Pet>) {
        activity.runOnUiThread {
            baliAdapter.setPetList(pets)
        }
    }

    override fun onBackPressedWithScene(latLngBounds: LatLngBounds) {
    }

    override fun moveMapAndAddMaker(latLngs: List<LatLng>, latLngBounds: LatLngBounds) {
        activity.runOnUiThread {
            mapOverlayLayout.moveCamera(latLngBounds)
            mapOverlayLayout.setOnCameraIdleListener(GoogleMap.OnCameraIdleListener {
                latLngs.forEachIndexed({ i, latLng ->
                    mapOverlayLayout.createAndShowMarker(i, latLng)
                })
                mapOverlayLayout.setOnCameraIdleListener(null)
            })
            mapOverlayLayout.setOnCameraMoveListener(GoogleMap.OnCameraMoveListener { mapOverlayLayout.refresh() })
        }
    }

    override fun updateMapZoomAndRegion(northeastLatLng: LatLng, southwestLatLng: LatLng) {
        activity.runOnUiThread {
            mapOverlayLayout.animateCamera(LatLngBounds(southwestLatLng, northeastLatLng))
            mapOverlayLayout.setOnCameraIdleListener(GoogleMap.OnCameraIdleListener {
                mapOverlayLayout.drawStartAndFinishMarker()
            })
        }
    }

    fun onBackPressed(): Boolean {
        if (!showRoute) {
            return false
        }

        val latLngBounds = baliAdapter.getPetList()
                .map { it.shelter }
                .filterNot { it.latitude == 0.0 && it.longitude == 0.0 }
                .distinct()
                .fold(LatLngBounds.Builder()) { builder, it ->
                    val latLng = LatLng(it.latitude, it.longitude)
                    builder.include(latLng)
                }
                .build()
        mapOverlayLayout.onBackPressed(latLngBounds)
        showRoute = false

        return true
    }
}