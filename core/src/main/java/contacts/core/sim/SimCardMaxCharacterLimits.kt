package contacts.core.sim

import android.content.ContentResolver
import android.content.Context
import android.content.SharedPreferences
import android.telephony.TelephonyManager
import contacts.core.Contacts
import contacts.core.ContactsPermissions
import contacts.core.contentResolver
import contacts.core.entities.NewSimContact
import contacts.core.isSimCardReady

/**
 * Provides functions to determine max character limits of the default/active SIM card for name and
 * number.
 */
interface SimCardMaxCharacterLimits {
    /**
     * The max character limit for SIM contact names. This is typically around 30 but will vary
     * per SIM card and/or device.
     *
     * This will be 0 if a SIM card is not ready or permissions are not granted.
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.READ_PERMISSION] and [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread. This is
     * only the case when the length has not yet been calculated. Subsequent calls will not
     * block the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun nameMaxLength(): Int

    /**
     * The max character limit for SIM contact names. This is typically around 30 but will vary
     * per SIM card and/or device.
     *
     * This will be 0 if a SIM card is not ready or permissions are not granted.
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.READ_PERMISSION] and [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Cancellation
     *
     * To cancel at any time, the [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread. This is
     * only the case when the length has not yet been calculated. Subsequent calls will not
     * block the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    // @JvmOverloads cannot be used in interface methods...
    // fun nameMaxLength(cancel: () -> Boolean = { false }): Result
    fun nameMaxLength(cancel: () -> Boolean): Int

    /**
     * The max character limit for SIM contact numbers. This is typically around 20 but will vary
     * per SIM card and/or device.
     *
     * This will be 0 if a SIM card is not ready or permissions are not granted.
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.READ_PERMISSION] and [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread. This is
     * only the case when the length has not yet been calculated. Subsequent calls will not
     * block the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    fun numberMaxLength(): Int

    /**
     * The max character limit for SIM contact numbers. This is typically around 20 but will vary
     * per SIM card and/or device.
     *
     * This will be 0 if a SIM card is not ready or permissions are not granted.
     *
     * ## Permissions
     *
     * Requires [ContactsPermissions.READ_PERMISSION] and [ContactsPermissions.WRITE_PERMISSION].
     *
     * ## Cancellation
     *
     * To cancel at any time, the [cancel] function should return true.
     *
     * This is useful when running this function in a background thread or coroutine.
     *
     * ## Thread Safety
     *
     * This should be called in a background thread to avoid blocking the UI thread. This is
     * only the case when the length has not yet been calculated. Subsequent calls will not
     * block the UI thread.
     */
    // [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
    // @JvmOverloads cannot be used in interface methods...
    // fun numberMaxLength(cancel: () -> Boolean = { false }): Result
    fun numberMaxLength(cancel: () -> Boolean): Int

    /**
     * To avoid performing blocking calls every time [nameMaxLength] and [numberMaxLength] are
     * accessed, this API caches those values to shared preferences. Call this function to clear
     * the cache/shared-prefs.
     *
     * You may want to invoke this function when a different SIM card is inserted, which may have
     * different character limits. Unfortunately, this API currently does not support automatic
     * detection of different SIM cards because of the additional privileges required
     * to access [TelephonyManager.getSimSerialNumber]. The cache will be cleared [nameMaxLength]
     * or [numberMaxLength] if a SIM card is not in a ready state.
     */
    fun clearCachedNameAndNumberMaxLengths()
}

@Suppress("FunctionName")
internal fun SimCardMaxCharacterLimits(contactsApi: Contacts): SimCardMaxCharacterLimits =
    SimCardMaxCharacterLimitsImpl(
        contactsApi,
        SimCardMaxCharacterLimitsPreferences(contactsApi.applicationContext)
    )

private class SimCardMaxCharacterLimitsImpl(
    private val contactsApi: Contacts,
    private val preferences: SimCardMaxCharacterLimitsPreferences
) : SimCardMaxCharacterLimits {

    private val hasPermissions: Boolean
        get() = contactsApi.permissions.canQuery() &&
                contactsApi.permissions.canInsertToSim() &&
                contactsApi.permissions.canUpdateDelete()

    override fun nameMaxLength(): Int = nameMaxLength { false }

    override fun nameMaxLength(cancel: () -> Boolean): Int =
        if (contactsApi.isSimCardReady && hasPermissions) {
            val cachedMaxLength = preferences.nameMaxLength
            if (cachedMaxLength > 0) {
                cachedMaxLength
            } else {
                val maxLength = contactsApi.contentResolver.maxCharLimit(
                    nameChar = "X",
                    numberChar = null,
                    initialMaxCharLimit = INITIAL_MAX_CHAR_LIMIT,
                    cancel = cancel
                )
                preferences.nameMaxLength = maxLength
                maxLength
            }
        } else {
            preferences.nameMaxLength = 0
            0
        }

    override fun numberMaxLength(): Int = numberMaxLength { false }

    override fun numberMaxLength(cancel: () -> Boolean): Int =
        if (contactsApi.isSimCardReady && hasPermissions) {
            val cachedMaxLength = preferences.numberMaxLength
            if (cachedMaxLength > 0) {
                cachedMaxLength
            } else {
                val maxLength = contactsApi.contentResolver.maxCharLimit(
                    nameChar = null,
                    numberChar = "1",
                    initialMaxCharLimit = INITIAL_MAX_CHAR_LIMIT,
                    cancel = cancel
                )
                preferences.numberMaxLength = maxLength
                maxLength
            }
        } else {
            preferences.numberMaxLength = 0
            0
        }

    override fun clearCachedNameAndNumberMaxLengths() {
        preferences.nameMaxLength = 0
        preferences.numberMaxLength = 0
    }

    private companion object {
        private const val INITIAL_MAX_CHAR_LIMIT = 30
    }
}

private class SimCardMaxCharacterLimitsPreferences(private val applicationContext: Context) {

    private val sharedPrefs: SharedPreferences
        get() = applicationContext.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    var nameMaxLength: Int
        get() = sharedPrefs.getInt(NAME_MAX_LENGTH, 0)
        set(value) = sharedPrefs.edit().putInt(NAME_MAX_LENGTH, value).apply()

    var numberMaxLength: Int
        get() = sharedPrefs.getInt(NUMBER_MAX_LENGTH, 0)
        set(value) = sharedPrefs.edit().putInt(NUMBER_MAX_LENGTH, value).apply()

    private companion object {
        private const val SHARED_PREFS_NAME =
            "contacts.core.sim.SimCardMaxCharacterLimits.SHARED_PREFS"
        private const val NAME_MAX_LENGTH = "NAME_MAX_LENGTH"
        private const val NUMBER_MAX_LENGTH = "NUMBER_MAX_LENGTH"
    }
}

private fun ContentResolver.maxCharLimit(
    nameChar: String?,
    numberChar: String?,
    initialMaxCharLimit: Int,
    cancel: () -> Boolean
): Int {
    var insertSuccess = false
    var length = initialMaxCharLimit
    while (!insertSuccess && length > 0 && !cancel()) {
        val name = nameChar?.repeat(length)
        val number = numberChar?.repeat(length)
        insertSuccess = insertSimContact(NewSimContact(name = name, number = number), cancel)
        if (insertSuccess) {
            val insertedSimContact = getSimContacts(cancel)
                .find { it.name == name && it.number == number }
            if (insertedSimContact == null) {
                // Some devices return successful result even though a new row was not actually
                // inserted. Therefore, we have to check for this possibility.
                insertSuccess = false
            } else {
                deleteSimContact(insertedSimContact)
            }
        } else {
            length--
        }
    }

    return if (cancel()) {
        0
    } else {
        length
    }
}