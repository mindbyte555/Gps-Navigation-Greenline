package com.example.gpstest.gps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.example.gpstest.R
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationManager
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager

class MessageOnMap(val context: Context, mapView: MapView) {
    private val annotations = mutableMapOf<Long, Point>()
    var pointAnnotationManager: PointAnnotationManager =
        mapView.annotations.createPointAnnotationManager(null)

    val customMarkerView = LayoutInflater.from(context).inflate(R.layout.layout_custom_marker, null)
    val title = customMarkerView.findViewById<TextView>(R.id.textTitle)


    fun textAssign(text: String) {
        title.text = "$text"
    }

    fun convertViewToBitmap(view: View): Bitmap {
        val measureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED)
        view.measure(measureSpec, measureSpec)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)

        val bitmap =
            Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)

        return bitmap
    }

    fun showResults(results: Point) {
        val customMarkerBitmap = convertViewToBitmap(customMarkerView)
        pointAnnotationManager.deleteAll()
        annotations.clear()
        val options = PointAnnotationOptions()
            .withPoint(results)
            .withIconImage(customMarkerBitmap)
            .withIconAnchor(IconAnchor.BOTTOM_LEFT)
            .withIconSize(0.2)

        val annotation = pointAnnotationManager.create(options)
        annotations[annotation.id] = results
        // mapboxMap.setCamera(cameraOptions)
    }

    fun clearMarkers() {
        pointAnnotationManager.deleteAll()
        annotations.clear()
    }
}