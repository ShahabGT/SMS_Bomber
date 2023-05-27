package ir.shahabazimi.smsbomber

import android.app.Application
import com.google.android.material.color.DynamicColors

open class ApplicationClass : Application() {

    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}