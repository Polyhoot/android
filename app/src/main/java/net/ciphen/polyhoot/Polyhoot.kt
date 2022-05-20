package net.ciphen.polyhoot

import com.google.android.material.color.DynamicColors

class Polyhoot: android.app.Application() {
    override fun onCreate() {
        super.onCreate()
        DynamicColors.applyToActivitiesIfAvailable(this)
    }
}
