package contacts.core.util

import android.provider.ContactsContract

internal val Long.isProfileId: Boolean
    get() = ContactsContract.isProfileId(this)