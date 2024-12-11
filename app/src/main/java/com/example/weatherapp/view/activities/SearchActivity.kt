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
import android.widget.Toast
import androidx.activity.viewModels
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.weatherapp.R
import com.example.weatherapp.repository.WeatherRepository
import com.example.weatherapp.utils.WeatherUtils
import com.example.weatherapp.view.adapters.ForecastAdapter
import com.example.weatherapp.viewmodel.WeatherViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.json.JSONObject
import kotlin.math.roundToInt

class SearchActivity : AppCompatActivity() {
    private val weatherViewModel: WeatherViewModel by viewModels()
    private lateinit var forecastAdapter: ForecastAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        val backButton: ImageView = findViewById(R.id.exit_search_result)
        backButton.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

        val detailAddress = intent.getStringExtra("detailAddress")
        val searchAddress = findViewById<TextView>(R.id.search_address)
        searchAddress.setText(detailAddress)

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
        val loadingPage: View = findViewById(R.id.loading_page)
        val contentPage: LinearLayout = findViewById(R.id.content_page)
        val currentWeatherCard: LinearLayout = findViewById(R.id.current_weather_card)
        val addFavButton: FloatingActionButton = findViewById(R.id.add_fav_button)
        val remFavButton: FloatingActionButton = findViewById(R.id.rem_fav_button)

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

        val tabLocs = ArrayList<String>()
        val recordIds = ArrayList<String>()
        weatherViewModel.favoriteLocations.observe(this, { favLocs ->
            for (i in 0 until favLocs.size) {
                tabLocs.add(favLocs[i].city + ", " + favLocs[i].state)
                recordIds.add(favLocs[i]._id)
            }
            weatherViewModel.loadGeocodingData(detailAddress ?: "")
        })

        var isInCollections = false
        var recordId = ""
        var isCurLoc = false
        weatherViewModel.dailyWeather.observe(this, Observer { dailyWeather ->
            forecastAdapter = ForecastAdapter(dailyWeather)
            forecastRecyclerView.adapter = forecastAdapter
            if (tabLocs[0].equals(detailAddress)) isCurLoc = true
            Log.d("MyInfo", tabLocs[0] + " vs " + detailAddress)
            for (i in 1 until tabLocs.size) {
                if (tabLocs[i].equals(detailAddress)) {
                    isInCollections = true
                    recordId = recordIds[i]
                    break
                }
            }
            if (isInCollections) {
                remFavButton.visibility = View.VISIBLE
                addFavButton.visibility = View.GONE
            } else if (!isInCollections && !isCurLoc) {
                remFavButton.visibility = View.GONE
                addFavButton.visibility = View.VISIBLE
            } else {
                remFavButton.visibility = View.GONE
                addFavButton.visibility = View.GONE
            }

            loadingPage.visibility = View.GONE
            contentPage.visibility = View.VISIBLE
        })

        weatherViewModel.loadFavLocations()


        currentWeatherCard.setOnClickListener {
            val intent = Intent(this, DetailActivity::class.java)
            val cityName = cityNameTextView.text.toString()
            val temperature = currentTemperatureTextView.text.toString().replace("°F", "")
            intent.putExtra("city_name", cityName)
            intent.putExtra("temperature", temperature)
            startActivity(intent)
        }

        addFavButton.setOnClickListener {
            val searchLocParts = (detailAddress?:"").split(", ")
            if (searchLocParts.size > 1) {
                weatherViewModel.addToFavorites(searchLocParts[0],
                    searchLocParts[1],
                    onSuccess = {
                        Toast.makeText(this,
                            searchLocParts[1] + "was added to favorites",
                            Toast.LENGTH_SHORT).show()
                        startActivity(getIntent());
                    })
            } else {
                Log.d("MyInfo", "detailAddress is inappropriate: " + detailAddress)
            }
        }

        remFavButton.setOnClickListener {
            val searchLocParts = (detailAddress?:"").split(", ")
            if (searchLocParts.size > 1) {

                weatherViewModel.remFromFavorites(recordId, onSuccess = {
                    Toast.makeText(this,
                        searchLocParts[1] + "was removed from favorites",
                        Toast.LENGTH_SHORT).show()
                    startActivity(getIntent());
                })
            } else {
                Log.d("MyInfo", "detailAddress is inappropriate: " + detailAddress)
            }
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