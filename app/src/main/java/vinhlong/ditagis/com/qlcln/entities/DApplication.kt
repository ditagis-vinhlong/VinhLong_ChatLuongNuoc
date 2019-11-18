package vinhlong.ditagis.com.qlcln.entities


import android.app.Application
import android.location.Location
import vinhlong.ditagis.com.qlcln.entities.entitiesDB.User

class DApplication : Application() {


    var user: User? = null
    private var mLocation: Location? = null


    fun setmLocation(mLocation: Location) {
        this.mLocation = mLocation
    }
}