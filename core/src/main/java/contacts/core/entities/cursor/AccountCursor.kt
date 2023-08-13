package contacts.core.entities.cursor

import android.accounts.Account
import contacts.core.util.nullIfSamsungOrXiaomiLocalAccount

sealed interface AccountCursor {

    val accountName: String?

    val accountType: String?
}

internal fun AccountCursor.account(): Account? {
    val name = accountName
    val type = accountType

    return if (name != null && type != null) {
        Account(name, type).nullIfSamsungOrXiaomiLocalAccount()
    } else {
        null
    }
}