package com.example.myweatherapp_kotlin

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.GestureDetector
import android.view.KeyEvent
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import com.example.weatherapp.WeatherAPI
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity(), View.OnTouchListener {

    var longitude: Double = 0.0
    var latitude: Double = 0.0

    private lateinit var geocodeAPI: GeocodeAPI
    private lateinit var weatherAPI: WeatherAPI

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var gestureDetector: GestureDetector

    private val permissionId = 39
    private var weatherData: WeatherData? = WeatherData()

    val weatherDataStore = WeatherDataStore(this)

    @SuppressLint("MissingPermission", "ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //LocationServices is the main entry point for location services integration
        //creates an instance of FusedLocationProviderClient
        fusedLocationClient  = LocationServices.getFusedLocationProviderClient(this)
        geocodeAPI = GeocodeAPI(this, this, this)
        weatherAPI = WeatherAPI(this, this, this)

        lifecycleScope.launch {
            WeatherRepository.weatherDataList = weatherDataStore.getWeatherDataOnce()
        }

        val leftIntent = Intent(this, MultiLocationActivity::class.java)
        gestureDetector = GestureDetector(this, SwipeGestureListener(this, null, leftIntent){
            weatherData?.copy()?.let { WeatherRepository.weatherDataList.add(it) }
            lifecycleScope.launch {
                weatherDataStore.saveWeatherData(WeatherRepository.weatherDataList)
            }
            Toast.makeText(this, "Added Location😊", Toast.LENGTH_SHORT).show()
        })

        val rootView = findViewById<View>(R.id.root)
        rootView.isClickable = true
        rootView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }

        getLastLocation()

        val autoCompleteTextView: AutoCompleteTextView = findViewById(R.id.locationName)

        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun afterTextChanged(s: Editable?) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if((s?.length ?: 0) > 0){
                    autoCompleteTextView.showDropDown()
                }
            }
        })

        autoCompleteTextView.setOnEditorActionListener{_, actionId, event ->
            if (actionId ==  EditorInfo.IME_ACTION_DONE ||
                (event?.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_UP)){
                val city: String = autoCompleteTextView.text.toString();

                if(city == ""){
                    getLastLocation()
                    Toast.makeText(this, "Getting Current Location", Toast.LENGTH_SHORT).show()
                } else{
                    geocodeAPI.getCoordinates(city) { lat, lon ->
                        weatherAPI.makeAPICall(lat, lon) {weatherData: WeatherData? ->
                            geocodeAPI.getCityName(lat, lon) { cityName: String? ->
                                weatherData?.locationName = cityName
                                this.weatherData = weatherData
                            }
                        }
                    }

                    Toast.makeText(this, "Searching: ${autoCompleteTextView.text}", Toast.LENGTH_SHORT).show()
                }
                true
            } else{
                false
            }
        }
    }

    private fun isSystemLocationEnabled(): Boolean {
        //Location Manager provides access to the system location services
        val locationManager = this.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    // shows location a request dialog
    private fun requestLocationPermissions() {
        //requestPermission() shows a request dialog and what Permission is being asked
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            ),
            permissionId
        )
    }

    //checks if the permissions has been granted on the app (coarse and fine location)
    private fun checkAppPermissions(): Boolean{
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @SuppressLint("MissingPermission")
    fun getLastLocation(){
        if(checkAppPermissions()){
            if (isSystemLocationEnabled()){
                //once its gotten the last location
                fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                    val location: Location? = task.result
                    if (location == null){
                        requestLocationData()
                    } else {
                        Log.d("USER_LOCATION", "${location.latitude}, ${location.longitude}")

                        latitude = location.latitude
                        longitude = location.longitude

                        weatherAPI.makeAPICall(latitude, longitude) {weatherData: WeatherData? ->
                            geocodeAPI.getCityName(latitude, longitude) { cityName: String? ->
                                weatherData?.locationName = cityName
                                this.weatherData = weatherData
                                Log.i("geocode", "${this.weatherData!!.locationName}")
                            }
                        }
                        geocodeAPI.getCityName(location.latitude, location.longitude)


                    }
                }
            } else {
                val intent = Intent(this, ErrorActivity::class.java)
                startActivity(intent)
            }
        } else {
            showPermissionExplanationDialog()
        }
    }

    private fun requestLocationData() {
        // requests location
        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY, 5000
        ).setMinUpdateIntervalMillis(0).setMaxUpdates(1).build()

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        } else {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.myLooper()
            )
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let {
                latitude = it.latitude
                longitude = it.longitude

                weatherAPI.makeAPICall(latitude, longitude) {weatherData: WeatherData? ->
                    geocodeAPI.getCityName(latitude, longitude) { cityName: String? ->
                        weatherData?.locationName = cityName
                        Log.i("geocode", "${weatherData!!.locationName}")
                    }
                }
            }
        }
    }

    //create a dialog to ask for permission
    private fun showPermissionExplanationDialog(){
        val builder = AlertDialog.Builder(this)
        val inflater = this.layoutInflater
        val customLayout: View = inflater.inflate(R.layout.custom_dialog_layout, null)
        builder.setView(customLayout)

        val dialog = builder.create().apply {
            show()
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            window?.setLayout(
                780,
                450
            )
        }

        val acceptButton: Button = customLayout.findViewById(R.id.acceptBtn)
        val denyButton: Button = customLayout.findViewById(R.id.denyBtn)

        acceptButton.setOnClickListener{
            requestLocationPermissions()
            dialog.dismiss()
        }

        denyButton.setOnClickListener{
            val intent = Intent(this, ErrorActivity::class.java)
            Log.i("activity check", "Created Intent")

            startActivity(intent)
            Log.i("activity check", "Going to Error_Page")
            dialog.dismiss()
        }
    }

    @Override
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == permissionId){
            if((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)){
                getLastLocation()
            }
            else{
                val intent = Intent(this, ErrorActivity::class.java)
                startActivity(intent)
            }
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event!!)
    }
}


