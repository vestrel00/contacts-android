package contacts.core.util

import android.provider.ContactsContract

/**
 * True if this ID is a Profile row.
 *
 * This method can be used to identify whether the given ID is associated with profile data. It
 * does not necessarily indicate that the ID is tied to valid data, merely that accessing data
 * using this ID will result in profile access checks and will only return data from the profile.
 */
internal val Long.isProfileId: Boolean
    get() = ContactsContract.isProfileId(this)

/**
 * Returns true if all IDs in the collection are Profile IDs.
 *
 * If the collection is empty, this will return false.
 */
internal val Collection<Long>.allAreProfileIds: Boolean
    // By default, all returns true when the collection is empty. So, we override that.
    get() = if (isEmpty()) false else all { it.isProfileId }