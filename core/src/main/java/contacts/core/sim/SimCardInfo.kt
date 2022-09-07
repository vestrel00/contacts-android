package contacts.core.sim

import android.app.Activity
import android.content.Context
import android.telephony.TelephonyManager

/**
 * Provides functions for checking SIM card state, max character limits, etc.
 */
interface SimCardInfo {

    /**
     * Returns true if the default/active SIM card is ready for use.
     */
    val isReady: Boolean
}

@Suppress("FunctionName")
internal fun SimCardInfo(context: Context): SimCardInfo =
    SimCardInfoImpl(context.applicationContext)

private class SimCardInfoImpl(private val applicationContext: Context) : SimCardInfo {

    private val telephonyManager: TelephonyManager
        get() = applicationContext.getSystemService(Activity.TELEPHONY_SERVICE) as TelephonyManager

    override val isReady: Boolean
        get() = telephonyManager.simState == TelephonyManager.SIM_STATE_READY
}