package com.example.myweatherapp_kotlin

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.AutoCompleteTextView
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONArray
import org.json.JSONObject

class GeocodeAPI(private val activity: Activity?, private val context: Context?, private val appCompatActivity: AppCompatActivity?) {

    var latitude: Double = 0.0
        private set

    var longitude: Double = 0.0
        private set

    var city: String = ""
        private set

    private val APIKey: String = "AIzaSyCFErLOhCBb0721e6RbQDOVkRBF2Vu5kdM"

    fun getCoordinates(city: String){
        this.city = city
        val url = "https://maps.googleapis.com/maps/api/geocode/json?address=${city.replace(" ", "+")}&key=$APIKey"

        val requestLocation = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try{
                    val results = response?.getJSONArray("results")?.getJSONObject(0) ?: return@JsonObjectRequest
                    val location = results.getJSONObject("geometry").getJSONObject("location")

                    latitude = location.getDouble("lat")
                    longitude = location.getDouble("lng")
                    populateUI()

                    Log.i("geocode", "latitude: $latitude")
                    Log.i("geocode", "longitude: $longitude")
                } catch (e: Exception){
                    Log.e("geocode", "Failed to get co-ordinates: $e")
                }
            },
            { error ->
                Log.e("geocode", "ERROR: $error")
            }
        )

        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
        requestQueue.add(requestLocation)
    }

    fun getCoordinates(city: String, callback: (latitude: Double, longitude: Double) -> Unit){
        this.city = city
        val url = "https://maps.googleapis.com/maps/api/geocode/json?address=${city.replace(" ", "+")}&key=$APIKey"

        val requestLocation = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try{
                    val results = response?.getJSONArray("results")?.getJSONObject(0) ?: return@JsonObjectRequest
                    val location = results.getJSONObject("geometry").getJSONObject("location")

                    latitude = location.getDouble("lat")
                    longitude = location.getDouble("lng")
                    populateUI()

                    callback(latitude, longitude)
                    Log.i("geocode", "latitude: $latitude")
                    Log.i("geocode", "longitude: $longitude")
                } catch (e: Exception){
                    Log.e("geocode", "Failed to get co-ordinates: $e")
                }
            },
            { error ->
                Log.e("geocode", "ERROR: $error")
            }
        )

        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
        requestQueue.add(requestLocation)
    }

    fun getCityName(lat: Double, lon: Double) {
        latitude = lat
        longitude = lon
        val url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=$lat,$lon&key=AIzaSyCFErLOhCBb0721e6RbQDOVkRBF2Vu5kdM"

        val requestCity = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try{
                    val results = response?.getJSONArray("results")?.getJSONObject(0) ?: return@JsonObjectRequest
                    val addressComponents: JSONArray = results.getJSONArray("address_components")
                    for (i in 0 until addressComponents.length()) {
                        val component = addressComponents.getJSONObject(i)
                        val types = component.getJSONArray("types")

                        // Checking if the type contains "locality" (which represents a city)
                        if (types.toString().contains("locality")) {
                            city = component.getString("long_name")
                            break
                        }
                    }

                    populateUI()

                    Log.i("geocode", "city: $city")
                } catch (e: Exception){
                    Log.e("geocode", "Failed to get co-ordinates: $e")
                }
            },
            { error ->
                Log.e("geocode", "ERROR: $error")
            }
        )

        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
        requestQueue.add(requestCity)
    }

    fun getCityName(lat: Double, lon: Double, callback: (name: String?) -> Unit) {
        latitude = lat
        longitude = lon
        val url = "https://maps.googleapis.com/maps/api/geocode/json?latlng=$lat,$lon&key=AIzaSyCFErLOhCBb0721e6RbQDOVkRBF2Vu5kdM"

        val requestCity = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try{
                    val results = response?.getJSONArray("results")?.getJSONObject(0) ?: return@JsonObjectRequest
                    val addressComponents: JSONArray = results.getJSONArray("address_components")
                    for (i in 0 until addressComponents.length()) {
                        val component = addressComponents.getJSONObject(i)
                        val types = component.getJSONArray("types")

                        // Checking if the type contains "locality" (which represents a city)
                        if (types.toString().contains("locality")) {
                            city = component.getString("long_name")
                            break
                        }
                    }

                    Log.i("geocode", "city: $city")
                    callback(city)
                } catch (e: Exception){
                    Log.e("geocode", "Failed to get co-ordinates: $e")
                    callback(null)
                }
            },
            { error ->
                Log.e("geocode", "ERROR: $error")
                callback(null)
            }
        )

        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
        requestQueue.add(requestCity)
    }


    fun populateUI(){
        appCompatActivity?.let{ activity ->
            val locationText = activity.findViewById<AutoCompleteTextView>(R.id.locationName)

            locationText.setText(city)
        }
    }
}