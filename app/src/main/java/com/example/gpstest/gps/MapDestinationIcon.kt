package com.example.gpstest.gps

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import androidx.annotation.DrawableRes
import androidx.appcompat.content.res.AppCompatResources
import com.example.gpstest.R
import com.mapbox.geojson.Point
import com.mapbox.maps.MapView
import com.mapbox.maps.MapboxMap
import com.mapbox.maps.extension.style.layers.properties.generated.IconAnchor
import com.mapbox.maps.plugin.annotation.annotations
import com.mapbox.maps.plugin.annotation.generated.PointAnnotationOptions
import com.mapbox.maps.plugin.annotation.generated.createPointAnnotationManager

class MapDestinationIcon(mapView: MapView, context: Context) {
    private val annotations = mutableMapOf<Long, Point>()
    private val mapboxMap: MapboxMap = mapView.getMapboxMap()
    private val pointAnnotationManager = mapView.annotations.createPointAnnotationManager(null)
    private val pinBitmap = bitmapFromDrawableRes(context, R.drawable.destination_point)

    private fun bitmapFromDrawableRes(context: Context, @DrawableRes resourceId: Int) =
        convertDrawableToBitmap(AppCompatResources.getDrawable(context, resourceId))

    private fun convertDrawableToBitmap(sourceDrawable: Drawable?): Bitmap? {
        if (sourceDrawable == null) {
            return null
        }
        return if (sourceDrawable is BitmapDrawable) {
            sourceDrawable.bitmap
        } else {
// copying drawable object to not manipulate on the same reference
            val constantState = sourceDrawable.constantState ?: return null
            val drawable = constantState.newDrawable().mutate()
            val bitmap: Bitmap = Bitmap.createBitmap(
                drawable.intrinsicWidth, drawable.intrinsicHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
            bitmap
        }
    }

    fun clearMarkers() {
        pointAnnotationManager.deleteAll()
        annotations.clear()
    }

    fun showResults(results: Point) {
        clearMarkers()

        val options = PointAnnotationOptions()
            .withPoint(results)
            .withIconImage(pinBitmap!!)
            .withIconAnchor(IconAnchor.BOTTOM)
            .withIconSize(0.5)

        val annotation = pointAnnotationManager.create(options)
        annotations[annotation.id] = results
        // mapboxMap.setCamera(cameraOptions)
    }
}