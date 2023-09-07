package contacts.sample

import android.annotation.TargetApi
import android.os.Build
import android.telecom.InCallService

/**
 * Starting with API 33 (Tiramisu), to qualify for the "android.app.role.DIALER" role, an
 * application needs to handle the intent to dial, and implement an [InCallService].
 *
 * This is required in order for [contacts.ui.util.requestToBeTheDefaultDialerApp] to work on
 * devices running Tiramisu or higher.
 *
 * See https://developer.android.com/reference/androidx/core/role/RoleManagerCompat#ROLE_DIALER()
 */
@TargetApi(Build.VERSION_CODES.M)
class SampleInCallServiceForDialerRoleRequest: InCallService()