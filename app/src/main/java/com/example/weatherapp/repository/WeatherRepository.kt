package com.example.weatherapp.repository
import android.content.Context
import android.util.Log
import com.example.weatherapp.utils.ApiConstants
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.example.weatherapp.viewmodel.FavRecord
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.log


class WeatherRepository(context: Context) {
    private val requestQueue: RequestQueue = Volley.newRequestQueue(context)

    fun fetchIpInfo(
        onSuccess: (latitude: String, longitude: String, city: String, state: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "${ApiConstants.BASE_URL}/get_ipToken"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val ipToken = response.getString("token")
                    val ipInfoUrl = "https://ipinfo.io/?token=$ipToken"

                    val ipInfoRequest = JsonObjectRequest(
                        Request.Method.GET, ipInfoUrl, null,
                        { ipInfoResponse ->
                            val loc = ipInfoResponse.getString("loc").split(",")
                            val latitude = loc[0]
                            val longitude = loc[1]
                            val city = ipInfoResponse.getString("city")
                            val state = ipInfoResponse.getString("region")
                            onSuccess(latitude, longitude, city, state)
                        },
                        { error ->
                            onError(error.message ?: "Error fetching IP Info data")
                        }
                    )
                    requestQueue.add(ipInfoRequest)

                } catch (e: Exception) {
                    onError("Error parsing IP token: ${e.message}")
                }
            },
            { error ->
                onError(error.message ?: "Error fetching IP token")
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    fun fetchGeocodingData(
        address: String,
        onSuccess: (latitude: Double, longitude: Double, formattedAddress: String) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "${ApiConstants.BASE_URL}/get_geocoding?address=$address"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val status = response.getString("status")
                    if (status == "OK") {
                        val results = response.getJSONArray("results")
                        val location = results.getJSONObject(0).getJSONObject("geometry").getJSONObject("location")
                        val latitude = location.getDouble("lat")
                        val longitude = location.getDouble("lng")
                        val formattedAddress = results.getJSONObject(0).getString("formatted_address")
                        onSuccess(latitude, longitude, formattedAddress)
                    } else {
                        onError("Geocoding API error: $status")
                    }
                } catch (e: Exception) {
                    onError("Error parsing Geocoding data: ${e.message}")
                }
            },
             { error ->
                onError(error.message ?: "Error fetching Geocoding data")
            }
        )

        requestQueue.add(jsonObjectRequest)
    }


    fun fetchWeatherData(
        lat: Double,
        lon: Double,
        onSuccess: (JSONObject) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "${ApiConstants.BASE_URL}/get_weather?latitude=$lat&longitude=$lon"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    if (response.has("data")) {
                        onSuccess(response)
                    } else {
                        onError("Weather API error: ${response.optString("error")}")
                    }
                } catch (e: Exception) {
                    onError("Error parsing Weather data: ${e.message}")
                }
            },
            { error ->
                onError(error.message ?: "Error fetching Weather data")
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    fun fetchCitySuggestions(
        inputValue: String,
        onSuccess: (ArrayList<String>) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "https://cs571assignment3-441221.uc.r.appspot.com/autocomplete?input=$inputValue"

        val jsonObjectRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                try {

                    val locs = ArrayList<String>()
                    for (i in 0 until response.length()) {
                        val jsonObject = response.getJSONObject(i)
                        val loc = jsonObject.getString("city") + ", " + jsonObject.getString("state")
                        locs.add(loc)  // Add the extracted data to the list
                    }
                    onSuccess(locs)
                } catch (e: Exception) {
                    onError("Error fetching suggestions: ${e.message}")
                }
            },
            { error ->
                onError(error.message ?: "Error fetching suggestions")
            }
        )

        requestQueue.add(jsonObjectRequest)
    }

    fun fetchFavorites(
        onSuccess: (JSONArray) -> Unit,
        onError: (String) -> Unit
    ) {
        val url = "https://cs571assignment3-441221.uc.r.appspot.com/get_all_favs"
        Log.d("MyInfo", "after url")

        val jsonObjectRequest = JsonArrayRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    onSuccess(response)
                } catch (e: Exception) {
                    onError("Error fetching favorites: ${e.message}")
                }
            },
            { error ->
                Log.d("MyInfo", "Fav Error")
                onError(error.message ?: "Error fetching favorites")
            }
        )

        requestQueue.add(jsonObjectRequest)

    }

    fun postFavorites(city: String,
                      state: String,
                      onSuccess: () -> Unit) {

        fetchFavorites(
            onSuccess = {arrayResp ->
                var hasCollected = false
                for (i in 0 until arrayResp.length()) {
                    val respCity = arrayResp.getJSONObject(i).getString("city")
                    val respState = arrayResp.getJSONObject(i).getString("state")
                    if (respCity.equals(city) && respState.equals(state)) {
                        hasCollected = true
                        break
                    }
                }

                if (!hasCollected) {
                    val url = "https://cs571assignment3-441221.uc.r.appspot.com/add_fav"
                    val jsonBody = JSONObject()
                    jsonBody.put("city", city)
                    jsonBody.put("state", state)
                    jsonBody.put("lat", 0)
                    jsonBody.put("lng", 0)

                    val postRequest = JsonObjectRequest(Request.Method.POST, url, jsonBody,
                        { response ->
                            // Handle the response
                            Log.d("MyInfo", "Post Fav Response: $response")
                            onSuccess()
                        },
                        { error ->
                            // Handle error
                            Log.e("MyInfo", "Post Fav Error: ${error.message}")
                        }
                    )
                    requestQueue.add(postRequest)
                }
            },
            onError = {}
        )

    }

    fun deleteFavorite (recId: String,
                        onSuccess: () -> Unit) {
        val url = "https://cs571assignment3-441221.uc.r.appspot.com/delete_fav/$recId"

        val deleteRequest = StringRequest(Request.Method.DELETE, url,
            { response ->
                // Handle the response
                Log.d("Volley", "Response: $response")
                onSuccess()
            },
            { error ->
                // Handle error
                Log.e("Volley", "Error: ${error.message}")
            }
        )

        requestQueue.add(deleteRequest)

    }

}
