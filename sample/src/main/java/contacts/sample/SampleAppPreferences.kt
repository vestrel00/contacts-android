package contacts.sample

import android.accounts.Account
import android.content.Context
import android.content.SharedPreferences

class SampleAppPreferences(private val context: Context) {

    private val sharedPrefs: SharedPreferences
        get() = context.getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

    var defaultAccountForNewContacts: Account? = null
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
            field = value
            sharedPrefs
                .edit()
                .putString(ACCOUNT_NAME, value?.name)
                .putString(ACCOUNT_TYPE, value?.type)
                .apply()
        }
}

private const val SHARED_PREFS_NAME = "contacts.sample.SHARED_PREFS"

private const val ACCOUNT_NAME = "ACCOUNT_NAME"
private const val ACCOUNT_TYPE = "ACCOUNT_TYPE"