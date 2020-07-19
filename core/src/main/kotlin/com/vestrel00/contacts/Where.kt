package com.vestrel00.contacts

import android.database.DatabaseUtils
import com.vestrel00.contacts.entities.Entity
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.util.isEmpty
import java.util.*

// TODO TEST
// Java consumers would have to access these static functions via Wherekt instead of Where.
// Using @file:JvmName("Where") will not work because of the name clash with the Where class.
// In order for Java consumers to use these via OrderBy instead of Wherekt, we could redefine
// these functions in a companion object within the Where class. However, we won't do this just
// because it creates duplicate code. Java users just need to migrate to Kotlin already...

// FIXME? Verify that LIKE or NOT LIKE operations are case-sensitive when outside of ASCII range.
// From my limited testing, it seems like it is case-insensitive even when outside of ASCII range.
// Perhaps, I was not using non-ascii characters when I was testing it out. For now, I'll just
// mention that "String comparison is case-insensitive when within ASCII range". I'll wait until the
// community sees an issue and raises it.

// TODO ESCAPE LIKE wildcards % _. Try using Query + contains to match LOVE_IS_%BLIND nickname

// region Operators

/**
 * String comparison is case-sensitive.
 */
infix fun <T : Field> T.equalTo(value: Any): Where<T> = EqualTo(this, value)

/**
 * String comparison is case-sensitive.
 */
infix fun <T : Field> T.notEqualTo(value: Any): Where<T> = NotEqualTo(this, value)

/**
 * Same as `like("$value")`.
 *
 * String comparison is case-insensitive when within ASCII range.
 */
infix fun <T : Field> T.equalToIgnoreCase(value: Any): Where<T> = like("$value")

/**
 * Same as `notLike("$value")`.
 *
 * String comparison is case-insensitive when within ASCII range.
 */
infix fun <T : Field> T.notEqualToIgnoreCase(value: Any): Where<T> = notLike("$value")

infix fun <T : Field> T.greaterThan(value: Any): Where<T> = GreaterThan(this, value)
infix fun <T : Field> T.greaterThanOrEqual(value: Any): Where<T> = GreaterThanOrEqual(this, value)

infix fun <T : Field> T.lessThan(value: Any): Where<T> = LessThan(this, value)
infix fun <T : Field> T.lessThanOrEqual(value: Any): Where<T> = LessThanOrEqual(this, value)

infix fun <T : Field> T.`in`(values: Collection<Any>): Where<T> = In(this, values.asSequence())
infix fun <T : Field> T.`in`(values: Sequence<Any>): Where<T> = In(this, values)
infix fun <T : Field> T.notIn(values: Collection<Any>): Where<T> = NotIn(this, values.asSequence())
infix fun <T : Field> T.notIn(values: Sequence<Any>): Where<T> = NotIn(this, values)

/**
 * Same as `like("$value%")`.
 *
 * String comparison is case-insensitive when within ASCII range.
 */
infix fun <T : Field> T.startsWith(value: String): Where<T> = like("$value%")

/**
 * Same as `like("%$value")`.
 *
 * String comparison is case-insensitive when within ASCII range.
 */
infix fun <T : Field> T.endsWith(value: String): Where<T> = like("%$value")

/**
 * Same as `like("%$value%")`.
 *
 * String comparison is case-insensitive when within ASCII range.
 */
infix fun <T : Field> T.contains(value: String): Where<T> = like("%$value%")

/**
 * Same as `notLike("$value%")`.
 *
 * String comparison is case-insensitive when within ASCII range.
 */
infix fun <T : Field> T.doesNotStartWith(value: String): Where<T> = notLike("$value%")

/**
 * Same as `notLike("%$value")`.
 *
 * String comparison is case-insensitive when within ASCII range.
 */
infix fun <T : Field> T.doesNotEndWith(value: String): Where<T> = notLike("%$value")

/**
 * Same as `notLike("%$value%")`.
 *
 * String comparison is case-insensitive when within ASCII range.
 */
infix fun <T : Field> T.doesNotContain(value: String): Where<T> = notLike("%$value%")

/**
 * SQLite provides two wildcards for constructing patterns; percent sign % and underscore _;
 *
 * - The percent sign % wildcard matches any sequence of zero or more characters.
 * - The underscore _ wildcard matches any single character.
 *
 * ## The percent sign % wildcard examples
 *
 * The s% pattern that uses the percent sign wildcard (%) matches any string that starts with s
 * e.g.,son and so.
 *
 * The %er pattern matches any string that ends with er like peter, clever, etc.
 *
 * And the %per% pattern matches any string that contains per such as percent and peeper.
 *
 * ## The underscore _ wildcard examples
 *
 * The h_nt pattern matches hunt, hint, etc. The __pple pattern matches topple, supple, tipple, etc.
 *
 * ## Note
 *
 * The above explanation of the % and _ wildcards are copied from
 * https://www.sqlitetutorial.net/sqlite-like/. I do not take credit for it. All credit goes to
 * that website. I just wanted the simplest documentation for this and I found that that is the
 * simplest / best. Credits to them. Don't sue me, please. If this is an issue, I'll change the
 * documentation above. AM I BEING PARANOID HERE?!?!
 *
 * String comparison is case-insensitive when within ASCII range.
 */
infix fun <T : Field> T.like(pattern: String): Where<T> = Like(this, pattern)

/**
 * Same as [like] but preceded with a NOT.
 *
 * String comparison is case-insensitive when within ASCII range.
 */
infix fun <T : Field> T.notLike(pattern: String): Where<T> = NotLike(this, pattern)

/**
 * ANDs [this] and [where]. If [where] is null, returns [this].
 */
infix fun <T : Field> Where<T>.and(where: Where<T>?): Where<T> = if (where != null) {
    And(this, where)
} else {
    this
}

/**
 * ORs [this] and [where]. If [where] is null, returns [this].
 */
infix fun <T : Field> Where<T>.or(where: Where<T>?): Where<T> = if (where != null) {
    Or(this, where)
} else {
    this
}

// region Non-infix convenience functions

/**
 * Note that functions for "isNull" or "isNullOrEmpty" are not exposed to consumers to prevent
 * making misleading queries.
 *
 * Removing a piece of existing data results in the deletion of the row in the Data table if that
 * row no longer contains any meaningful data (no meaningful non-null "datax" columns left). This is
 * the behavior of the native Android Contacts app. Therefore, querying for null fields is not
 * possible. For example, there may be no Data rows that exist where the email address is null.
 * Thus, a query to search for all contacts with null email address may return 0 contacts even if
 * there are some contacts without email addresses.
 */
fun <T : Field> T.isNotNull(): Where<T> = IsNotNull(this)

/**
 * Note that functions for "isNull" or "isNullOrEmpty" are not exposed to consumers to prevent
 * making misleading queries.
 *
 * Removing a piece of existing data results in the deletion of the row in the Data table if that
 * row no longer contains any meaningful data (no meaningful non-null "datax" columns left). This is
 * the behavior of the native Android Contacts app. Therefore, querying for null fields is not
 * possible. For example, there may be no Data rows that exist where the email address is null.
 * Thus, a query to search for all contacts with null email address may return 0 contacts even if
 * there are some contacts without email addresses.
 */
fun <T : Field> T.isNotNullOrEmpty(): Where<T> = isNotNull() and notEqualTo("")

/**
 * Keep this function internal. Do not expose to consumers. Read the docs on [isNotNull] or
 * [isNotNullOrEmpty].
 */
internal fun <T : Field> T.isNull(): Where<T> = IsNull(this)

// endregion

// endregion

// region Collections

/**
 * Transforms each item in this collection to a [Where] and combines them with the "OR" operator.
 *
 * For example;
 *
 * ```
 * val letters = listOf("a", "b", "c")
 * val whereStartsWithLetter = letters whereOr { Fields.Contact.DisplayNamePrimary startsWith it }
 * ```
 *
 * Outputs
 *
 * ```
 * // (display_name LIKE 'a%%') OR (display_name LIKE 'b%%') OR (display_name LIKE 'c%%')
 * ```
 *
 * Another, more useful example is a where starting with a number;
 *
 * ```
 * val whereStartsWithNumber = (0..9).asSequence()
 *      .whereOr { Fields.Contact.DisplayNamePrimary startsWith "$it" }
 * ```
 *
 * This may also be applied to a collection of [Field]s. For example,
 *
 * ```
 * val fields = listOf(Fields.Contact.DisplayNamePrimary, Fields.Email.Address)
 * val whereFieldsStartsWithLetter = fields whereOr { it startsWith "letter" }
 * ```
 *
 * Outputs
 *
 * ```
 * // (display_name LIKE 'letter%%') OR (data1 LIKE 'letter%%' <omitted for brevity>)
 * ```
 */
// Not inlined because of private functions and classes.
infix fun <T : Field, R : Any?> Collection<R>.whereOr(where: (R) -> Where<T>): Where<T>? =
    asSequence().joinWhere(where, "OR")

/**
 * See [whereOr].
 */
// Not inlined because of private functions and classes.
infix fun <T : Field, R : Any?> Sequence<R>.whereOr(where: (R) -> Where<T>): Where<T>? =
    joinWhere(where, "OR")

/**
 * Transforms each item in this collection to a [Where] and combines them with the "AND" operator.
 *
 * For example;
 *
 * ```
 * val letters = listOf("a", "b", "c")
 * val whereDoesNotStartWithLetter =
 *      letters whereAnd { Fields.Contact.DisplayNamePrimary doesNotStartWith it }
 * ```
 *
 * Outputs
 *
 * ```
 * (display_name NOT LIKE 'a%%') AND (display_name NOT LIKE 'b%%') AND (display_name NOT LIKE 'c%%')
 * ```
 *
 * This may also be applied to a collection of [Field]s. For example,
 *
 * ```
 * val fields = listOf(Fields.Contact.DisplayNamePrimary, Fields.Email.Address)
 * val whereFieldsDoesNotStartWithLetter = fields whereAnd { it doesNotStartWith "letter" }
 * ```
 *
 * Outputs
 *
 * ```
 * // (display_name NOT LIKE 'letter%%') AND (data1 NOT LIKE 'letter%%' <omitted for brevity>)
 */
// Not inlined because of private functions and classes.
infix fun <T : Field, R : Any?> Collection<R>.whereAnd(where: (R) -> Where<T>): Where<T>? =
    asSequence().joinWhere(where, "AND")

/**
 * See [whereAnd].
 */
// Not inlined because of private functions and classes.
infix fun <T : Field, R : Any?> Sequence<R>.whereAnd(where: (R) -> Where<T>): Where<T>? =
    joinWhere(where, "AND")

/**
 * See [whereOr].
 */
infix fun <T : Field> FieldSet<T>.whereOr(where: (T) -> Where<T>): Where<T>? = all.whereOr(where)

/**
 * See [whereAnd].
 */
infix fun <T : Field> FieldSet<T>.whereAnd(where: (T) -> Where<T>): Where<T>? = all.whereAnd(where)

// Note that the above functions are not inlined because it requires this private fun to be public.
private fun <T : Field, R : Any?> Sequence<R>.joinWhere(
    where: (R) -> Where<T>,
    separator: String
): Where<T>? {
    if (isEmpty()) {
        return null
    }

    val whereString = joinToString(" $separator ") { "(${where(it)})" }
    return JoinedWhere(whereString)
}

// endregion

// region Conversions

private class ContactsTableWhere(whereString: String) : Where<ContactsField>(whereString)

/**
 * Converts [this] Data where clause to a where clause that is usable for the Contacts table.
 *
 * More specifically, this translates the following column names to work with the Contacts table;
 *
 * - RawContacts.CONTACT_ID -> Contacts._ID
 * - Data.CONTACT_ID -> Contacts._ID
 *
 * This does no translate anything else. So any fields used that does not exist in the Contacts
 * table will remain.
 */
internal fun <T : AbstractDataField> Where<T>.inContactsTable(): Where<ContactsField> =
    ContactsTableWhere(
        toString()
            .replace(RawContactsFields.ContactId.columnName, ContactsFields.Id.columnName)
            // Technically, RawContactsFields.ContactId and Fields.Contact.Id have the same columnName.
            // For the sake of OCD, I'm performing this redundant replacement =) SUE ME!
            .replace(Fields.Contact.Id.columnName, ContactsFields.Id.columnName)
    )

private class RawContactsTableWhere(whereString: String) : Where<RawContactsField>(whereString)

/**
 * Converts [this] Data where clause to a where clause that is usable for the RawContacts table.
 *
 * More specifically, this translates the following column names to work with the RawContacts table;
 *
 * - Data.RAW_CONTACT_ID -> RawContacts._ID
 *
 * This does no translate anything else. So any fields used that does not exist in the RawContacts
 * table will remain.
 */
internal fun <T : AbstractDataField> Where<T>.inRawContactsTable(): Where<RawContactsField> =
    RawContactsTableWhere(
        toString().replace(Fields.RawContact.Id.columnName, RawContactsFields.Id.columnName)
    )

// endregion

// region Where

/**
 * Each where expression is paired with its mimetype because the contacts Data table uses
 * generic column names (e.g. data1, data2, etc) using the column 'mimetype' to distinguish
 * the type of data in that generic column.
 *
 * For example, querying for contacts with name LIKE 'john' AND address LIKE 'colorado';
 *
 * ```
 * WHERE (data1 = 'john' AND mimetype = 'vnd.android.cursor.item/name')
 *   AND (data1 = 'colorado' AND mimetype = 'vnd.android.cursor.item/postal-address_v2')
 * ```
 *
 * This is important because if the mimetypes are not paired with the query;
 *
 * ```
 * WHERE (data1 = 'johnson' AND data1 = 'colorado')
 * ```
 *
 * The above will never match any row because 'johnson' = 'colorado' is never true.
 */
private fun where(field: Field, operator: String, value: Any?): String {
    var where = "${field.columnName} $operator ${value.toSqlString()}"
    if (field is CommonDataField && field.mimeType.value.isNotBlank()) {
        where += " AND ${Fields.MimeType.columnName} = '${field.mimeType.value}'"
    }
    return where
}

private fun <T : Field> where(lhs: Where<T>, operator: String, rhs: Where<T>): String =
    "($lhs) $operator ($rhs)"

/**
 * The WHERE clause of a database query made up of a specific type of [Field] ([T]).
 *
 * ## Developer notes
 *
 * The type [T] is not exactly "used" in this class itself. Rather, it is used for adding type
 * restrictions when constructing instances at compile time. For example, this allows us to create a
 * function that takes in a Where of GroupsField. The caller of that function can then only provide
 * a Where composed of one or more GroupsField.
 */
sealed class Where<out T : Field>(private val whereString: String) {
    override fun toString(): String = whereString
}

// endregion

// region Where classes

private class And<T : Field>(lhs: Where<T>, rhs: Where<T>) : Where<T>(where(lhs, "AND", rhs))
private class Or<T : Field>(lhs: Where<T>, rhs: Where<T>) : Where<T>(where(lhs, "OR", rhs))

private class EqualTo<T : Field>(field: Field, value: Any) : Where<T>(where(field, "=", value))
private class NotEqualTo<T : Field>(field: Field, value: Any) : Where<T>(where(field, "!=", value))

private class GreaterThan<T : Field>(field: Field, value: Any) : Where<T>(where(field, ">", value))
private class GreaterThanOrEqual<T : Field>(field: Field, value: Any) :
    Where<T>(where(field, ">=", value))

private class LessThan<T : Field>(field: Field, value: Any) : Where<T>(where(field, "<", value))
private class LessThanOrEqual<T : Field>(field: Field, value: Any) :
    Where<T>(where(field, "<=", value))

private class IsNull<T : Field>(field: Field) : Where<T>(where(field, "IS", null))
private class IsNotNull<T : Field>(field: Field) : Where<T>(where(field, "IS NOT", null))

private class In<T : Field>(field: Field, values: Sequence<Any>) :
    Where<T>(where(field, "IN", values))

private class NotIn<T : Field>(field: Field, values: Sequence<Any>) :
    Where<T>(where(field, "NOT IN", values))

private class Like<T : Field>(field: Field, value: Any) : Where<T>(where(field, "LIKE", value))
private class NotLike<T : Field>(field: Field, value: Any) :
    Where<T>(where(field, "NOT LIKE", value))

private class JoinedWhere<T : Field>(whereString: String) : Where<T>(whereString)

// endregion

private fun Any?.toSqlString(): String = when (this) {
    null -> "NULL"
    is Boolean -> if (this) "1" else "0"
    is String -> DatabaseUtils.sqlEscapeString(this)
    is Array<*> -> this.asSequence().toSqlString()
    is Collection<*> -> this.asSequence().toSqlString()
    is Sequence<*> -> this.map { it?.toSqlString() }
        .joinToString(separator = ", ", prefix = "(", postfix = ")")
    is Entity.Type -> value.toSqlString()
    is Date -> time.toSqlString()
    is MimeType -> value.toSqlString()
    else -> this.toString().toSqlString()
}
