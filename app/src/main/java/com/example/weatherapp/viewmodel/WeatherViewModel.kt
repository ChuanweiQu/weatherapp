package com.example.weatherapp.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData


import org.json.JSONObject
import com.example.weatherapp.repository.WeatherRepository
import com.example.weatherapp.utils.WeatherUtils
import kotlin.math.log

data class FavRecord(
    val _id: String = "",
    val city: String  = "",
    val state: String  = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0
)

class WeatherViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = WeatherRepository(application)


    private val _dailyWeather = MutableLiveData<List<JSONObject>>()
    val dailyWeather: LiveData<List<JSONObject>> get() = _dailyWeather

    private val _hourlyWeather = MutableLiveData<List<JSONObject>>()
    val hourlyWeather: LiveData<List<JSONObject>> get() = _hourlyWeather

    private val _currentWeather = MutableLiveData<JSONObject>()
    val currentWeather: LiveData<JSONObject> get() = _currentWeather

    private val _latitude = MutableLiveData<Double>()
    val latitude: LiveData<Double> get() = _latitude

    private val _longitude = MutableLiveData<Double>()
    val longitude: LiveData<Double> get() = _longitude

    private val _formattedAddress = MutableLiveData<String>()
    val formattedAddress: LiveData<String> get() = _formattedAddress

    private val _weatherData = MutableLiveData<JSONObject>()
    val weatherData: LiveData<JSONObject> get() = _weatherData

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    private  val _locSuggestions = MutableLiveData<ArrayList<String>>()
    val locSuggestions: LiveData<ArrayList<String>> get() = _locSuggestions

    // location for tabs
    private  val _favoriteLocations = MutableLiveData<ArrayList<FavRecord>>()
    val favoriteLocations: LiveData<ArrayList<FavRecord>> get() = _favoriteLocations

    fun loadFavLocations() {
        repository.fetchFavorites(
            onSuccess = { arrayResp ->
                Log.d("MyInfo", "Fav success")
                val result = ArrayList<FavRecord>()
                for (i in 0 until arrayResp.length()) {
                    val respId = arrayResp.getJSONObject(i).getString("_id")
                    val respCity = arrayResp.getJSONObject(i).getString("city")
                    val respState = arrayResp.getJSONObject(i).getString("state")
                    result.add(FavRecord(_id = respId, city = respCity, state = respState))
                }

                repository.fetchIpInfo(
                    onSuccess = { lat, lon, city, state ->

                        val finalTabCities = arrayListOf(FavRecord(city = city, state = WeatherUtils.stateAbb[state]?:""))
                        finalTabCities.addAll(result)
                        _favoriteLocations.value = finalTabCities
                    },
                    onError = { errorMessage ->

                        _error.value = errorMessage
                    }
                )
            },
            onError = { errorMessage ->
                Log.d("MyInfo", "Fav on Error")
                _error.value = errorMessage
            }
        )
    }

    fun loadLocSuggestions(address: String) {
        if(address != "") {
            repository.fetchCitySuggestions(
                inputValue = address,
                onSuccess = { citySuggestions ->
                    _locSuggestions.value = citySuggestions
                },
                onError = { errorMessage ->
                    _error.value = errorMessage
                }
            )
        } else {
            _locSuggestions.value = ArrayList<String>()
        }
    }

    fun loadGeocodingData(address: String) {

        repository.fetchGeocodingData(
            address = address,
            onSuccess = { lat, lon, formattedAddress ->
                Log.d("MyInfo", "success to load geocoding")
                _latitude.value = lat
                _longitude.value = lon
                val locParts = formattedAddress.split(", ")
                _formattedAddress.value = locParts[0] + ", " + locParts[1]


                loadWeatherData(lat, lon)
            },
            onError = { errorMessage ->
                Log.d("MyInfo", "fail to load geocoding")
                _error.value = errorMessage
            }
        )
    }

    // Function to load weather data using latitude and longitude
    fun loadWeatherData(lat: Double, lon: Double) {
        repository.fetchWeatherData(
            lat = lat,
            lon = lon,
            onSuccess = { response ->
                _weatherData.value = response
                parseDailyWeather(response)
                parseHourlyWeather(response)
            },
            onError = { errorMessage ->
                Log.d("MyInfo", "fail to load weatherData")
                _error.value = errorMessage
            }
        )
    }

    fun loadIpInfo() {
        repository.fetchIpInfo(
            onSuccess = { lat, lon, city, state ->

                _latitude.value = lat.toDouble()
                _longitude.value = lon.toDouble()
                _formattedAddress.value = "$city, $state"

                loadWeatherData(lat.toDouble(), lon.toDouble())
            },
            onError = { errorMessage ->

                _error.value = errorMessage
            }
        )
    }

    fun addToFavorites(city: String,
                       state: String,
                       onSuccess: () -> Unit) {
        repository.postFavorites(city, state, onSuccess)
    }

    fun remFromFavorites(recId: String,
                         onSuccess: () -> Unit) {
        repository.deleteFavorite(recId, onSuccess)
    }

    private fun parseDailyWeather(response: JSONObject) {
        val dailyData = mutableListOf<JSONObject>()
        val timelines = response.getJSONObject("data").getJSONArray("timelines")

        for (i in 0 until timelines.length()) {
            val timeline = timelines.getJSONObject(i)
            if (timeline.getString("timestep") == "1d") {
                val intervals = timeline.getJSONArray("intervals")
                for (j in 0 until intervals.length()) {
                    dailyData.add(intervals.getJSONObject(j))
                }
            }
        }
        _dailyWeather.value = dailyData
    }
    private fun parseHourlyWeather(response: JSONObject) {
        val hourlyData = mutableListOf<JSONObject>()
        val timelines = response.getJSONObject("data").getJSONArray("timelines")

        for (i in 0 until timelines.length()) {
            val timeline = timelines.getJSONObject(i)
            if (timeline.getString("timestep") == "1h") {
                val intervals = timeline.getJSONArray("intervals")
                for (j in 0 until intervals.length()) {
                    hourlyData.add(intervals.getJSONObject(j))
                }
            }
        }
        _hourlyWeather.value = hourlyData
        if (hourlyData.isNotEmpty()) {
            _currentWeather.value = hourlyData[0]
        }
    }
}
