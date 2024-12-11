package com.example.weatherapp.view.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.utils.WeatherUtils
import com.example.weatherapp.view.activities.DetailActivity
import com.example.weatherapp.view.activities.MainActivity
import com.example.weatherapp.view.adapters.ForecastAdapter
import com.example.weatherapp.viewmodel.WeatherViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject
import kotlin.math.roundToInt

class HomeScreen : Fragment() {
    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var forecastAdapter: ForecastAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View?{
        val view = inflater.inflate(R.layout.fragment_homescreen, container, false)
        val locationText = arguments?.getString("locationText")

        val currentTemperatureTextView: TextView = view.findViewById(R.id.current_temperature)
        val cityNameTextView: TextView = view.findViewById(R.id.city_name)
        val weatherIconImageView: ImageView = view.findViewById(R.id.weather_icon)
        val humidityTextView: TextView = view.findViewById(R.id.humidity)
        val windSpeedTextView: TextView = view.findViewById(R.id.wind_speed)
        val visibilityTextView: TextView = view.findViewById(R.id.visibility)
        val pressureTextView: TextView = view.findViewById(R.id.pressure)
        val weatherSummaryTextView: TextView = view.findViewById(R.id.weather_summary)
        val forecastRecyclerView: RecyclerView = view.findViewById(R.id.forecast_recyclerview)
        val currentWeatherCard: LinearLayout = view.findViewById(R.id.current_weather_card)
        val loadingPage: View = view.findViewById(R.id.loading_page)
        val contentPage: LinearLayout = view.findViewById(R.id.content_page)
        val remFavButton: FloatingActionButton = view.findViewById(R.id.rem_fav_button)
        var temperatureChartOptions = mutableListOf<Triple<Long, Int, Int>>()

        forecastRecyclerView.layoutManager = LinearLayoutManager(view.context)
        forecastAdapter = ForecastAdapter(emptyList())
        forecastRecyclerView.adapter = forecastAdapter

        weatherViewModel.formattedAddress.observe(viewLifecycleOwner, Observer { address ->
            cityNameTextView.text = address
        })

        weatherViewModel.currentWeather.observe(viewLifecycleOwner, Observer { currentWeather ->
            updateWeatherAttributes(currentWeather, currentTemperatureTextView, weatherIconImageView, humidityTextView,
                windSpeedTextView, visibilityTextView, pressureTextView, weatherSummaryTextView)
        })

        val tabLocs = ArrayList<String>()
        val recordIds = ArrayList<String>()
        weatherViewModel.favoriteLocations.observe(viewLifecycleOwner, { favLocs ->
            for (i in 0 until favLocs.size) {
                tabLocs.add(favLocs[i].city + ", " + favLocs[i].state)
                recordIds.add(favLocs[i]._id)
            }
            Log.d("MyInfo", "Got data favLocs")
            weatherViewModel.loadGeocodingData(locationText ?: "")
        })

        var recordId = ""
        var isCurLoc = false
        weatherViewModel.dailyWeather.observe(viewLifecycleOwner, Observer { dailyWeather ->
            Log.d("MyInfo", "Got data daily weather")
            forecastAdapter = ForecastAdapter(dailyWeather)
            forecastRecyclerView.adapter = forecastAdapter
            temperatureChartOptions = forecastAdapter.getTemperatureChartOptions().toMutableList()
            Log.d("MyInfo", tabLocs[0] + "vs" + locationText)
            if (tabLocs[0].equals(locationText)) isCurLoc = true

            for (i in 1 until tabLocs.size) {
                if (tabLocs[i].equals(locationText)) {
                    recordId = recordIds[i]
                    break
                }
            }

            if (!isCurLoc) {
                remFavButton.visibility = View.VISIBLE
            } else {
                remFavButton.visibility = View.GONE
            }

            loadingPage.visibility = View.GONE
            contentPage.visibility = View.VISIBLE
        })

        weatherViewModel.loadFavLocations()

        // Set click listener for current_weather_card
        currentWeatherCard.setOnClickListener {
            val intent = Intent(view.context, DetailActivity::class.java)
            val cityName = cityNameTextView.text.toString()
            val temperature = currentTemperatureTextView.text.toString()
            val weatherDesc = weatherSummaryTextView.text.toString()
            val currentWeather = weatherViewModel.currentWeather.value
            val values = currentWeather?.getJSONObject("values")
            intent.putExtra("city_name", cityName)
            intent.putExtra("temperature", temperature)
            intent.putExtra("humidity", values?.getInt("humidity"))
            intent.putExtra("wind_speed", values?.getDouble("windSpeed").toString())
            intent.putExtra("visibility", values?.getDouble("visibility").toString())
            intent.putExtra("pressure", values?.getDouble("pressureSeaLevel").toString())
            intent.putExtra("precipitation", values?.getInt("precipitationProbability"))
            intent.putExtra("cloud_cover", values?.getInt("cloudCover"))
            intent.putExtra("ozone", values?.getInt("uvIndex").toString())
            intent.putExtra("weather_desc", weatherDesc)
            intent.putExtra("weather_icon", WeatherUtils.getWeatherIcon(values?.getInt("weatherCode") ?: 0))
            intent.putExtra("temperature_chart_options", ArrayList(temperatureChartOptions))
            startActivity(intent)
        }

        remFavButton.setOnClickListener {
            val searchLocParts = (locationText?:"").split(", ")
            if (searchLocParts.size > 1) {

                weatherViewModel.remFromFavorites(recordId, onSuccess = {
                    Toast.makeText(context,
                        searchLocParts[1] + "was removed from favorites",
                        Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, MainActivity::class.java)
                    startActivity(intent)
                })
            } else {
                Log.d("MyInfo", "detailAddress is inappropriate: " + locationText)
            }
        }

        return view
    }

    private fun updateWeatherAttributes(
        currentWeather: JSONObject,
        currentTemperatureTextView: TextView,
        weatherIconImageView: ImageView,
        humidityTextView: TextView,
        windSpeedTextView: TextView,
        visibilityTextView: TextView,
        pressureTextView: TextView,
        weatherSummaryTextView: TextView
    ) {
        val values = currentWeather.getJSONObject("values")
        val temperature = values.getDouble("temperature").roundToInt()
        val humidity = values.getInt("humidity")
        val windSpeed = values.getDouble("windSpeed")
        val visibility = values.getDouble("visibility")
        val pressure = values.getDouble("pressureSeaLevel")
        val weatherCode = values.getInt("weatherCode")

        // Update the views with the current weather data
        currentTemperatureTextView.text = "${temperature}°F"
        humidityTextView.text = "$humidity%"
        windSpeedTextView.text = "${windSpeed} mph"
        visibilityTextView.text = "${visibility} mi"
        pressureTextView.text = "${pressure} inHg"
        weatherSummaryTextView.text = WeatherUtils.getWeatherDescription(weatherCode)

        val weatherIconResId = WeatherUtils.getWeatherIcon(weatherCode)
        weatherIconImageView.setImageResource(weatherIconResId)
    }
}