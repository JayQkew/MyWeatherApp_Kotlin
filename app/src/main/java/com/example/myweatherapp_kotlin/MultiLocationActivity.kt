package com.example.myweatherapp_kotlin

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.coroutines.launch

class MultiLocationActivity : AppCompatActivity(), View.OnTouchListener {

    private lateinit var recyclerView: RecyclerView
    private lateinit var weatherAdapter: WeatherAdapter
    private lateinit var gestureDetector: GestureDetector

    val weatherDataStore = WeatherDataStore(this)

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_multi_location)

        recyclerView = findViewById(R.id.weatherRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)


        lifecycleScope.launch {
            val weatherList = weatherDataStore.getWeatherDataOnce() // Load from DataStore
            weatherAdapter = WeatherAdapter(weatherList, this@MultiLocationActivity){ weatherToRemove ->
                removeWeatherData(weatherToRemove)
            }
            recyclerView.adapter = weatherAdapter
        }

        var rightIntent = Intent(this, MainActivity::class.java)
        gestureDetector = GestureDetector(this,SwipeGestureListener(this, rightIntent, null){
            Log.i("geocode", "${WeatherRepository.weatherDataList}")
        })
        val rootView = findViewById<View>(R.id.weatherRecyclerView)
        rootView.isClickable = true
        rootView.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
        }
    }

    override fun onTouch(v: View?, event: MotionEvent?): Boolean {
        return gestureDetector.onTouchEvent(event!!)
    }

    private fun removeWeatherData(index: Int) {
        val removedItem = WeatherRepository.weatherDataList.removeAt(index)
        weatherAdapter.updateList(WeatherRepository.weatherDataList)
        Log.i("geocode", "${WeatherRepository.weatherDataList}")

        lifecycleScope.launch {
            weatherDataStore.saveWeatherData(WeatherRepository.weatherDataList)
            Toast.makeText(this@MultiLocationActivity, "Removed ${removedItem.locationName}", Toast.LENGTH_SHORT).show()
        }
    }
}