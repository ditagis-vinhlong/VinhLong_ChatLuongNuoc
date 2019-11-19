package vinhlong.ditagis.com.qlcln.entities

import com.esri.arcgisruntime.geometry.GeometryEngine
import com.esri.arcgisruntime.geometry.Point
import com.esri.arcgisruntime.geometry.SpatialReferences

class DAddress(val longtitude: Double, val latitude: Double, val subAdminArea: String?, val adminArea: String?, val location: String) {
    val point: Point
        get() {
            val pointLongLat = Point(this.longtitude, this.latitude)
            val geometryWg = GeometryEngine.project(pointLongLat, SpatialReferences.getWgs84())
            val geometryWM = GeometryEngine.project(geometryWg, SpatialReferences.getWebMercator())
            return geometryWM.extent.center
        }
}
