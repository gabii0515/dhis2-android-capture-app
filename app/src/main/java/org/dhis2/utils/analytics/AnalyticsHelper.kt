package org.dhis2.utils.analytics

import android.annotation.SuppressLint
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import javax.inject.Inject
import org.dhis2.data.prefs.PreferenceProvider
import org.dhis2.utils.Constants
import org.hisp.dhis.android.core.D2Manager

class AnalyticsHelper @Inject constructor(
    val analytics: FirebaseAnalytics,
    private val preferencesProvider: PreferenceProvider
) {

    @SuppressLint("CheckResult")
    fun setEvent(param: String, value: String, event: String) {
        val d2 = D2Manager.getD2()
        if (d2 != null && d2.userModule().isLogged.blockingGet()) {
            val user = d2.userModule().userCredentials().blockingGet()
            val info = d2.systemInfoModule().systemInfo().blockingGet()
            setBundleEvent(param, value, event, user.username(), info.contextPath())
        } else setBundleEvent(param, value, event)
    }

    fun setEvent(event: String, params: Map<String, String>) {
        val bundle = Bundle()
        val d2 = D2Manager.getD2()

        if (d2 != null && d2.userModule().blockingIsLogged()) {
            if (preferencesProvider.contains(Constants.USER)) {
                analytics.setUserProperty(
                    USER_PROPERTY_NAME,
                    d2.userModule().userCredentials().blockingGet().username()
                )
            }
            analytics.setUserProperty(
                USER_PROPERTY_SERVER,
                d2.systemInfoModule().systemInfo().blockingGet().contextPath()
            )
        }
        params.entries.forEach { bundle.putString(it.key, it.value) }
        logEvent(event, bundle)
    }

    /**
     * Mask to use from Java
     * */
    private fun setBundleEvent(param: String, value: String, event: String) {
        setBundleEvent(param, value, event, "", "")
    }

    private fun setBundleEvent(
        param: String,
        value: String,
        event: String,
        user: String? = "",
        server: String? = ""
    ) {
        val bundle = Bundle()
        bundle.apply {
            if (!user.isNullOrEmpty() && !server.isNullOrEmpty()) {
                putString("user", user)
                putString("server", server)
            }
            putString(param, value)
        }

        logEvent(event, bundle)
    }

    private fun logEvent(event: String, bundle: Bundle) {
        analytics.logEvent(event, bundle)
    }
}
