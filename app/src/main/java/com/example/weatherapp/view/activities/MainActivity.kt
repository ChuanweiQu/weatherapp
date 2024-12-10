// MainActivity.kt
package com.example.weatherapp.view.activities

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet.Layout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.utils.WeatherUtils
import com.example.weatherapp.view.adapters.ForecastAdapter
import com.example.weatherapp.viewmodel.WeatherViewModel
import org.json.JSONObject
import kotlin.math.log
import kotlin.math.roundToInt


class MainActivity : AppCompatActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var forecastAdapter: ForecastAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val currentTemperatureTextView: TextView = findViewById(R.id.current_temperature)
        val cityNameTextView: TextView = findViewById(R.id.city_name)
        val weatherIconImageView: ImageView = findViewById(R.id.weather_icon)
        val humidityTextView: TextView = findViewById(R.id.humidity)
        val windSpeedTextView: TextView = findViewById(R.id.wind_speed)
        val visibilityTextView: TextView = findViewById(R.id.visibility)
        val pressureTextView: TextView = findViewById(R.id.pressure)
        val weatherSummaryTextView: TextView = findViewById(R.id.weather_summary)
        val forecastRecyclerView: RecyclerView = findViewById(R.id.forecast_recyclerview)
        val currentWeatherCard: LinearLayout = findViewById(R.id.current_weather_card)
        val mapSearchButton: ImageButton = findViewById(R.id.map_search)
        val clearSearchButton: ImageButton = findViewById(R.id.clear_search)
        val exitSearchButton: ImageButton = findViewById(R.id.exit_search)
        val citySearchInput: AutoCompleteTextView = findViewById(R.id.city_search_input)
        val loadingPage: View = findViewById(R.id.loading_page)
        val contentPage: LinearLayout = findViewById(R.id.content_page)

        forecastRecyclerView.layoutManager = LinearLayoutManager(this)
        forecastAdapter = ForecastAdapter(emptyList())
        forecastRecyclerView.adapter = forecastAdapter

        weatherViewModel.formattedAddress.observe(this, Observer { address ->
            cityNameTextView.text = address
        })

        weatherViewModel.currentWeather.observe(this, Observer { currentWeather ->
            updateWeatherAttributes(currentWeather, currentTemperatureTextView, weatherIconImageView, humidityTextView,
                windSpeedTextView, visibilityTextView, pressureTextView, weatherSummaryTextView)
        })

        weatherViewModel.dailyWeather.observe(this, Observer { dailyWeather ->
            forecastAdapter = ForecastAdapter(dailyWeather)
            forecastRecyclerView.adapter = forecastAdapter
            loadingPage.visibility = View.GONE
            contentPage.visibility = View.VISIBLE
        })

        weatherViewModel.loadIpInfo()

        // Set click listener for current_weather_card
        currentWeatherCard.setOnClickListener {
            val intent = Intent(this, DetailActivity::class.java)
            val cityName = cityNameTextView.text.toString()
            val temperature = currentTemperatureTextView.text.toString().replace("°F", "")
            intent.putExtra("city_name", cityName)
            intent.putExtra("temperature", temperature)
            startActivity(intent)
        }

        val searchToggler: (Boolean) -> Unit = {openSearch ->
            run {
                val mapSearchBar = findViewById<ConstraintLayout>(R.id.map_search_bar)
                val mapSearchInput = findViewById<ConstraintLayout>(R.id.map_search_input)
                if (openSearch) {
                    mapSearchInput.visibility = View.VISIBLE
                    mapSearchBar.visibility = View.GONE
                } else {
                    mapSearchInput.visibility = View.GONE
                    mapSearchBar.visibility = View.VISIBLE
                    citySearchInput.setText("")

                    val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    imm.hideSoftInputFromWindow(citySearchInput.windowToken, 0)
                    mapSearchInput.clearFocus()
                }
            }
        }

        mapSearchButton.setOnClickListener { searchToggler(true) }
        exitSearchButton.setOnClickListener { searchToggler(false) }

        clearSearchButton.setOnClickListener { citySearchInput.setText("") }

        var beforeSearchText = ""
        citySearchInput.addTextChangedListener {
            if (beforeSearchText != citySearchInput.text.toString()) {
                beforeSearchText = citySearchInput.text.toString()
                weatherViewModel.loadLocSuggestions(citySearchInput.text.toString())
            }
        }
        val emptyList: List<String> = listOf()
        val citySugadapter = ArrayAdapter(this, R.layout.simple_dropdown, emptyList)
        citySearchInput.threshold = 0
        citySearchInput.setAdapter(citySugadapter)

        weatherViewModel.locSuggestions.observe(this) { locSuggestions ->
            citySugadapter.clear()
            citySugadapter.addAll(locSuggestions)
            citySearchInput.setText(citySearchInput.text)
            citySearchInput.setSelection(citySearchInput.text.length)
        }

        citySearchInput.setOnItemClickListener { adapterView, view, i, l ->
            val selectedCity = adapterView.getItemAtPosition(i).toString()
            val intent = Intent(this, SearchActivity::class.java)
            intent.putExtra("detailAddress", selectedCity)
            startActivity(intent)
        }
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

