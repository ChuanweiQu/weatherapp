package com.example.weatherapp.view.adapters
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.weatherapp.viewmodel.FavRecord
import com.example.weatherapp.view.fragments.HomeScreen

class HomeTabAdapter (activity: AppCompatActivity, favLocations: ArrayList<FavRecord>)
    : FragmentStateAdapter(activity) {
        private val _favLocations = favLocations
    override fun getItemCount() : Int {
        return _favLocations.size
    }

    override fun createFragment(position: Int): Fragment {
        val fragment = HomeScreen()
        fragment.arguments = Bundle().apply {
            putString("locationText",
                _favLocations[position].city + ", " + _favLocations[position].state)
        }
        return fragment
    }
}