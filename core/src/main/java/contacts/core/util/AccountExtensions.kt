package contacts.core.util

import android.accounts.Account
import android.accounts.AccountManager
import android.annotation.SuppressLint
import android.content.Context
import contacts.core.*

/**
 * Returns true if [this] is in the list of all accounts in the system.
 *
 * ## Permissions
 *
 * Requires [contacts.core.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
 *
 * ## Samsung devices
 *
 * Samsung devices use "vnd.sec.contact.phone" for the account name and type of local RawContacts
 * in the RawContacts table instead of null. This will return false for [Account] instances created
 * with this name and type because it is not an actual account that is registered/returned by the
 * system AccountManager.
 */
internal fun Account?.isInSystem(context: Context): Boolean = nullIfNotInSystem(context) != null

/**
 * Returns true if [this] is NOT in the list of all accounts in the system.
 *
 * ## Permissions
 *
 * Requires [contacts.core.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
 *
 * ## Samsung devices
 *
 * Samsung devices use "vnd.sec.contact.phone" for the account name and type of local RawContacts
 * in the RawContacts table instead of null. This will return true for [Account] instances created
 * with this name and type because it is not an actual account that is registered/returned by the
 * system AccountManager.
 */
internal fun Account?.isNotInSystem(context: Context): Boolean = !isInSystem(context)

/**
 * Verifies that [this] given [Account] is in the list of all accounts in the system and returns
 * itself. Otherwise, returns null.
 *
 * ## Permissions
 *
 * Requires [contacts.core.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
 *
 * ## Samsung devices
 *
 * Samsung devices use "vnd.sec.contact.phone" for the account name and type of local RawContacts
 * in the RawContacts table instead of null. This will return null for [Account] instances created
 * with this name and type because it is not an actual account that is registered/returned by the
 * system AccountManager.
 */
@SuppressLint("MissingPermission")
internal fun Account?.nullIfNotInSystem(context: Context): Account? = this?.let {
    nullIfNotIn(AccountManager.get(context.applicationContext).accounts.toList())
}

/**
 * Returns null if this is a Samsung phone Account, which is not returned by the AccountManager.
 */
internal fun Account.nullIfSamsungPhoneAccount(): Account? = if (
    name == SAMSUNG_PHONE_ACCOUNT && type == SAMSUNG_PHONE_ACCOUNT
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
            // RawContactsFields and GroupsFields AccountName and AccountType are the same.
            (GroupsFields.AccountName equalToIgnoreCase account.name) and
                    (GroupsFields.AccountType equalToIgnoreCase account.type)
        } else {
            (GroupsFields.AccountName.isNull() and GroupsFields.AccountType.isNull())
                .or(
                    (GroupsFields.AccountName equalTo SAMSUNG_PHONE_ACCOUNT) and
                            (GroupsFields.AccountType equalTo SAMSUNG_PHONE_ACCOUNT)
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
// This is NOT an actual Account and is not returned by the Android AccountManager.
// See https://github.com/vestrel00/contacts-android/issues/257
private const val SAMSUNG_PHONE_ACCOUNT = "vnd.sec.contact.phone"