package com.vestrel00.contacts

import android.net.Uri
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.entities.Entity
import com.vestrel00.contacts.util.*
import java.util.*

// Java consumers would have to access these static functions via OrderBykt instead of OrderBy.
// Using @file:JvmName("OrderBy") will not work because of the name clash with the OrderBy class.
// In order for Java consumers to use these via OrderBy instead of OrderBykt, we could redefine
// these functions in a companion object within the OrderBy class. However, we won't do this just
// because it creates duplicate code. Java users just need to migrate to Kotlin already...

private const val DEFAULT_IGNORE_CASE = true

/**
 * Default [ignoreCase] is true.
 */
@JvmOverloads
fun AbstractField.asc(ignoreCase: Boolean = DEFAULT_IGNORE_CASE): OrderBy =
    Ascending(this, ignoreCase)

/**
 * Default [ignoreCase] is true.
 */
@JvmOverloads
fun AbstractField.desc(ignoreCase: Boolean = DEFAULT_IGNORE_CASE): OrderBy =
    Descending(this, ignoreCase)

/**
 * Default [ignoreCase] is true.
 */
@JvmOverloads
fun Collection<AbstractField>.asc(ignoreCase: Boolean = DEFAULT_IGNORE_CASE): Collection<OrderBy> =
    asSequence().map { it.asc(ignoreCase) }.toSet()

/**
 * Default [ignoreCase] is true.
 */
@JvmOverloads
fun Collection<AbstractField>.desc(ignoreCase: Boolean = DEFAULT_IGNORE_CASE): Collection<OrderBy> =
    asSequence().map { it.desc(ignoreCase) }.toSet()

/**
 * Serves two different purposes;
 *
 * 1. As a [Comparator] of [Contact]s.
 * 2. As the ORDER BY value string in Contacts Provider queries.
 */
sealed class OrderBy(
    internal val field: AbstractField,
    private val order: String,
    protected val ignoreCase: Boolean
) : Comparator<Contact> {

    override fun toString(): String = if (ignoreCase) {
        "${field.columnName} COLLATE NOCASE $order"
    } else {
        "${field.columnName} $order"
    }
}

private class Ascending(field: AbstractField, ignoreCase: Boolean) :
    OrderBy(field, "ASC", ignoreCase) {

    override fun compare(lhs: Contact, rhs: Contact): Int = field.compare(lhs, rhs, ignoreCase)
}

private class Descending(field: AbstractField, ignoreCase: Boolean) :
    OrderBy(field, "DESC", ignoreCase) {

    override fun compare(lhs: Contact, rhs: Contact): Int = -field.compare(lhs, rhs, ignoreCase)
}

/**
 * A set of one or more [OrderBy].
 */
internal class CompoundOrderBy(private val orderBys: Set<OrderBy>) : Comparator<Contact> {

    fun allFieldsAreContainedIn(fieldSet: Set<AbstractField>): Boolean {
        val orderByFieldSet = orderBys.asSequence().map { it.field }.toSet()
        return fieldSet.containsAll(orderByFieldSet)
    }

    override fun toString(): String = orderBys.joinToString(", ")

    override fun compare(lhs: Contact, rhs: Contact): Int {
        var result = 0

        for (orderBy in orderBys) {
            result = orderBy.compare(lhs, rhs)

            if (result == 0) {
                // lhs and rhs are equal given this orderBy.
                // Use subsequent orderBys, if any, to determine if any inequality exist.
                continue
            } else {
                // lhs and rhs are not equal given this orderBy.
                // No need to check for inequality using subsequent orderBys
                break
            }
        }

        return result
    }
}

/**
 * Compares [Contact] values corresponding to [AbstractField]s defined in [Fields].
 */
private fun AbstractField.compare(lhs: Contact, rhs: Contact, ignoreCase: Boolean): Int {

    return when (this) {
        // ADDRESS
        Fields.Address.Type -> lhs.addresses().compareTo(ignoreCase, rhs.addresses()) {
            it.type?.ordinal?.toString()
        }
        Fields.Address.Label -> lhs.addresses().compareTo(ignoreCase, rhs.addresses()) {
            it.label
        }
        Fields.Address.FormattedAddress -> lhs.addresses().compareTo(ignoreCase, rhs.addresses()) {
            it.formattedAddress
        }
        Fields.Address.Street -> lhs.addresses().compareTo(ignoreCase, rhs.addresses()) {
            it.street
        }
        Fields.Address.PoBox -> lhs.addresses().compareTo(ignoreCase, rhs.addresses()) {
            it.poBox
        }
        Fields.Address.Neighborhood -> lhs.addresses().compareTo(ignoreCase, rhs.addresses()) {
            it.neighborhood
        }
        Fields.Address.City -> lhs.addresses().compareTo(ignoreCase, rhs.addresses()) {
            it.city
        }
        Fields.Address.Region -> lhs.addresses().compareTo(ignoreCase, rhs.addresses()) {
            it.region
        }
        Fields.Address.PostCode -> lhs.addresses().compareTo(ignoreCase, rhs.addresses()) {
            it.postcode
        }
        Fields.Address.Country -> lhs.addresses().compareTo(ignoreCase, rhs.addresses()) {
            it.country
        }

        // CONTACT
        Fields.Contact.Id -> lhs.id.compareTo(rhs.id)
        Fields.Contact.DisplayNamePrimary -> {
            lhs.displayNamePrimary.compareTo(
                ignoreCase, rhs.displayNamePrimary
            )
        }
        Fields.Contact.DisplayNameAlt -> {
            lhs.displayNameAlt.compareTo(
                ignoreCase, rhs.displayNameAlt
            )
        }
        // PhotoUri and PhotoThumbnailUri are excluded.
        Fields.Contact.LastUpdatedTimestamp ->
            lhs.lastUpdatedTimestamp.compareTo(rhs.lastUpdatedTimestamp)

        // EMAIL
        Fields.Email.Type -> lhs.emails().compareTo(ignoreCase, rhs.emails()) {
            it.type?.ordinal?.toString()
        }
        Fields.Email.Label -> lhs.emails().compareTo(ignoreCase, rhs.emails()) {
            it.label
        }
        Fields.Email.Address -> lhs.emails().compareTo(ignoreCase, rhs.emails()) {
            it.address
        }

        // EVENT
        Fields.Event.Type -> lhs.events().compareTo(ignoreCase, rhs.events()) {
            it.type?.ordinal?.toString()
        }
        Fields.Event.Label -> lhs.events().compareTo(ignoreCase, rhs.events()) {
            it.label
        }
        Fields.Event.Date -> lhs.events().compareTo(ignoreCase, rhs.events()) {
            it.date?.time.toString()
        }

        // GROUP MEMBERSHIP intentionally excluded because they should never be combined.

        // ID (data row ID) intentionally excluded.

        // IM
        Fields.Im.Protocol -> lhs.ims().compareTo(ignoreCase, rhs.ims()) {
            it.protocol?.ordinal?.toString()
        }
        Fields.Im.CustomProtocol -> lhs.ims().compareTo(ignoreCase, rhs.ims()) {
            it.customProtocol
        }
        Fields.Im.Data -> lhs.ims().compareTo(ignoreCase, rhs.ims()) {
            it.data
        }

        // Primary and super primary intentionally excluded.

        // MIMETYPE intentionally excluded.

        // NAME
        Fields.Name.DisplayName -> lhs.names().compareTo(ignoreCase, rhs.names()) {
            it.displayName
        }
        Fields.Name.GivenName -> lhs.names().compareTo(ignoreCase, rhs.names()) {
            it.givenName
        }
        Fields.Name.MiddleName -> lhs.names().compareTo(ignoreCase, rhs.names()) {
            it.middleName
        }
        Fields.Name.FamilyName -> lhs.names().compareTo(ignoreCase, rhs.names()) {
            it.familyName
        }
        Fields.Name.Prefix -> lhs.names().compareTo(ignoreCase, rhs.names()) {
            it.prefix
        }
        Fields.Name.Suffix -> lhs.names().compareTo(ignoreCase, rhs.names()) {
            it.suffix
        }
        Fields.Name.PhoneticGivenName -> lhs.names().compareTo(ignoreCase, rhs.names()) {
            it.phoneticGivenName
        }
        Fields.Name.PhoneticMiddleName -> lhs.names().compareTo(ignoreCase, rhs.names()) {
            it.phoneticMiddleName
        }
        Fields.Name.PhoneticFamilyName -> lhs.names().compareTo(ignoreCase, rhs.names()) {
            it.phoneticFamilyName
        }

        // NICKNAME
        Fields.Nickname.Name -> lhs.nicknames().compareTo(ignoreCase, rhs.nicknames()) {
            it.name
        }

        // NOTE
        Fields.Note.Note -> lhs.notes().compareTo(ignoreCase, rhs.notes()) {
            it.note
        }

        // OPTIONS
        Fields.Options.Starred -> lhs.options?.starred.compareTo(rhs.options?.starred)
        Fields.Options.TimesContacted ->
            lhs.options?.timesContacted.compareTo(rhs.options?.timesContacted)
        Fields.Options.LastTimeContacted ->
            lhs.options?.lastTimeContacted.compareTo(rhs.options?.lastTimeContacted)
        Fields.Options.CustomRingtone ->
            lhs.options?.customRingtone.compareTo(rhs.options?.customRingtone)
        Fields.Options.SendToVoicemail ->
            lhs.options?.sendToVoicemail.compareTo(rhs.options?.sendToVoicemail)

        // ORGANIZATION
        Fields.Organization.Company -> lhs.organizations().compareTo(
            ignoreCase,
            rhs.organizations()
        ) {
            it.company
        }
        Fields.Organization.Title -> lhs.organizations().compareTo(
            ignoreCase,
            rhs.organizations()
        ) {
            it.title
        }
        Fields.Organization.Department -> lhs.organizations().compareTo(
            ignoreCase,
            rhs.organizations()
        ) {
            it.department
        }
        Fields.Organization.JobDescription -> lhs.organizations().compareTo(
            ignoreCase,
            rhs.organizations()
        ) {
            it.jobDescription
        }
        Fields.Organization.OfficeLocation -> lhs.organizations().compareTo(
            ignoreCase,
            rhs.organizations()
        ) {
            it.officeLocation
        }
        Fields.Organization.Symbol -> lhs.organizations().compareTo(
            ignoreCase,
            rhs.organizations()
        ) {
            it.symbol
        }
        Fields.Organization.PhoneticName -> lhs.organizations().compareTo(
            ignoreCase,
            rhs.organizations()
        ) {
            it.phoneticName
        }

        // PHONE
        Fields.Phone.Type -> lhs.phones().compareTo(ignoreCase, rhs.phones()) {
            it.type?.ordinal?.toString()
        }
        Fields.Phone.Label -> lhs.phones().compareTo(ignoreCase, rhs.phones()) {
            it.label
        }
        Fields.Phone.Number -> lhs.phones().compareTo(ignoreCase, rhs.phones()) {
            it.number
        }
        Fields.Phone.NormalizedNumber -> lhs.phones().compareTo(ignoreCase, rhs.phones()) {
            it.normalizedNumber
        }

        // RAW CONTACT intentionally excluded.

        // RELATION
        Fields.Relation.Type -> lhs.relations().compareTo(ignoreCase, rhs.relations()) {
            it.type?.ordinal?.toString()
        }
        Fields.Relation.Label -> lhs.relations().compareTo(ignoreCase, rhs.relations()) {
            it.label
        }
        Fields.Relation.Name -> lhs.relations().compareTo(ignoreCase, rhs.relations()) {
            it.name
        }

        // SIP ADDRESS
        Fields.SipAddress.SipAddress -> lhs.sipAddresses()
            .compareTo(ignoreCase, rhs.sipAddresses()) {
                it.sipAddress
            }

        // WEBSITE
        Fields.Website.Url -> lhs.websites().compareTo(ignoreCase, rhs.websites()) {
            it.url
        }

        else -> 0 // Treat unhandled fields as equals instead of throwing an exception.
    }
}

/**
 * Compares [this] set of entities to the [other] set using the [comparisonKey].
 */
private inline fun <T : Entity> Sequence<T>.compareTo(
    ignoreCase: Boolean, other: Sequence<T>, crossinline comparisonKey: (T) -> String?
): Int = asSequence()
    .map { comparisonKey(it) }
    .compareTo(ignoreCase, other.asSequence().map { comparisonKey(it) })

/**
 * Zips [this] and [other] and iterates through each entry pair.
 *
 * Returns a positive integer if [this] contains a string that is **less** than the [other] string.
 * Returns a negative integer if [this] contains a string that is **greater** than the [other]
 * string. Otherwise, returns 0 if [this] sequence contains the exact same elements in order as
 * [other].
 *
 * If both sequences contain the exact same elements in order but differ in the number of items,
 * then a negative integer is returned if [this] has less items than [other]. Returns a
 * positive integer otherwise.
 */
private fun Sequence<String?>.compareTo(ignoreCase: Boolean, other: Sequence<String?>): Int {
    for ((lhs, rhs) in this.zip(other)) {
        val result = lhs.compareTo(ignoreCase, rhs)

        if (result == 0) {
            // lhs and rhs are equal.
            // Compare subsequent items, if any, to determine if any inequality exist.
            continue
        } else {
            // lhs and rhs are not equal.
            // No need to check further for inequality.
            return result
        }
    }

    // The call to count may be expensive as it traverses the sequence, invoking all of the
    // intermediate functions.
    return other.count() - this.count()
}

/**
 * Compares [this] nullable string to the [other] nullable string.
 *
 * If both [this] and [other] are not null, then a comparison is done on both. Otherwise, this
 * returns a positive integer if [this] is null and [other] is not null. Returns a negative integer
 * if [this] is not null and [other] is null. Returns 0 if both [this] and [other] are null.
 */
private fun String?.compareTo(ignoreCase: Boolean, other: String?): Int {
    return if (this != null && other != null) {
        compareTo(other, ignoreCase)
    } else if (this == null && other != null) {
        1
    } else if (this != null && other == null) {
        -1
    } else {
        0
    }
}

/**
 * Compares [this] nullable date to the [other] nullable date.
 *
 * If both [this] and [other] are not null, then a comparison is done on both. Otherwise, this
 * returns a positive integer if [this] is null and [other] is not null. Returns a negative integer
 * if [this] is not null and [other] is null. Returns 0 if both [this] and [other] are null.
 */
private fun Date?.compareTo(other: Date?): Int {
    return if (this != null && other != null) {
        compareTo(other)
    } else if (this == null && other != null) {
        1
    } else if (this != null && other == null) {
        -1
    } else {
        0
    }
}

/**
 * Compares [this] nullable uri to the [other] nullable uri.
 *
 * If both [this] and [other] are not null, then a comparison is done on both. Otherwise, this
 * returns a positive integer if [this] is null and [other] is not null. Returns a negative integer
 * if [this] is not null and [other] is null. Returns 0 if both [this] and [other] are null.
 */
private fun Uri?.compareTo(other: Uri?): Int {
    return if (this != null && other != null) {
        compareTo(other)
    } else if (this == null && other != null) {
        1
    } else if (this != null && other == null) {
        -1
    } else {
        0
    }
}

/**
 * Compares [this] nullable boolean to the [other] nullable integer.
 *
 * If both [this] and [other] are not null, then a comparison is done on both. Otherwise, this
 * returns a positive integer if [this] is null and [other] is not null. Returns a negative integer
 * if [this] is not null and [other] is null. Returns 0 if both [this] and [other] are null.
 */
private fun Boolean?.compareTo(other: Boolean?): Int {
    return if (this != null && other != null) {
        compareTo(other)
    } else if (this == null && other != null) {
        1
    } else if (this != null && other == null) {
        -1
    } else {
        0
    }
}

/**
 * Compares [this] nullable integer to the [other] nullable integer.
 *
 * If both [this] and [other] are not null, then a comparison is done on both. Otherwise, this
 * returns a positive integer if [this] is null and [other] is not null. Returns a negative integer
 * if [this] is not null and [other] is null. Returns 0 if both [this] and [other] are null.
 */
private fun Int?.compareTo(other: Int?): Int {
    return if (this != null && other != null) {
        compareTo(other)
    } else if (this == null && other != null) {
        1
    } else if (this != null && other == null) {
        -1
    } else {
        0
    }
}

/**
 * Compares [this] nullable long to the [other] nullable long.
 *
 * If both [this] and [other] are not null, then a comparison is done on both. Otherwise, this
 * returns a positive integer if [this] is null and [other] is not null. Returns a negative integer
 * if [this] is not null and [other] is null. Returns 0 if both [this] and [other] are null.
 */
private fun Long?.compareTo(other: Long?): Int {
    return if (this != null && other != null) {
        compareTo(other)
    } else if (this == null && other != null) {
        1
    } else if (this != null && other == null) {
        -1
    } else {
        0
    }
}