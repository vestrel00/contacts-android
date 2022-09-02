package contacts.core.sim

import android.app.Activity
import android.content.Context
import android.telephony.TelephonyManager

/**
 * Provides functions for checking SIM card states.
 */
interface SimCardState {

    /**
     * Returns true if the default/active SIM card is ready for use.
     */
    val isReady: Boolean
}

@Suppress("FunctionName")
internal fun SimCardState(context: Context): SimCardState =
    SimCardStateImpl(context.applicationContext)

private class SimCardStateImpl(private val applicationContext: Context) : SimCardState {

    private val telephonyManager: TelephonyManager
        get() = applicationContext.getSystemService(Activity.TELEPHONY_SERVICE) as TelephonyManager

    override val isReady: Boolean
        get() = telephonyManager.simState == TelephonyManager.SIM_STATE_READY
}