package contacts.core.util

import android.accounts.Account
import android.annotation.SuppressLint
import contacts.core.*

/**
 * Returns true if [this] is in the list of all accounts in the system.
 *
 * ## Permissions
 *
 * Requires [contacts.core.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
 *
 * ## Samsung and Xiaomi devices
 *
 * Samsung and Xiaomi devices use non-null values for the account name and type of local RawContacts
 * in the RawContacts table instead of null. This will return false for [Account] instances created
 * with this name and type.
 */
internal fun Account?.isInSystem(contactsApi: Contacts): Boolean =
    nullIfNotInSystem(contactsApi) != null

/**
 * Returns true if [this] is NOT in the list of all accounts in the system.
 *
 * ## Permissions
 *
 * Requires [contacts.core.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
 *
 * ## Samsung and Xiaomi devices
 *
 * Samsung and Xiaomi devices use non-null values for the account name and type of local RawContacts
 * in the RawContacts table instead of null. This will return true for [Account] instances created
 * with this name and type.
 */
internal fun Account?.isNotInSystem(contactsApi: Contacts): Boolean = !isInSystem(contactsApi)

/**
 * Verifies that [this] given [Account] is in the list of all accounts in the system and returns
 * itself. Otherwise, returns null.
 *
 * ## Permissions
 *
 * Requires [contacts.core.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
 *
 * ## Samsung and Xiaomi devices
 *
 * Samsung and Xiaomi devices use non-null values for the account name and type of local RawContacts
 * in the RawContacts table instead of null. This will return null for [Account] instances created
 * with this name and type.
 */
@SuppressLint("MissingPermission")
internal fun Account?.nullIfNotInSystem(contactsApi: Contacts): Account? = this?.let {
    nullIfNotIn(contactsApi.accounts().query().find())
}

/**
 * Returns null if this is a Samsung or Xiaomi device/phone Account, which is not returned by the
 * AccountManager.
 */
internal fun Account.nullIfSamsungOrXiaomiLocalAccount(): Account? = if (
    (name == SAMSUNG_PHONE_ACCOUNT && type == SAMSUNG_PHONE_ACCOUNT) ||
    (name == XIAOMI_PHONE_ACCOUNT_NAME && type == XIAOMI_PHONE_ACCOUNT_TYPE)
) { null } else { this }

/**
 * Verifies that [this] given [Account] is in the list of given [accounts] and returns itself.
 * Otherwise, returns null.
 */
private fun Account.nullIfNotIn(accounts: List<Account>): Account? =
    if (accounts.contains(this)) this else null

/*
 * A note about toRawContactsWhere and toGroupsWhere.
 *
 * Yes, I know that the column names of RawContactsFields and GroupsFields AccountName and
 * AccountType are the same. Two different functions exist that have outputs with the same
 * underlying data for adding type to the Where.
 *
 * - Output of toRawContactsWhere may be combined with other RawContactsFields.
 * - Output of toGroupsWhere may be combined with other GroupsFields.
 */

/**
 * Uses [whereOr] to form a where clause that matches the given [Account]. This is for use in
 * RawContacts table queries.
 */
internal fun Account?.toRawContactsWhere(): Where<RawContactsField> =
    // Assume that this will not return a null Where because there is one element in the sequence.
    sequenceOf(this).toRawContactsWhere() as Where<RawContactsField>

/**
 * Uses [whereOr] to form a where clause that matches any of the given [Account]s. This is for use
 * in RawContacts table queries.
 *
 * If the sequence is empty, returns null.
 */
internal fun Collection<Account?>.toRawContactsWhere(): Where<RawContactsField>? =
    asSequence().toRawContactsWhere()

/**
 * Uses [whereOr] to form a where clause that matches any of the given [Account]s. This is for use
 * in RawContacts table queries.
 *
 * If the sequence is empty, returns null.
 */
internal fun Sequence<Account?>.toRawContactsWhere(): Where<RawContactsField>? = distinct()
    .whereOr { account ->
        if (account != null) {
            (RawContactsFields.AccountName equalToIgnoreCase account.name) and
                    (RawContactsFields.AccountType equalToIgnoreCase account.type)
        } else {
            (RawContactsFields.AccountName.isNull() and RawContactsFields.AccountType.isNull())
                .or(
                    (RawContactsFields.AccountName equalTo SAMSUNG_PHONE_ACCOUNT) and
                            (RawContactsFields.AccountType equalTo SAMSUNG_PHONE_ACCOUNT)
                )
                .or(
                    (RawContactsFields.AccountName equalTo XIAOMI_PHONE_ACCOUNT_NAME) and
                            (RawContactsFields.AccountType equalTo XIAOMI_PHONE_ACCOUNT_TYPE)
                )
        }
    }

/**
 * Uses [whereOr] to form a where clause that matches any of the given [Account]s. This is for use
 * in Groups table queries.
 *
 * If the sequence is empty, returns null.
 */
internal fun Sequence<Account?>.toGroupsWhere(): Where<GroupsField>? = distinct()
    .whereOr { account ->
        if (account != null) {
            (GroupsFields.AccountName equalToIgnoreCase account.name) and
                    (GroupsFields.AccountType equalToIgnoreCase account.type)
        } else {
            (GroupsFields.AccountName.isNull() and GroupsFields.AccountType.isNull())
                .or(
                    (GroupsFields.AccountName equalTo SAMSUNG_PHONE_ACCOUNT) and
                            (GroupsFields.AccountType equalTo SAMSUNG_PHONE_ACCOUNT)
                )
                .or(
                    (GroupsFields.AccountName equalTo XIAOMI_PHONE_ACCOUNT_NAME) and
                            (GroupsFields.AccountType equalTo XIAOMI_PHONE_ACCOUNT_TYPE)
                )
        }
    }

// region Redactable

/**
 * Returns a copy of this account where the [Account.name] and [Account.type] are redacted.
 */
fun Account.redactedCopy(): Account = Account(
    name.redactString(),
    type.redactString()
)

/**
 * If [redact] is true, returns a copy of this account where the [Account.name] and [Account.type]
 * are redacted. Otherwise, just returns this.
 */
fun Account.redactedCopyOrThis(redact: Boolean) = if (redact) redactedCopy() else this

/**
 * Returns a copy of every account in this collection such that the [Account.name] and
 * [Account.type] are redacted.
 */
fun Collection<Account>.redactedCopies(): List<Account> = map { it.redactedCopy() }

/**
 * If [redact] is true, returns a copy of every account in this collection such that the
 * [Account.name] and [Account.type] are redacted. Otherwise, just returns this.
 */
fun Collection<Account>.redactedCopiesOrThis(redact: Boolean) =
    if (redact) redactedCopies() else this

/**
 * Returns a copy of every account in this sequence such that the [Account.name] and
 * [Account.type] are redacted.
 */
fun Sequence<Account>.redactedCopies(): Sequence<Account> = map { it.redactedCopy() }

/**
 * If [redact] is true, returns a copy of every account in this sequence such that the
 * [Account.name] and [Account.type] are redacted. Otherwise, just returns this.
 */
fun Sequence<Account>.redactedCopiesOrThis(redact: Boolean) =
    if (redact) redactedCopies() else this

// endregion

// Samsung devices use "vnd.sec.contact.phone" for local account name and type instead of null.
// See https://github.com/vestrel00/contacts-android/issues/257
private const val SAMSUNG_PHONE_ACCOUNT = "vnd.sec.contact.phone"


// Xiaomi devices use "default" and "com.android.contacts.default" for local account name and type
// respectively instead of null.
// See https://github.com/vestrel00/contacts-android/issues/296
private const val XIAOMI_PHONE_ACCOUNT_NAME = "default"
private const val XIAOMI_PHONE_ACCOUNT_TYPE = "com.android.contacts.default"