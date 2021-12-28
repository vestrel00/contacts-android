package contacts.core.util

import android.accounts.Account
import contacts.core.*
import contacts.core.accounts.Accounts

/**
 * Returns true if [this] is in the list of all accounts in the system.
 *
 * ## Permissions
 *
 * Requires [contacts.core.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
 */
internal fun Account.isInSystem(accounts: Accounts): Boolean = nullIfNotInSystem(accounts) != null

/**
 * Returns true if [this] is NOT in the list of all accounts in the system.
 *
 * ## Permissions
 *
 * Requires [contacts.core.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
 */
internal fun Account.isNotInSystem(accounts: Accounts): Boolean = !isInSystem(accounts)

/**
 * Verifies that [this] given [Account] is in the list of all accounts in the system and returns
 * itself. Otherwise, returns null.
 *
 * ## Permissions
 *
 * Requires [contacts.core.accounts.AccountsPermissions.GET_ACCOUNTS_PERMISSION].
 */
internal fun Account.nullIfNotInSystem(accounts: Accounts): Account? =
    nullIfNotIn(accounts.query().allAccounts())

/**
 * Verifies that [this] given [Account] is in the list of given [accounts] and returns itself.
 * Otherwise, returns null.
 */
internal fun Account.nullIfNotIn(accounts: List<Account>): Account? =
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
internal fun Sequence<Account?>.toRawContactsWhere(): Where<RawContactsField>? = distinct()
    .whereOr { account ->
        if (account != null) {
            (RawContactsFields.AccountName equalToIgnoreCase account.name) and
                    (RawContactsFields.AccountType equalToIgnoreCase account.type)
        } else {
            RawContactsFields.AccountName.isNull() and
                    RawContactsFields.AccountType.isNull()
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
            GroupsFields.AccountName.isNull() and
                    GroupsFields.AccountType.isNull()
        }
    }

// region Redactable

// These Redactable Account extensions might prove useful for some consumers. Thus, they are public.

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

/* This might come in handy in the future. It is unused for now so it is commented out.
/**
 * Returns a [RedactableAccount] that can be used for redacting this account and keeping track of
 * its [RedactableAccount.isRedacted] state.
 *
 * This is particularly useful for loggers that can only take in [Redactable] instances.
 */
fun Account.asRedactable() = RedactableAccount(this, isRedacted = false)

/**
 * Holds/wraps an [Account] and keeps track of its [isRedacted] state.
 *
 * This is particularly useful for loggers that can only take in [Redactable] instances.
 *
 * ## Developer notes
 *
 * It might be possible to extend [Account]. However, I am not sure what kind of issues we can run
 * into by extending this platform type. Therefore, we are using composition instead =)
 */
class RedactableAccount(
    val account: Account,
    override val isRedacted: Boolean = false
) : Redactable {

    override fun toString(): String = account.toString()

    override fun redactedCopy() = RedactableAccount(
        account.redactedCopy(),
        isRedacted = true
    )
}
 */

// endregion