package com.example.weatherapp.view.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.weatherapp.R
import com.example.weatherapp.viewmodel.HeavyTaskIndicator
import com.example.weatherapp.viewmodel.WeatherViewModel
import com.highsoft.highcharts.core.*
import com.highsoft.highcharts.common.hichartsclasses.*
import com.highsoft.highcharts.common.HIColor
import com.highsoft.highcharts.common.HIGradient
import com.highsoft.highcharts.common.HIStop
import java.util.LinkedList

class WeeklyFragment : Fragment() {

    private val heavyTaskIndicator: HeavyTaskIndicator by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_weekly, container, false)

        val chartView: HIChartView = view.findViewById(R.id.hc_temperature_range)
        val loadingPage: View = view.findViewById(R.id.loading_page)
        val contentPage: LinearLayout = view.findViewById(R.id.content_page)

        val temperatureChartOptions = arguments?.getSerializable("temperature_chart_options") as? List<Triple<Long, Int, Int>>
        temperatureChartOptions?.let {
            heavyTaskIndicator.doHeavyTask { renderChart(chartView, it) }
        }

        heavyTaskIndicator.isCompleted.observe(viewLifecycleOwner, { isCompleted ->
            if (isCompleted) {
                contentPage.visibility = View.VISIBLE
                loadingPage.visibility = View.GONE
                heavyTaskIndicator.setCompletedFalse()
            }
        })
        return view
    }

    private fun renderChart(chartView: HIChartView, data: List<Triple<Long, Int, Int>>) {
        val options = HIOptions()

        val chart = HIChart().apply {
            type = "arearange"
        }
        options.chart = chart

        var title = HITitle().apply {
            text = "Temperature variation by day"
        }
        options.title = title

        val xAxis = HIXAxis().apply {
            type = "datetime"
        }
        options.xAxis = arrayListOf(xAxis)

        val yAxis = HIYAxis()
        val yAxisTitle = HITitle()
        yAxisTitle.text = "Temperature(°F)"
        yAxis.setTitle(yAxisTitle)
        options.yAxis = arrayListOf(yAxis)

        val tooltip = HITooltip().apply {
            val shadowOptions = HIShadowOptionsObject().apply { enabled = true }
            shadow = shadowOptions
            valueSuffix = "°F"
        }
        options.tooltip = tooltip

        val legend = HILegend().apply {
            enabled = false
        }
        options.legend = legend

        val series = HIArearange().apply {
            name = "Temperatures"
            val seriesData = data.map { arrayOf<Any>(it.first, it.second, it.third) }
            this.data = ArrayList(seriesData)


            val gradient = HIGradient(0.0f, 0.0f, 0.0f, 1.0f)
            val stopsList = LinkedList<HIStop>()
            stopsList.add(HIStop(0f, HIColor.initWithHexValue("e1a84c")))
            stopsList.add(HIStop(1f, HIColor.initWithHexValue("c4e5fb")))

            val gradientColor = HIColor.initWithLinearGradient(gradient, stopsList)
            color = gradientColor
            lineColor = HIColor.initWithHexValue("27A3FC")
        }


        options.series = arrayListOf(series)
        chartView.options = options
    }

}