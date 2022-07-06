package com.lutalic.smartdvr.presentation.graph

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.jjoe64.graphview.DefaultLabelFormatter
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import com.lutalic.smartdvr.databinding.FragmentGraphBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class GraphFragment : Fragment() {

    private val mDateFormat: DateFormat = SimpleDateFormat("HH:mm:ss")

    private val viewModel: GraphViewModel by viewModel()


    private lateinit var binding: FragmentGraphBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentGraphBinding.inflate(inflater, container, false)
        initGraph()
        return binding.root
    }

    private fun initGraph() {

        val mCalendar = Calendar.getInstance()

        val graph = binding.graph
        val mSeriesGraph: LineGraphSeries<DataPoint> = LineGraphSeries(
            viewModel.getAllAttentionData()
        )
        graph.addSeries(mSeriesGraph)
        val series2 = LineGraphSeries(
            viewModel.getAllMeditationData()
        )

        graph.secondScale.addSeries(series2)
        graph.secondScale.setMinY(0.0);
        graph.secondScale.setMaxY(100.0);
        series2.color = Color.GREEN;

        graph.viewport.isScrollable = true // enables horizontal scrolling

        graph.viewport.setScrollableY(true) // enables vertical scrolling

        graph.viewport.isScalable = true // enables horizontal zooming and scrolling

        graph.viewport.setScalableY(true) // enables vertical zooming and scrolling


        graph.gridLabelRenderer.labelFormatter = object : DefaultLabelFormatter() {
            override fun formatLabel(value: Double, isValueX: Boolean): String {
                return if (isValueX) {
                    mCalendar.timeInMillis = value.toLong()
                    mDateFormat.format(mCalendar.timeInMillis)
                } else {
                    super.formatLabel(value, isValueX)
                }
            }
        }

        graph.gridLabelRenderer.numHorizontalLabels = 5

        mSeriesGraph.color = Color.RED

    }

    companion object {
        fun newInstance() = GraphFragment()
    }
}