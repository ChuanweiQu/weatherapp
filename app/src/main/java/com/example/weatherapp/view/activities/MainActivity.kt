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
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.viewpager2.widget.ViewPager2
import com.example.weatherapp.R
import com.example.weatherapp.view.adapters.ForecastAdapter
import com.example.weatherapp.view.adapters.HomeTabAdapter
import com.example.weatherapp.viewmodel.WeatherViewModel
import com.example.weatherapp.viewmodel.FavRecord
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


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
        weatherViewModel.favoriteLocations.observe(this, {favLocations ->
            loadHomeScreen(favLocations)
        })
        weatherViewModel.loadFavLocations()
    }

    private fun loadHomeScreen(favRecords: ArrayList<FavRecord>){

        val mapSearchButton: ImageButton = findViewById(R.id.map_search)
        val clearSearchButton: ImageButton = findViewById(R.id.clear_search)
        val exitSearchButton: ImageButton = findViewById(R.id.exit_search)
        val citySearchInput: AutoCompleteTextView = findViewById(R.id.city_search_input)
        val homePager = findViewById<ViewPager2>(R.id.home_pager)
        val homeTabs = findViewById<TabLayout>(R.id.home_tabs)

        homeTabs.removeAllTabs()
        for (i in 0 until favRecords.size) {
            homeTabs.addTab(homeTabs.newTab())
        }

        val adapter = HomeTabAdapter(this, favRecords)
        homePager.adapter = adapter

        TabLayoutMediator(homeTabs, homePager) { tab, position ->
            tab.icon = AppCompatResources.getDrawable(this, R.drawable.tab_white_circle)
        }.attach()


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

}

