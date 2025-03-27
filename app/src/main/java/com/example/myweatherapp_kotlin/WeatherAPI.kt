package com.example.weatherapp

import android.app.Activity
import android.content.Context
import android.util.Log
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.myweatherapp_kotlin.WeatherData
import com.example.myweatherapp_kotlin.R
import org.json.JSONException
import org.json.JSONObject
import java.util.*

class WeatherAPI(private val activity: Activity?, private val context: Context?, private var appCompatActivity: AppCompatActivity? = null) {

    private var weatherResponse: JSONObject? = null
    private var currentResponse: JSONObject? = null

    private var isForecastReady = false
    private var isCurrentReady = false

    var weatherForecast: Array<WeatherData?> = Array(6) { WeatherData("", 0, 0, 0) }

    fun makeAPICall(lat: Double, lon: Double, callback: (WeatherData?) -> Unit) {
        val urlForecast = "https://api.openweathermap.org/data/2.5/forecast?lat=${lat}&lon=${lon}&appid=b5931244e08cf12bc6a3a80b9f24ffc5&units=metric"
        val urlCurrent = "https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&appid=b5931244e08cf12bc6a3a80b9f24ffc5&units=metric"

        val requestForecast = JsonObjectRequest(
            Request.Method.GET, urlForecast, null,
            { response ->
                try {
                    weatherResponse = response
                    getDataForecast()
                    Log.i("CHECK_TEMP", "FORECAST RECEIVED: $weatherForecast")
                } catch (e: Exception) {
                    Log.e("CHECK_TEMP", "FAILED TO RESPOND: $e")
                }
            },
            { error ->
                Log.e("CHECK_TEMP", "ERROR: $error")
                Log.e("CHECK_TEMP", "LAT: $lat")
                Log.e("CHECK_TEMP", "LON: $lon")
            })

        val requestCurrent = JsonObjectRequest(
            Request.Method.GET, urlCurrent, null,
            { response ->
                try {
                    currentResponse = response
                    Log.i("CHECK_TEMP", "CURRENT WEATHER RECEIVED: $currentResponse")
                    callback(getDataCurrent())
                } catch (e: Exception) {
                    Log.e("CHECK_TEMP", "FAILED TO RESPOND: $e")
                    callback(null)
                }
            },
            { error ->
                Log.e("CHECK_TEMP", "ERROR: $error")
                Log.e("CHECK_TEMP", "LAT: $lat")
                Log.e("CHECK_TEMP", "LON: $lon")
                callback(null)
            })

        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
        requestQueue.add(requestForecast)
        requestQueue.add(requestCurrent)
    }

    fun getLocationWeather(lat: Double, lon: Double, callback: (WeatherData?) -> Unit){
        val url = "https://api.openweathermap.org/data/2.5/weather?lat=${lat}&lon=${lon}&appid=b5931244e08cf12bc6a3a80b9f24ffc5&units=metric"
        val requestCurrent = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val weatherData: WeatherData = extractDataLocationWeather(response)
                    weatherData.locationLat = lat
                    weatherData.locationLon = lon
                    Log.i("CHECK_TEMP", "LOCATION WEATHER RECEIVED: $currentResponse")
                    callback(weatherData)
                } catch (e: Exception) {
                    Log.e("CHECK_TEMP", "FAILED TO RESPOND: $e")
                    callback(null)
                }
            },
            { error ->
                Log.e("CHECK_TEMP", "ERROR: $error")
                Log.e("CHECK_TEMP", "LAT: $lat")
                Log.e("CHECK_TEMP", "LON: $lon")
                callback(null)
            })

        val requestQueue: RequestQueue = Volley.newRequestQueue(context)
        requestQueue.add(requestCurrent)
    }

    private fun getDataForecast() {
        try {
            val list = weatherResponse?.getJSONArray("list") ?: return

            for (i in 1..5) {
                val weatherData = list.getJSONObject(i)
                val main = weatherData.getJSONObject("main")

                val currTemp = main.getDouble("temp").toInt()
                val minTemp = main.getDouble("temp_min").toInt()
                val maxTemp = main.getDouble("temp_max").toInt()

                val weather = weatherData.getJSONArray("weather").getJSONObject(0)
                val condition = weather.getString("main")

                weatherForecast[i]?.condition = condition;
                weatherForecast[i]?.currTemp = currTemp;
                weatherForecast[i]?.min = minTemp;
                weatherForecast[i]?.max = maxTemp;
            }

            isForecastReady = true
            checkAndPopulateUI()
        } catch (e: JSONException) {
            Log.e("CHECK_TEMP_ERROR", e.toString())
        }
    }

    private fun getDataCurrent(): WeatherData? {
        var weatherData: WeatherData = WeatherData()
        try {
            val main = currentResponse?.getJSONObject("main") ?: return null
            val weather = currentResponse?.getJSONArray("weather")?.getJSONObject(0) ?: return null

            val currTemp = main.getDouble("temp").toInt()
            val minTemp = main.getDouble("temp_min").toInt()
            val maxTemp = main.getDouble("temp_max").toInt()
            val condition = weather.getString("main")

            weatherForecast[0]?.condition = condition
            weatherForecast[0]?.currTemp = currTemp
            weatherForecast[0]?.min = minTemp
            weatherForecast[0]?.max = maxTemp

            weatherData = weatherForecast[0]!!;

            isCurrentReady = true
            checkAndPopulateUI()
        } catch (e: JSONException) {
            Log.e("CHECK_TEMP_ERROR", e.toString())
        }

        return weatherData
    }

    private fun extractDataLocationWeather(data: JSONObject): WeatherData {
        val weatherData: WeatherData = WeatherData()

        try {
            val main = data.getJSONObject("main")
            val weather = data.getJSONArray("weather").getJSONObject(0)

            val currTemp = main.getDouble("temp").toInt()
            val condition = weather.getString("main")

            weatherData.condition = condition
            weatherData.currTemp = currTemp

        } catch (e: JSONException) {
            Log.e("CHECK_TEMP_ERROR", e.toString())
        }

        return weatherData
    }

    private fun populateUI() {
        Log.i("CHECK_TEMP", "Populating UI...")
        appCompatActivity?.let { activity ->
            val tempText = activity.findViewById<TextView>(R.id.currTemp)
            val weatherDescText = activity.findViewById<TextView>(R.id.weatherDesc)
            val minTempText = activity.findViewById<TextView>(R.id.minTempText)
            val currTempText = activity.findViewById<TextView>(R.id.currTempText)
            val maxTempText = activity.findViewById<TextView>(R.id.maxTempText)
            val backgroundView = activity.findViewById<ImageView>(R.id.imageView)
            val root = activity.findViewById<LinearLayout>(R.id.root)

            Log.i("CHECK_TEMP", "Populated ")
            weatherForecast[0]?.let { data ->
                tempText.text = "${data.currTemp}°"
                weatherDescText.text = data.condition?.uppercase()
                minTempText.text = "${data.min}°"
                currTempText.text = "${data.currTemp}°"
                maxTempText.text = "${data.max}°"

                Log.i("CHECK_TEMP", "WEATHER condition ${data.condition}")
                Log.i("CHECK_TEMP", "WEATHER currTemp ${data.currTemp}")
                Log.i("CHECK_TEMP", "WEATHER min ${data.min}")
                Log.i("CHECK_TEMP", "WEATHER max ${data.max}")
            }

            val condition = weatherForecast[0]?.condition?.lowercase() ?: "clear"
            val (backgroundRes, colorRes) = when (condition) {
                "clear" -> R.drawable.forest_sunny to R.color.sunny_bg
                "clouds", "mist", "haze", "partlysunny" -> R.drawable.forest_cloudy to R.color.cloudy_bg
                "drizzle", "thunderstorm", "rain" -> R.drawable.forest_rainy to R.color.rainy_bg
                else -> R.drawable.forest_sunny to R.color.sunny_bg
            }

            backgroundView.setImageResource(backgroundRes)
            root.setBackgroundColor(ContextCompat.getColor(activity, colorRes))

            val days = nextFiveDays()

            for (i in 1..5) {
                weatherForecast[i]?.let { dayData ->
                    val tempID = activity.resources.getIdentifier("temp${i}", "id", activity.packageName)
                    val iconID = activity.resources.getIdentifier("icon$i", "id", activity.packageName)
                    val dayID = activity.resources.getIdentifier("day${i}", "id", activity.packageName)

                    val dayText = activity.findViewById<TextView>(dayID)
                    val dayTempText = activity.findViewById<TextView>(tempID)
                    val icon = activity.findViewById<ImageView>(iconID)

                    dayText.text = days[i - 1]
                    dayTempText.text = "${dayData.currTemp}°"

                    val iconResID = when (dayData.condition?.lowercase()) {
                        "clear" -> R.drawable.clear3x
                        "clouds", "mist", "haze", "partlysunny" -> R.drawable.partlysunny3x
                        "drizzle", "thunderstorm", "rain" -> R.drawable.rain3x
                        else -> R.drawable.clear3x
                    }
                    icon.setImageResource(iconResID)
                }
            }
        }

        Log.i("CHECK_TEMP", "UI POPULATED")
    }

    private fun nextFiveDays(): Array<String?> {
        val locale = Locale.getDefault()
        return Array(5) { i ->
            val calendar = Calendar.getInstance()
            calendar.add(Calendar.DAY_OF_YEAR, i + 1)
            calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, locale)
        }
    }

    private fun checkAndPopulateUI() {
        if (isForecastReady && isCurrentReady) {
            activity?.runOnUiThread { populateUI() }
        }
    }
}