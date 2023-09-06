package contacts.sample

import android.accounts.Account
import android.content.Context
import android.content.SharedPreferences
import contacts.sample.PhoneticName.ALWAYS_SHOW
import contacts.sample.PhoneticName.HIDE_IF_EMPTY

class SampleAppPreferences(private val context: Context) {

    private val sharedPrefs: SharedPreferences
        get() = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    var defaultAccountForNewContacts: Account?
        get() {
            val accountName = sharedPrefs.getString(ACCOUNT_NAME, null)
            val accountType = sharedPrefs.getString(ACCOUNT_TYPE, null)
            return if (accountName != null && accountType != null) {
                Account(accountName, accountType)
            } else {
                null
            }
        }
        set(value) {
            sharedPrefs
                .edit()
                .putString(ACCOUNT_NAME, value?.name)
                .putString(ACCOUNT_TYPE, value?.type)
                .apply()
        }

    var sortBy: SortBy
        get() = sharedPrefs.getString(SORT_BY, null)?.let(SortBy::valueOf)
            ?: SortBy.FIRST_NAME
        set(value) {
            sharedPrefs
                .edit()
                .putString(SORT_BY, value.name)
                .apply()
        }

    var nameFormat: NameFormat
        get() = sharedPrefs.getString(NAME_FORMAT, null)?.let(NameFormat::valueOf)
            ?: NameFormat.FIRST_NAME_FIRST
        set(value) {
            sharedPrefs
                .edit()
                .putString(NAME_FORMAT, value.name)
                .apply()
        }


    var phoneticName: PhoneticName
        get() = sharedPrefs.getString(PHONETIC_NAME, null)?.let(PhoneticName::valueOf)
            ?: ALWAYS_SHOW
        set(value) {
            sharedPrefs
                .edit()
                .putString(PHONETIC_NAME, value.name)
                .apply()
        }
}

enum class SortBy(private val value: String) {
    FIRST_NAME("First name"),
    LAST_NAME("Last name");

    override fun toString() = "Sort by: $value"
}

enum class NameFormat(private val value: String) {
    FIRST_NAME_FIRST("First name first"),
    LAST_NAME_FIRST("Last name first");

    override fun toString() = "Name format: $value"
}

/**
 * Determines the visibility of phonetic name fields in the contact details' view mode.
 *
 * - When [ALWAYS_SHOW], phonetic name fields will be shown even if they are all empty.
 * - When [HIDE_IF_EMPTY], phonetic name fields will be hidden if all are empty.
 *
 * Phonetic name fields will always be visible in edit mode.
 *
 * This has nothing to do with the contact primary or alt display name.
 */
enum class PhoneticName(private val value: String) {
    ALWAYS_SHOW("Always show"),
    HIDE_IF_EMPTY("Hide if empty");

    override fun toString() = "Phonetic name: $value"
}

private const val SHARED_PREFS_NAME = "contacts.sample.SHARED_PREFS"

private const val ACCOUNT_NAME = "ACCOUNT_NAME"
private const val ACCOUNT_TYPE = "ACCOUNT_TYPE"

private const val SORT_BY = "SORT_BY"
private const val NAME_FORMAT = "NAME_FORMAT"
private const val PHONETIC_NAME = "PHONETIC_NAME"