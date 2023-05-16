package com.example.hikingdom.ui.main.hiking

//import com.example.hikingdom.ui.main.group.GroupFragment

import android.Manifest
import android.app.AlertDialog
import android.content.*
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.hikingdom.BuildConfig
import com.example.hikingdom.R
import com.example.hikingdom.data.local.AppDatabase
import com.example.hikingdom.data.remote.api.RetrofitTokenInstance
import com.example.hikingdom.data.remote.hiking.HikingRetrofitInterface
//import com.example.hikingdom.data.remote.hiking.Mountain
//import com.example.hikingdom.data.remote.hiking.MountainResponse
import com.example.hikingdom.databinding.FragmentHikingBinding
import com.example.hikingdom.ui.BaseFragment
//import com.example.hikingdom.ui.main.hiking.dialog.MountainAdapter
//import com.example.hikingdom.ui.main.hiking.dialog.MountainViewModel
import com.example.hikingdom.utils.*
import net.daum.mf.map.api.*
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime


class HikingFragment(): BaseFragment<FragmentHikingBinding>(FragmentHikingBinding::inflate)
//    , MapView.CurrentLocationEventListener
{
    private var locationService: LocationService? = null
    private val hikingViewModel : HikingViewModel by viewModels()

    private var bound: Boolean = false
    lateinit var startTime: LocalDateTime
    lateinit var finishTime: LocalDateTime

    lateinit var mapView: MapView
    lateinit var mapViewContainer: ConstraintLayout
    val customMarker = MapPOIItem()

    private var lastLat: Double = 0.0
    private var lastLng: Double = 0.0

    private val ZOOM_LEVEL = 2

    private val POLYLINE_COLOR_CODE = 0xFF0F7BDF.toInt()

    private val LOCATION_PERMISSION_CODE = 1
    private val BACKGROUND_LOCATION_PERMISSION_CODE = 2

    lateinit var hikingStartBtn: Button
    lateinit var hikingFinishBtn: Button
    lateinit var hikingSummitBtn: Button

    override fun initAfterBinding() {
        binding.lifecycleOwner = this
        binding.hikingFragmentViewModel = hikingViewModel

        mapView = MapView(requireContext())
        mapViewContainer = binding.hikingMapview

//        db = AppDatabase.getInstance(requireContext())!!    // 로컬 DB

        checkPermission()

    }

    companion object {
        fun newInstance(): HikingFragment = HikingFragment()
        const val  ACTION_STOP = "${BuildConfig.APPLICATION_ID}.stop"
    }

    // Service와 HikingFragment를 Binding
    /* Defines callbacks for service binding, passed to bindService()  */
    // bound service를 통해 Activity 등 구성요소를 서비스에 바인딩하고, 요청을 보내고, 응답을 수신하며, 프로세스 간 통신(IPC)을 실행할 수 있다.
    // HikingFragment의 액티비티인 MainActivity를 서비스에 바인딩한다.
    private val connection = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName, ibinder: IBinder) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = ibinder as LocationService.LocationBinder
            locationService = binder.getService()
            bound = true

            // 사용자의 실시간 위치 정보 화면에 띄워주기
            loadLocationInfo()
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            bound = false
        }
    }

    fun setHikingService(){
        hikingStartBtn = binding.hikingStartBtn
        hikingFinishBtn = binding.hikingFinishBtn
        hikingSummitBtn = binding.hikingSummitBtn

//        if(locationService != null && locationService?.isHikingStarted?.value == true){
//            Log.d("setHikingService", "true")
//            hikingFinishBtn.visibility = View.VISIBLE
//            hikingStartBtn.visibility = View.GONE
//        } else{
//
//            Log.d("setHikingService", "false / "+locationService.toString() + "/ "+locationService?.isHikingStarted?.value)
//        }

        if(getIsLocationServiceRunning()){  // 서비스를 실행중인지 여부를 LocationService에서 처리하여 sharedPreference에 저장해주고있다. 저장되어있는 값을 가져와서 실행중인지 확인 후 버튼을 띄워준다.
            showToast("등산 기록을 불러오는 중입니다.")

            // LocationService 이미 start된 상태이므로 binding만 해준다.
            bindHikingService()

            // 사용자의 실시간 위치 정보 화면에 띄워주기
            loadLocationInfo()
            hikingFinishBtn.visibility = View.VISIBLE
            hikingSummitBtn.visibility = View.VISIBLE
            hikingStartBtn.visibility = View.GONE
        }else{
            hikingFinishBtn.visibility = View.GONE
            hikingSummitBtn.visibility = View.GONE
            hikingStartBtn.visibility = View.VISIBLE
        }

        hikingStartBtn.setOnClickListener {
            startHikingService()
            showToast("등산 기록을 시작합니다.")
            hikingFinishBtn.visibility = View.VISIBLE
            hikingSummitBtn.visibility = View.VISIBLE
            hikingStartBtn.visibility = View.GONE

            saveIsSummit(false) // 완등인증 여부 초기화

            hikingViewModel.mountainSummitLat.value = 0.0  // 완등인증시 사용할 산 정상 위도 초기화
            hikingViewModel.mountainSummitLng.value = 0.0  // 완등인증시 사용할 산 정상 경도 초기화

            // TODO: 산 목록 api, 오늘 일정 조회 api 호출, 호출하고 응답 잘 받아왔다면 산 정상의 lat, lng 같이 받아온다. 받아온 lat, lng viewmodel에 저장해야함
//            hikingViewModel.mountainSummitLat.value = 37.5013   // 임시 값 setting
//            hikingViewModel.mountainSummitLng.value = 127.0395  // 임시 값 setting
        }

        hikingFinishBtn.setOnClickListener {
            val stopServiceIntent = Intent(activity, LocationService::class.java)
            stopServiceIntent.action = ACTION_STOP
            activity?.startService(stopServiceIntent)
            Intent(activity, LocationService::class.java).also{
                    intent ->
                activity?.unbindService(connection)
            }
            bound = false
            showToast("등산 기록을 종료합니다.")
            hikingFinishBtn.visibility = View.GONE
            hikingSummitBtn.visibility = View.GONE
            hikingStartBtn.visibility = View.VISIBLE

            finishTime = LocalDateTime.now()    // 종료시간 세팅
        }

        hikingSummitBtn.setOnClickListener {
            // 완등 인증 버튼을 누르면, 정상에 100m 이내일 시 인증완료. 인증 여부를 로컬에 저장
            if(getIsSummit()){
                showToast("이미 완등 인증을 완료하였습니다.")
                Log.d("saveIsSummit ", getIsSummit().toString()+"/ 이미 완등 인증을 완료")
            } else {
                if(checkIsSummit()){
                    saveIsSummit(true)
                    showToast("완등 인증이 완료되었습니다.")
                    Log.d("saveIsSummit", getIsSummit().toString()+"/ 완등 인증 완료")
                } else{
                    saveIsSummit(false)
                    showToast("산 정상에 도착 후 다시 인증해주세요.")
                }
            }
        }
    }

    fun loadLocationInfo() {

        locationService?.totalDistance?.observe(this) {
            Log.d("HikingFragment", it.toString())
            hikingViewModel.setTotalDistance(it)
        }

        locationService?.duration?.observe(this) {
            hikingViewModel.setDuration(it)
        }

        locationService?.currentLocation?.observe(this){
            hikingViewModel.setCurrentLocation(it)

            if (lastLat == 0.0 && lastLng == 0.0){
                Log.d("polyline test 1", StringBuilder("lastLat: ").append(lastLat.toString()).append("/ lastLng: ").append(lastLng.toString()).toString())
                lastLat = it.latitude
                lastLng = it.longitude

                setDepartMarker(lastLat, lastLng)
            }else{
                val polyline = MapPolyline()
                Log.d("polyline test 2", StringBuilder("lastLat: ").append(lastLat.toString()).append("/ lastLng: ").append(lastLng.toString())
                    .append(lastLng.toString()).append("/ lat: ").append(it.latitude).append("/ lng: ").append(it.longitude).toString())
                polyline.addPoint(MapPoint.mapPointWithGeoCoord(lastLat, lastLng));
                polyline.addPoint(MapPoint.mapPointWithGeoCoord(it.latitude, it.longitude))
                polyline.lineColor = POLYLINE_COLOR_CODE
                //                polyline.setLineColor(R.color.blue)
    //                polyline.lineColor = Color.rgb(255,204,0)
    //                polyline.lineColor = Color.rgb(15.0f,123.0f,223.0f)  // @color/blue 에 해당하는 rgb color
    //                polyline.lineColor = Color.argb(1, 15, 123, 223)
                mapView.addPolyline(polyline)
    //                mapView.fitMapViewAreaToShowPolyline(polyline)

                lastLat = it.latitude
                lastLng = it.longitude
            }
            val currentMapPoint = MapPoint.mapPointWithGeoCoord(lastLat, lastLng)
            mapView.setMapCenterPoint(currentMapPoint, true)
        }

        if(locationService?.isHikingStarted?.value == true){
            Log.d("setHikingService", "true")
            hikingFinishBtn.visibility = View.VISIBLE
            hikingStartBtn.visibility = View.GONE
        }else{
            Log.d("setHikingService", "false")
        }

//        locationService.lastLocation.observe(this){
//            hikingViewModel.setTotalAltitude(it.altitude)
//        }
    }

    private fun checkPermission() {
//        Log.d("checkPermission", "첫번쨰 확인")
        if (ContextCompat.checkSelfPermission(
                this.requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Fine Location permission is granted
            // Check if current android version >= 11, if >= 11 check for Background Location permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (ContextCompat.checkSelfPermission(
                        this.requireActivity(),
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    // Background Location Permission is granted so do your work here
//                    Log.d("checkPermission", "이미 백그라운드 허용됨")
                    setServiceAndMapView()
                } else {
                    // Ask for Background Location Permission
//                    Log.d("checkPermission", "이미 위치권한 허용됨, but 백그라운드 위치권한은 허용 X")
                    askPermissionForBackgroundUsage()
                }
            }
        } else {
            // Fine Location Permission is not granted so ask for permission
//            Log.d("checkPermission", "위치권한, 백그라운드 허용 X")
            askForLocationPermission()
        }
    }

    private val locationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            askPermissionForBackgroundUsage()
        } else {
            showToast("위치 권한을 허용하지 않으면 경로 기록 서비스를 이용하실 수 없습니다.")
        }
    }
    private val backgroundLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            setServiceAndMapView()
        } else {
            showToast("위치 권한을 항상 허용하지 않으면 백그라운드 경로 기록이 불가능합니다.")
        }
    }

    private fun askForLocationPermission() {
//        Log.d("askForLocationPermission", "진입")
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this.requireActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        ) {
//            Log.d("askForLocationPermission", "조건문 true")
            AlertDialog.Builder(this.requireContext())
                .setTitle("백그라운드 위치 권한 설정")
                .setMessage("경로 기록 서비스를 이용하려면 백그라운드 위치 권한을 허용해야 합니다.")
                .setPositiveButton("허용",
                    DialogInterface.OnClickListener { dialog, which ->
                        ActivityCompat.requestPermissions(
                            this.requireActivity(), arrayOf<String>(
                                Manifest.permission.ACCESS_FINE_LOCATION
                            ), LOCATION_PERMISSION_CODE
                        )
                    })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, which ->
                    // Permission is denied by the user
                })
                .create().show()
        } else {
//            Log.d("askForLocationPermission", "조건문 false")
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun askPermissionForBackgroundUsage() {
//        Log.d("askPermissionForBackgroundUsage", "진입")
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this.requireActivity(),
                Manifest.permission.ACCESS_BACKGROUND_LOCATION
            )
        ) {
//            Log.d("askPermissionForBackgroundUsage", "조건문 true")
            AlertDialog.Builder(this.requireContext())
                .setTitle("백그라운드 권한 설정")
                .setMessage("백그라운드 위치 권한을 설정하지 않으면, 백그라운드 경로 기록 서비스를 이용할 수 없어요.")
                .setPositiveButton("설정",
                    DialogInterface.OnClickListener { dialog, which ->
                        ActivityCompat.requestPermissions(
                            this.requireActivity(), arrayOf<String>(
                                Manifest.permission.ACCESS_BACKGROUND_LOCATION
                            ), BACKGROUND_LOCATION_PERMISSION_CODE
                        )
                    })
                .setNegativeButton("취소", DialogInterface.OnClickListener { dialog, which ->
                    // User declined for Background Location Permission.
                })
                .create().show()
        } else {
//            Log.d("askPermissionForBackgroundUsage", "조건문 false")
            backgroundLocationPermissionLauncher.launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
    }
    private fun setServiceAndMapView(){
        setHikingService()
        setMapView()
        if(getIsLocationServiceRunning()){
            Log.d("setServiceAndMapView", "locationService is not null")
            drawPolylineByExistingTrackingData()
        }else{
            Log.d("setServiceAndMapView", "locationService is null")
        }
        setMarkerSetting()
    }

    fun startHikingService(){
        val startServiceIntent = Intent(activity, LocationService::class.java)
//            ContextCompat.startForegroundService(this.requireContext(), Intent(this.requireContext(), LocationService::class.java))
        activity?.startService(startServiceIntent)
        Intent(activity, LocationService::class.java).also{
                intent ->
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    fun bindHikingService(){
        Intent(activity, LocationService::class.java).also{
                intent ->
            activity?.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
    }

    private fun setMapView(){
        mapViewContainer.addView(mapView)

//        Log.d("zoomLevel:",mapView.zoomLevelFloat.toString())
//        mapView.setZoomLevel(ZOOM_LEVEL, true);
//        mapView.isShowingCurrentLocationMarker
//        mapView.setCurrentLocationEventListener(this)
//        mapView.setMapViewEventListener(this)

        /* 현위치 트래킹 모드 및 나침반 모드를 설정한다.
        TrackingModeOnWithHeading: 현위치 트랙킹 모드 On + 나침반 모드 On, 단말의 위치에 따라 지도 중심이 이동하며 단말의 방향에 따라 지도가 회전한다. */
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithHeading //
//        if(mapView.isShowingCurrentLocationMarker){
//            Log.d("isShowing 1", "true")
//        }else{
//            Log.d("isShowing 1", "false")
//        }
        mapView.setShowCurrentLocationMarker(false)
//        if(mapView.isShowingCurrentLocationMarker){
//            Log.d("isShowing 2", "true")
//        }else{
//            Log.d("isShowing 2", "false")
//        }
//        LocationHelper().startListeningUserLocation(requireContext() , object : LocationHelper.HikingLocationListener {
//            override fun onLocationChanged(location: Location) {
//                // Here you got user location :)
//                Log.d("first Location","" + location.latitude + "," + location.longitude)
//                val currentMapPoint = MapPoint.mapPointWithGeoCoord(location.latitude, location.longitude)
//                //이 좌표로 지도 중심 이동
//                mapView.setMapCenterPoint(currentMapPoint, true)
//            }
//        })
//        mapView.setShowCurrentLocationMarker(false)

//        mapView.addPOIItem(customMarker)

    }

    private fun setMarkerSetting(){    // 마커를 찍기 위한 기본 세팅
        customMarker.itemName = "Custom Marker"
        customMarker.tag = 1
//        val mapPoint = MapPoint.mapPointWithGeoCoord(37.480426, 126.900177) //마커 표시할 위도경도
//        customMarker.mapPoint = MARKER_POINT
        customMarker.markerType = MapPOIItem.MarkerType.CustomImage // 마커타입을 커스텀 마커로 지정.

//        customMarker.customImageResourceId = R.drawable.custom_marker_red // 마커 이미지.

        customMarker.isCustomImageAutoscale = false // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.

        customMarker.setCustomImageAnchor(
            0.5f,
            1.0f
        ) // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.
    }

    private fun drawPolylineByExistingTrackingData(){   // viewmodel은 앱이 백그라운드로 가면 같이 죽기 때문에 당연히 비어있음. 로컬에서 가져오도록 변경해야함.
        val polyline = MapPolyline()
        polyline.lineColor = POLYLINE_COLOR_CODE  // @color/blue 에 해당하는 rgb color

        val existingLocationList = db?.userLocationDao()?.getUserLocations()

        if(!existingLocationList.isNullOrEmpty()){

            Log.d("existingLocationList", existingLocationList.size.toString()+" / "+existingLocationList.toString())

            val firstExistingLocation = existingLocationList[0]
            setDepartMarker(firstExistingLocation.latitude, firstExistingLocation.longitude)

            for(location in existingLocationList){
                polyline.addPoint(MapPoint.mapPointWithGeoCoord(location.latitude, location.longitude))
            }
            polyline.lineColor = POLYLINE_COLOR_CODE  // @color/blue 에 해당하는 rgb color
            mapView.addPolyline(polyline)

            val lastExistingLocation = existingLocationList.last()
            lastLat = lastExistingLocation.latitude
            lastLng = lastExistingLocation.longitude

//            // 지도뷰의 중심좌표와 줌레벨을 Polyline이 모두 나오도록 조정.
//            // 지도뷰의 중심좌표와 줌레벨을 Polyline이 모두 나오도록 조정.
//            val mapPointBounds = MapPointBounds(polyline.mapPoints)
//            val padding = 100 // px
//
//            mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding))
        }else{
            Log.d("existingLocationList", "is Null or Empty")
        }
    }

//    override fun onCurrentLocationUpdate(mv: MapView?, mapPoint: MapPoint?, accuracyInMeters: Float) {
//        val mapPointGeo: GeoCoordinate = mapPoint!!.mapPointGeoCoord
//        Log.i(
//            TAG,
//            String.format(
//                "MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)",
//                mapPointGeo.latitude,
//                mapPointGeo.longitude,
//                accuracyInMeters
//            )
//        )
//        val currentMapPoint = MapPoint.mapPointWithGeoCoord(mapPointGeo.latitude, mapPointGeo.longitude)
//        //이 좌표로 지도 중심 이동
//        mapView.setMapCenterPoint(currentMapPoint, true)
//        //트래킹 모드가 아닌 단순 현재위치 업데이트일 경우, 한번만 위치 업데이트하고 트래킹을 중단시키기 위한 로직
//        mapView.setShowCurrentLocationMarker(false)
//        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOff
//    }
//
//    override fun onCurrentLocationDeviceHeadingUpdate(p0: MapView?, p1: Float) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onCurrentLocationUpdateFailed(p0: MapView?) {
//        TODO("Not yet implemented")
//    }
//
//    override fun onCurrentLocationUpdateCancelled(p0: MapView?) {
//        TODO("Not yet implemented")
//    }

    private fun setDepartMarker(lat: Double, lng: Double){
        // 출발 좌표에 마커 그리기
//        val firstExistingLocation = existingLocationList[0]
        val customMarker = MapPOIItem()
        customMarker.itemName = "Custom Marker"
        customMarker.tag = 1
        customMarker.mapPoint = MapPoint.mapPointWithGeoCoord(lat, lng)
        customMarker.markerType = MapPOIItem.MarkerType.CustomImage // 마커타입을 커스텀 마커로 지정.

        customMarker.customImageResourceId = R.drawable.custom_marker_red_25 // 마커 이미지.

        customMarker.isCustomImageAutoscale = false // hdpi, xhdpi 등 안드로이드 플랫폼의 스케일을 사용할 경우 지도 라이브러리의 스케일 기능을 꺼줌.

        customMarker.setCustomImageAnchor(
            0.7f,
            1.0f
        ) // 마커 이미지중 기준이 되는 위치(앵커포인트) 지정 - 마커 이미지 좌측 상단 기준 x(0.0f ~ 1.0f), y(0.0f ~ 1.0f) 값.


        mapView.addPOIItem(customMarker)
    }

    private fun checkIsSummit(): Boolean {    // 현재 위치로 완등여부 확인
        val location = locationService?.currentLocation?.value
        if(location != null){
            val summitLocation = Location("")
            // 뷰모델에 저장된 산 정상 좌표 가져오기
            val summitLat = hikingViewModel.mountainSummitLat.value
            val summitLng = hikingViewModel.mountainSummitLng.value
            if (!((summitLat == 0.0) or (summitLng == 0.0))){
                summitLocation.latitude = summitLat!!
                summitLocation.longitude = summitLng!!

                val distance = summitLocation.distanceTo(location)
//            val distance = Location.distanceBetween(location.latitude, location.longitude, summitLocation.latitude, summitLocation.longitude)
                Log.d("distance", distance.toString())
                return distance < 200   // 200m 이내에 있으면 완등으로 판단
            }else{
                Log.d("checkIsSummit", "getMountainSummitLat() or getMountainSummitLng() is 0.0")
            }
        }
        return false
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d("fragment lifecycle", "onDestroyView")
    }

    override fun onPause() {
        super.onPause()
        Log.d("fragment lifecycle", "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.d("fragment lifecycle", "onStop")
        mapViewContainer.removeAllViews()
        Log.d("onStop LocationList", locationService?.locations?.value?.size.toString()+" / "+locationService?.locations?.value.toString())
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d("fragment lifecycle", "onDestroy")
        mapViewContainer?.removeAllViews()
    }

    override fun onDetach() {
        super.onDetach()
        Log.d("fragment lifecycle", "onDetach")
    }
}
