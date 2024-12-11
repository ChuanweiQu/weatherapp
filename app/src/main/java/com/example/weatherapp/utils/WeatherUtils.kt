package com.example.weatherapp.utils

import com.example.weatherapp.R

object WeatherUtils {
    private val weatherDescriptions = mapOf(
        0 to "Unknown",
        1000 to "Clear",
        1100 to "Mostly Clear",
        1101 to "Partly Cloudy",
        1102 to "Mostly Cloudy",
        1001 to "Cloudy",
        2000 to "Fog",
        2100 to "Light Fog",
        4000 to "Drizzle",
        4001 to "Rain",
        4200 to "Light Rain",
        4201 to "Heavy Rain",
        5000 to "Snow",
        5001 to "Flurries",
        5100 to "Light Snow",
        5101 to "Heavy Snow",
        6000 to "Freezing Drizzle",
        6001 to "Freezing Rain",
        6200 to "Light Freezing Rain",
        6201 to "Heavy Freezing Rain",
        7000 to "Ice Pellets",
        7101 to "Heavy Ice Pellets",
        7102 to "Light Ice Pellets",
        8000 to "Thunderstorm"
    )

    private val weatherIcons = mapOf(
        0 to "Unknown",
        1000 to R.drawable.clear_day,
        1100 to R.drawable.mostly_clear_day,
        1101 to R.drawable.partly_cloudy_day,
        1102 to R.drawable.mostly_cloudy,
        1001 to R.drawable.cloudy,
        2000 to R.drawable.fog,
        2100 to R.drawable.fog_light,
        4000 to R.drawable.drizzle,
        4001 to R.drawable.rain,
        4200 to R.drawable.rain_light,
        4201 to R.drawable.rain_heavy,
        5000 to R.drawable.snow,
        5001 to R.drawable.flurries,
        5100 to R.drawable.snow_light,
        5101 to R.drawable.snow_heavy,
        6000 to R.drawable.freezing_drizzle,
        6001 to R.drawable.freezing_rain,
        6200 to R.drawable.freezing_rain_light,
        6201 to R.drawable.freezing_rain_heavy,
        7000 to R.drawable.ice_pellets,
        7101 to R.drawable.ice_pellets_heavy,
        7102 to R.drawable.ice_pellets_light,
        8000 to R.drawable.tstorm
    )

    val stateAbb = mapOf(
        "Alabama" to "AL",
        "Alaska" to "AK",
        "Arizona" to "AZ",
        "Arkansas" to "AR",
        "California" to "CA",
        "Colorado" to "CO",
        "Connecticut" to "CT",
        "Delaware" to "DE",
        "Florida" to "FL",
        "Georgia" to "GA",
        "Hawaii" to "HI",
        "Idaho" to "ID",
        "Illinois" to "IL",
        "Indiana" to "IN",
        "Iowa" to "IA",
        "Kansas" to "KS",
        "Kentucky" to "KY",
        "Louisiana" to "LA",
        "Maine" to "ME",
        "Maryland" to "MD",
        "Massachusetts" to "MA",
        "Michigan" to "MI",
        "Minnesota" to "MN",
        "Mississippi" to "MS",
        "Missouri" to "MO",
        "Montana" to "MT",
        "Nebraska" to "NE",
        "Nevada" to "NV",
        "New Hampshire" to "NH",
        "New Jersey" to "NJ",
        "New Mexico" to "NM",
        "New York" to "NY",
        "North Carolina" to "NC",
        "North Dakota" to "ND",
        "Ohio" to "OH",
        "Oklahoma" to "OK",
        "Oregon" to "OR",
        "Pennsylvania" to "PA",
        "Rhode Island" to "RI",
        "South Carolina" to "SC",
        "South Dakota" to "SD",
        "Tennessee" to "TN",
        "Texas" to "TX",
        "Utah" to "UT",
        "Vermont" to "VT",
        "Virginia" to "VA",
        "Washington" to "WA",
        "West Virginia" to "WV",
        "Wisconsin" to "WI",
        "Wyoming" to "WY"
    )

    fun getWeatherDescription(weatherCode: Int): String {
        return weatherDescriptions[weatherCode] ?: "Unknown"
    }

    fun getWeatherIcon(weatherCode: Int): Int {
        return weatherIcons[weatherCode] as Int
    }
}