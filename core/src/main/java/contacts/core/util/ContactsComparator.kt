package contacts.core.util

import android.net.Uri
import contacts.core.AbstractCustomDataField
import contacts.core.AbstractDataField
import contacts.core.Ascending
import contacts.core.Descending
import contacts.core.Field
import contacts.core.Fields
import contacts.core.OrderBy
import contacts.core.entities.Contact
import contacts.core.entities.Entity
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.entities.toDbString
import java.util.Date

// Developer notes
// This was originally used to order query results. However, due to how it was used, it has been
// removed from use within the API. To prevent wasting hard work, it is now here for optional
// consumer use =)

/**0
 * Returns a [Comparator] of [Contact]s using [this] collection of [OrderBy]s.
 *
 * This is useful for sorting a collection of [Contact] instances based on a collection of
 * [AbstractDataField]. Sorting by any field in [Fields] is supported;
 *
 * ```
 * val collectionOfContacts: Collection<Contact>
 * val sortedCollectionOfContacts = collectionOfContacts.sortedWith(
 *      setOf(
 *          Fields.Contact.Options.Starred.desc(),
 *          Fields.Contact.DisplayNamePrimary.asc(),
 *          Fields.Email.Address.asc()
 *      ).contactsComparator()
 * )
 *
 * val mutableListOfContacts: MutableList<Contact>
 * mutableListOfContacts.sortWith(
 *      setOf(
 *          Fields.Contact.Options.Starred.desc(),
 *          Fields.Contact.DisplayNamePrimary.asc(),
 *          Fields.Email.Address.asc()
 *      ).contactsComparator()
 * )
 * ```
 */
@JvmOverloads
fun Collection<OrderBy<AbstractDataField>>.contactsComparator(
    customDataRegistry: CustomDataRegistry? = null
): Comparator<Contact> = ContactsComparator(customDataRegistry, toSet())

/**
 * See [contactsComparator].
 */
@JvmOverloads
fun Sequence<OrderBy<AbstractDataField>>.contactsComparator(
    customDataRegistry: CustomDataRegistry? = null
): Comparator<Contact> = ContactsComparator(customDataRegistry, toSet())

/**
 * Returns a [Comparator] of [Contact]s using [this] [OrderBy].
 *
 * This is useful for sorting a collection of [Contact] instances based on an [AbstractDataField].
 * Sorting by any field in [Fields] is supported;
 *
 * ```
 * val collectionOfContacts: Collection<Contact>
 * val sortedCollectionOfContacts = collectionOfContacts.sortedWith(
 *      Fields.Contact.DisplayNamePrimary.asc().contactsComparator()
 * )
 *
 * val mutableListOfContacts: MutableList<Contact>
 * mutableListOfContacts.sortWith(
 *      Fields.Email.Address.desc().contactsComparator()
 * )
 * ```
 */
@JvmOverloads
fun OrderBy<AbstractDataField>.contactsComparator(
    customDataRegistry: CustomDataRegistry? = null
): Comparator<Contact> = ContactsComparator(customDataRegistry, setOf(this))

/**
 * Compares [Contact] objects using one or more [OrderBy]s, which may be constructed from [Fields].
 */
private class ContactsComparator(
    private val customDataRegistry: CustomDataRegistry?,
    private val orderBys: Set<OrderBy<AbstractDataField>>
) : Comparator<Contact> {

    override fun compare(lhs: Contact, rhs: Contact): Int {
        var result = 0

        for (orderBy in orderBys) {
            result = when (orderBy) {
                is Ascending<*> -> {
                    orderBy.field.compare(customDataRegistry, lhs, rhs, orderBy.ignoreCase)
                }

                is Descending<*> -> {
                    -orderBy.field.compare(customDataRegistry, lhs, rhs, orderBy.ignoreCase)
                }
            }

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
 * Compares [Contact] values corresponding to [Field]s defined in [Fields].
 */
private fun AbstractDataField.compare(
    customDataRegistry: CustomDataRegistry?,
    lhs: Contact,
    rhs: Contact,
    ignoreCase: Boolean
): Int = when (this) {
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
    Fields.Contact.LookupKey -> lhs.lookupKey.compareTo(ignoreCase, rhs.lookupKey)
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

    Fields.Contact.LastUpdatedTimestamp ->
        lhs.lastUpdatedTimestamp.compareTo(rhs.lastUpdatedTimestamp)

    // CONTACT OPTIONS
    Fields.Contact.Options.Starred -> lhs.options?.starred.compareTo(rhs.options?.starred)
    /* Deprecated in API 29 - contains useless value for all Android versions in Play store.
    Fields.Contact.Options.TimesContacted ->
        lhs.options?.timesContacted.compareTo(rhs.options?.timesContacted)
    Fields.Contact.Options.LastTimeContacted ->
        lhs.options?.lastTimeContacted.compareTo(rhs.options?.lastTimeContacted)
     */
    Fields.Contact.Options.CustomRingtone ->
        lhs.options?.customRingtone.compareTo(rhs.options?.customRingtone)

    Fields.Contact.Options.SendToVoicemail ->
        lhs.options?.sendToVoicemail.compareTo(rhs.options?.sendToVoicemail)

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
        it.date?.toDbString()
    }

    // GROUP MEMBERSHIP intentionally excluded because they should never be combined.

    // ID (data row ID) intentionally excluded.

    // IM
    @Suppress("Deprecation") Fields.Im.Protocol ->
        @Suppress("Deprecation") lhs.ims().compareTo(ignoreCase, rhs.ims()) {
            it.protocol?.ordinal?.toString()
        }

    @Suppress("Deprecation") Fields.Im.CustomProtocol ->
        @Suppress("Deprecation") lhs.ims()
            .compareTo(ignoreCase, rhs.ims()) {
                it.customProtocol
            }

    @Suppress("Deprecation") Fields.Im.Data ->
        @Suppress("Deprecation") lhs.ims().compareTo(ignoreCase, rhs.ims()) {
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

    // PHOTO intentionally left out

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
    @Suppress("Deprecation") Fields.SipAddress.SipAddress ->
        @Suppress("Deprecation") lhs.sipAddresses()
            .compareTo(ignoreCase, rhs.sipAddresses()) {
                it.sipAddress
            }

    // WEBSITE
    Fields.Website.Url -> lhs.websites().compareTo(ignoreCase, rhs.websites()) {
        it.url
    }

    // CUSTOM
    is AbstractCustomDataField -> {
        if (customDataRegistry == null) {
            // Custom data is unhandled if registry is not provided.
            0
        } else {
            val mimeType = customDataRegistry.mimeTypeOf(this)

            val fieldMapper = customDataRegistry.entryOf(mimeType).fieldMapper

            val lhsCustomDataEntities = lhs.customDataSequenceOf(mimeType)
            val rhsCustomDataEntities = rhs.customDataSequenceOf(mimeType)

            lhsCustomDataEntities.compareTo(ignoreCase, rhsCustomDataEntities) {
                fieldMapper.valueOf(this, it)
            }
        }
    }

    else -> 0 // Treat unhandled fields as equals instead of throwing an exception.
}

/**
 * Compares [this] set of entities to the [other] set using the [comparisonKey].
 */
private inline fun <T : Entity> Sequence<T>.compareTo(
    ignoreCase: Boolean, other: Sequence<T>, crossinline comparisonKey: (T) -> String?
): Int = asSequence()
    .map { comparisonKey(it) }
    .compareTo(ignoreCase, other.map { comparisonKey(it) })

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
    } else if (this != null /* && other == null this condition is always true so lint complains */) {
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
    } else if (this != null /* && other == null this condition is always true so lint complains */) {
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
    } else if (this != null /* && other == null this condition is always true so lint complains */) {
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
    } else if (this != null /* && other == null this condition is always true so lint complains */) {
        -1
    } else {
        0
    }
}