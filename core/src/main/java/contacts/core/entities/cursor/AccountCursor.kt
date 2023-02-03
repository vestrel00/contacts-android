package contacts.core.entities.cursor

import android.accounts.Account

sealed interface AccountCursor {

    val accountName: String?

    val accountType: String?
}

internal fun AccountCursor.account(): Account? {
    val name = accountName
    val type = accountType

    return if (
        name != null && type != null &&
        name != SAMSUNG_PHONE_ACCOUNT && type != SAMSUNG_PHONE_ACCOUNT
    ) {
        Account(name, type)
    } else {
        null
    }
}

// Samsung devices use "vnd.sec.contact.phone" for local account name and type instead of null.
// This is NOT an actual Account and is not returned by the Android AccountManager.
// See https://github.com/vestrel00/contacts-android/issues/257
internal const val SAMSUNG_PHONE_ACCOUNT = "vnd.sec.contact.phone"