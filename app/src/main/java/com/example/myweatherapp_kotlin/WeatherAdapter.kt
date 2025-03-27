package com.example.myweatherapp_kotlin

import android.content.Context
import android.util.Log
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class WeatherAdapter(
    private var weatherList: List<WeatherData>,
    private val context: Context,
    private val onDoubleTap: (Int) -> Unit
): RecyclerView.Adapter<WeatherAdapter.WeatherViewHolder>() {

    class WeatherViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val bgImage: ImageView = itemView.findViewById(R.id.locationBackground)
        val locationText: TextView = itemView.findViewById(R.id.locationText)
        val locationCurrTemp: TextView = itemView.findViewById(R.id.locationCurrTemp)
    }

    @Override
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WeatherViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.location_weather, parent, false)
        return WeatherViewHolder(view)
    }

    @Override
    override fun onBindViewHolder(holder: WeatherViewHolder, position: Int) {
        val weather = weatherList[position]
        holder.locationText.text = weather.locationName ?: "----"
        holder.locationCurrTemp.text = "${weather.currTemp}°"

        val bg = when (weather.condition?.lowercase()){
            "clear" -> R.drawable.forest_sunny
            "clouds", "mist", "haze", "partlysunny" -> R.drawable.forest_cloudy
            "drizzle", "thunderstorm", "rain" -> R.drawable.forest_rainy
            else -> R.drawable.forest_sunny
        }
        holder.bgImage.setImageResource(bg)

        val gestureDetector = GestureDetector(context, SwipeGestureListener(
            context,
            sRightIntent = null,
            sLeftIntent = null,
            onDoubleTapCallback = {
                onDoubleTap(holder.adapterPosition)
                Log.i("geocode", "${holder.adapterPosition}")
            }
        ))

        holder.itemView.setOnTouchListener{ _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }
    }

    override fun getItemCount(): Int = weatherList.size

    fun updateList(newList: List<WeatherData>){
        weatherList = newList.toMutableList()
        notifyDataSetChanged()
    }
}