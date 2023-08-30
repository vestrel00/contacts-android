@file:Suppress("PropertyName")

package contacts.core

import android.annotation.TargetApi
import android.os.Build
import android.provider.BlockedNumberContract
import android.provider.ContactsContract.*
import android.provider.ContactsContract.Contacts
import contacts.core.AbstractCustomDataField.ColumnName
import contacts.core.ContactsFields.all
import contacts.core.Fields.all
import contacts.core.Fields.forMatching
import contacts.core.GroupsFields.all
import contacts.core.RequiredDataFields.all
import contacts.core.RequiredDataFields.forMatching
import contacts.core.entities.MimeType

// A note about the lazy usage here. I wrapped everything that is more memory or CPU intensive to
// to be lazily initialized. I did a rough measurement of how much time is saved during app cold
// start by using lazy; https://github.com/vestrel00/contacts-android/issues/286#issuecomment-1563335175
// It's roughly ~10ms for mid-range Android phones and ~5ms for high-range phones. For low range
// phones, it could be ~20-30ms.
//
// Variables annotated with @JvmField cannot be lazy. The @JvmField annotation is required so that
// Java consumers can access the member variables with the same ease as Kotlin folks. This should
// be trivial because such variables just initialize simple class instances.
//
// Thus, the current setup should not noticeably affect cold startup times.

// region Field Interfaces

/**
 * Represents a database column and the data stored in each row.
 *
 * ## Side note about the name "Field"
 *
 * Think of a field like a cell in a
 * spreadsheet, which is the intersection of a row and column. Or just think of this as a "column"
 * if it makes it easier for you. Not sure if there is a uniform definition of a field but this
 * works well for this library.
 * https://www.google.com/search?q=database+field+vs+column
 *
 * ## Developer notes
 *
 * All concrete implementations of this must be data classes or implement equals and hashCode.
 */
// This is a sealed class instead of a sealed interface because we want to keep some things internal
// and also initialize some properties.
sealed class Field {

    /**
     * The column name in the database that this field represents.
     */
    internal abstract val columnName: String

    /**
     * True if this field is required for queries.
     *
     * This is not (yet) used for inserts, updates, or deletes.
     */
    internal open val required: Boolean = false

    // Force concrete implementations to implements equals and hashCode, which can be manually
    // written or provided by being a data class.
    abstract override fun equals(other: Any?): Boolean

    abstract override fun hashCode(): Int
}

/**
 * Holds a set of [Field]s.
 */
sealed class FieldSet<out T : Field> {

    /**
     * All of the fields defined in this set, useful for specifying includes.
     */
    abstract val all: Set<T>
}

// endregion

// region Data Table Fields

/**
 * Returns a set of [T] that contains the intersection of this [FieldSet] with the given [fields].
 */
@Suppress("UNCHECKED_CAST")
internal fun <T : AbstractDataField> FieldSet<T>.intersect(fields: Set<AbstractDataField>): Set<T> =
    all.intersect(fields) as Set<T>

sealed class AbstractDataField : Field()

sealed class AbstractDataFieldSet<out T : AbstractDataField> : FieldSet<T>() {

    /**
     * All of the fields defined in this set that are safe for matching in queries.
     *
     * This is useful in creating where clauses that matches text that the user is typing in a
     * search field. For example, if the user is typing in some numbers trying to find a phone
     * number, this will NOT match numbers that represent booleans or types of data such as
     * [PhoneFields.Type], [EmailFields.Type], or [DataContactsOptionsFields.Starred].
     *
     * This field set is typically used with [Query] and [whereAnd] or [whereOr]. For example, the
     * where clause `Fields.forMatching whereOr { it contains searchText }` matches data contained
     * in any field that is included in this field set that contains the searchText.
     *
     * These are the same fields used by the Contacts Provider internally when performing a general
     * match; https://developer.android.com/training/contacts-provider/retrieve-names#GeneralMatch.
     *
     * Instead of using this with [Query], you may want to use [BroadQuery] instead for simplicity
     * and breadth.
     *
     * The following fields are being used by the ContactProvider general matching algorithm;
     *
     * - [AddressFields.FormattedAddress].
     * - [ImFields.Data].
     * - [NameFields.DisplayName], PhoneticGivenName, PhoneticMiddleName, and PhoneticFamilyName.
     * - [NicknameFields.Name]
     * - [NoteFields.Note]
     * - [OrganizationFields] (all of it)
     * - [PhoneFields.Number]
     */
    abstract val forMatching: Set<T>
}

data class GenericDataField internal constructor(override val columnName: String) :
    AbstractDataField() {
    override val required: Boolean = true
}

/**
 * Contains all fields / columns that are accessible via the Data table with joins from the
 * RawContacts and Contacts tables (ContactsContract.DataColumnsWithJoins).
 *
 * For a shorter name, use [F] (useful for Kotlin-ers).
 *
 * The real (more technically correct) name of this object is [DataFields]. The name "Fields" is
 * used here so that Java consumers may be able to access these most frequently used fields using a
 * shorter name than "DataFields".
 *
 * ## Developer Notes
 *
 * All fields declared within this object such as [Fields.Address] are classes, not objects, so that
 * Java consumers may be able to access the fields within them. For example, if [AddressFields] is
 * an object instead of a class, then [AddressFields.City] (and all other fields) will not be
 * visible to Java consumers via this object.
 */
object Fields : AbstractDataFieldSet<AbstractDataField>() {

    @JvmField
    val Address = AddressFields()

    @JvmField
    val Contact = DataContactsFields()

    @JvmField
    val DataId = GenericDataField(Data._ID)

    @JvmField
    val Email = EmailFields()

    @JvmField
    val Event = EventFields()

    @JvmField
    val GroupMembership = GroupMembershipFields()

    @JvmField
    val Im = ImFields()

    @JvmField
    val IsPrimary = GenericDataField(Data.IS_PRIMARY)

    @JvmField
    val IsSuperPrimary = GenericDataField(Data.IS_SUPER_PRIMARY)

    internal val MimeType = GenericDataField(Data.MIMETYPE)

    @JvmField
    val Name = NameFields()

    @JvmField
    val Nickname = NicknameFields()

    @JvmField
    val Note = NoteFields()

    @JvmField
    val Organization = OrganizationFields()

    @JvmField
    val Phone = PhoneFields()

    @JvmField
    val Photo = PhotoFields()

    @JvmField
    val RawContact = DataRawContactsFields()

    @JvmField
    val Relation = RelationFields()

    @JvmField
    val SipAddress = SipAddressFields()

    @JvmField
    val Website = WebsiteFields()

    /**
     * See [RequiredDataFields].
     */
    @JvmField
    val Required = RequiredDataFields

    /**
     * See [PrimaryPhotoHolderFields].
     */
    @JvmField
    val PrimaryPhotoHolder = PrimaryPhotoHolderFields

    override val all by lazy {
        mutableSetOf<AbstractDataField>().apply {
            addAll(Address.all)
            addAll(Contact.all)
            add(DataId)
            addAll(Email.all)
            addAll(Event.all)
            addAll(GroupMembership.all)
            addAll(Im.all)
            add(IsPrimary)
            add(IsSuperPrimary)
            add(MimeType)
            addAll(Name.all)
            addAll(Nickname.all)
            addAll(Note.all)
            addAll(Organization.all)
            addAll(Phone.all)

            // The PhotoThumbnail is intentionally excluded in all because it could lead to
            // unintended inclusion of relatively large BLOB data. PhotoThumbnail must be explicitly
            // included in queries to ensure intentionality. The PhotoFileId should be enough for
            // queries to return photo mimetype rows.
            addAll(Photo.all.minus(Photo.PhotoThumbnail))

            addAll(RawContact.all)
            addAll(Relation.all)
            addAll(SipAddress.all)
            addAll(Website.all)
        }
    }

    override val forMatching by lazy {
        mutableSetOf<AbstractDataField>().apply {
            addAll(Address.forMatching)
            addAll(Contact.forMatching)
            // add(DataId) not included
            addAll(Email.forMatching)
            addAll(Event.forMatching)
            addAll(GroupMembership.forMatching)
            addAll(Im.forMatching)
            // add(IsPrimary) not included
            // add(IsSuperPrimary) not included
            // add(MimeType) not included
            addAll(Name.forMatching)
            addAll(Nickname.forMatching)
            addAll(Note.forMatching)
            addAll(Organization.forMatching)
            addAll(Phone.forMatching)
            addAll(Photo.forMatching)
            addAll(RawContact.forMatching)
            addAll(Relation.forMatching)
            addAll(SipAddress.forMatching)
            addAll(Website.forMatching)
        }.toSet() // ensure that this is not modifiable at runtime
    }

    /**
     * Same as [all], but as a function. This makes it visible to Java consumers when accessing this
     * using the object reference directly.
     */
    @JvmStatic
    fun all() = all

    /**
     * Same as [forMatching], but as a function. This makes it visible to Java consumers when
     * accessing this using the object reference directly.
     */
    @JvmStatic
    fun forMatching() = forMatching
}

/**
 * The real name of [Fields].
 *
 * Java consumers are unable to access the members of the instance of this even with @JvmField.
 * Therefore, this is for Kotlin-ers only =)
 */
// @JvmField
val DataFields = Fields

/**
 * Shorthand for [Fields].
 *
 * Java consumers are unable to access the members of the instance of this even with @JvmField.
 * Therefore, this is for Kotlin-ers only =)
 */
// @JvmField
val F = Fields

// region Composite Fields

/**
 * The set of data fields that are required to properly resolve
 * [contacts.core.entities.ExistingContactEntity.primaryPhotoHolder].
 */
object PrimaryPhotoHolderFields : AbstractDataFieldSet<AbstractDataField>() {

    override val all by lazy {
        setOf(
            Fields.Contact.PhotoFileId,
            Fields.Photo.PhotoFileId
        )
    }

    override val forMatching = emptySet<AbstractDataField>()

    /**
     * Same as [all], but as a function. This makes it visible to Java consumers when accessing this
     * using the object reference directly.
     */
    @JvmStatic
    fun all() = all

    /**
     * Same as [forMatching], but as a function. This makes it visible to Java consumers when
     * accessing this using the object reference directly
     */
    @JvmStatic
    fun forMatching() = forMatching
}

/**
 * The set of data fields that are required, internally by this library, to be included in all
 * query results.
 */
object RequiredDataFields : AbstractDataFieldSet<AbstractDataField>() {

    override val all by lazy {
        setOf(
            Fields.DataId,
            Fields.RawContact.Id,
            Fields.Contact.Id,
            // Intentionally not including the Contact lookup key because the APIs in this library
            // does not depend on it to function at a basic level.
            // As for the RawContact sourceId, it is simply not available in the joined data table.
            Fields.MimeType,
            Fields.IsPrimary,
            Fields.IsSuperPrimary
        )
    }

    override val forMatching = emptySet<AbstractDataField>()

    /**
     * Same as [all], but as a function. This makes it visible to Java consumers when accessing this
     * using the object reference directly.
     */
    @JvmStatic
    fun all() = all

    /**
     * Same as [forMatching], but as a function. This makes it visible to Java consumers when
     * accessing this using the object reference directly
     */
    @JvmStatic
    fun forMatching() = forMatching
}

// endregion

// region Joined Data Fields

data class DataContactsField internal constructor(
    override val columnName: String,
    override val required: Boolean = false
) : AbstractDataField()

class DataContactsFields internal constructor() : AbstractDataFieldSet<DataContactsField>() {

    // The Data.CONTACT_ID, which is not the same as the column name Contacts._ID. This is only
    // meant to be used for Data table operations.
    @JvmField
    val Id = DataContactsField(Data.CONTACT_ID, required = true)

    @JvmField
    val LookupKey = DataContactsField(Data.LOOKUP_KEY)

    @JvmField
    val DisplayNamePrimary = DataContactsField(Data.DISPLAY_NAME_PRIMARY)

    @JvmField
    val DisplayNameAlt = DataContactsField(Data.DISPLAY_NAME_ALTERNATIVE)

    @JvmField
    val LastUpdatedTimestamp = DataContactsField(Data.CONTACT_LAST_UPDATED_TIMESTAMP)

    @JvmField
    val Options = DataContactsOptionsFields()

    @JvmField
    val PhotoUri = DataContactsField(Contacts.PHOTO_URI)

    @JvmField
    val PhotoThumbnailUri = DataContactsField(Contacts.PHOTO_THUMBNAIL_URI)

    @JvmField
    val PhotoFileId = DataContactsField(Contacts.PHOTO_FILE_ID)

    @JvmField
    val HasPhoneNumber = DataContactsField(Contacts.HAS_PHONE_NUMBER)

    override val all by lazy {
        mutableSetOf(
            Id, LookupKey, DisplayNamePrimary, DisplayNameAlt, LastUpdatedTimestamp,
            PhotoUri, PhotoThumbnailUri, PhotoFileId, HasPhoneNumber
        ).apply {
            addAll(Options.all)
        }.toSet() // ensure that this is not modifiable at runtime
    }

    // The GeneralMatch algorithm of the Contacts Provider does not match any of these fields.
    override val forMatching = emptySet<DataContactsField>()
}

// Contains the same underlying column names as RawContactsOptionsFields and ContactsOptionsFields
// but with a different Field type.
class DataContactsOptionsFields internal constructor() : AbstractDataFieldSet<DataContactsField>() {

    internal val Id = DataContactsField(Data._ID, required = true)

    @JvmField
    val Starred = DataContactsField(Data.STARRED)

    /* Deprecated in API 29 - contains useless value for all Android versions from the Play store.
    @JvmField
    val TimesContacted = DataContactsField(Data.TIMES_CONTACTED)

    @JvmField
    val LastTimeContacted = DataContactsField(Data.LAST_TIME_CONTACTED)
     */

    @JvmField
    val CustomRingtone = DataContactsField(Data.CUSTOM_RINGTONE)

    @JvmField
    val SendToVoicemail = DataContactsField(Data.SEND_TO_VOICEMAIL)

    override val all by lazy {
        setOf(
            Id, Starred, CustomRingtone, SendToVoicemail
        )
    }

    // The GeneralMatch algorithm of the Contacts Provider does not match any of these fields.
    override val forMatching = emptySet<DataContactsField>()
}

data class DataRawContactsField internal constructor(
    override val columnName: String,
    override val required: Boolean = false
) : AbstractDataField()

class DataRawContactsFields internal constructor() : AbstractDataFieldSet<DataRawContactsField>() {

    @JvmField
    val Id = DataRawContactsField(Data.RAW_CONTACT_ID, required = true)

    override val all by lazy {
        setOf(Id)
    }

    // The GeneralMatch algorithm of the Contacts Provider does not match any of these fields.
    override val forMatching = emptySet<DataRawContactsField>()
}

// endregion

// region Data Fields

sealed class DataField : AbstractDataField() {
    internal abstract val mimeType: MimeType
}

internal object EmptyDataFields : AbstractDataFieldSet<DataField>() {

    override val all = emptySet<DataField>()

    override val forMatching = emptySet<DataField>()
}

data class AddressField internal constructor(override val columnName: String) : DataField() {
    override val mimeType: MimeType = MimeType.Address
}

class AddressFields internal constructor() : AbstractDataFieldSet<AddressField>() {

    @JvmField
    val Type = AddressField(CommonDataKinds.StructuredPostal.TYPE)

    @JvmField
    val Label = AddressField(CommonDataKinds.StructuredPostal.LABEL)

    @JvmField
    val FormattedAddress = AddressField(CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS)

    @JvmField
    val Street = AddressField(CommonDataKinds.StructuredPostal.STREET)

    @JvmField
    val PoBox = AddressField(CommonDataKinds.StructuredPostal.POBOX)

    @JvmField
    val Neighborhood = AddressField(CommonDataKinds.StructuredPostal.NEIGHBORHOOD)

    @JvmField
    val City = AddressField(CommonDataKinds.StructuredPostal.CITY)

    @JvmField
    val Region = AddressField(CommonDataKinds.StructuredPostal.REGION)

    @JvmField
    val PostCode = AddressField(CommonDataKinds.StructuredPostal.POSTCODE)

    @JvmField
    val Country = AddressField(CommonDataKinds.StructuredPostal.COUNTRY)

    override val all by lazy {
        setOf(
            Type, Label, FormattedAddress,
            Street, PoBox, Neighborhood,
            City, Region, PostCode, Country
        )
    }

    override val forMatching by lazy {
        // The GeneralMatch algorithm of the Contacts Provider only matches this field.
        setOf(FormattedAddress)
    }
}

data class EmailField internal constructor(override val columnName: String) : DataField() {
    override val mimeType: MimeType = MimeType.Email
}

class EmailFields internal constructor() : AbstractDataFieldSet<EmailField>() {

    @JvmField
    val Type = EmailField(CommonDataKinds.Email.TYPE)

    @JvmField
    val Label = EmailField(CommonDataKinds.Email.LABEL)

    @JvmField
    val Address = EmailField(CommonDataKinds.Email.ADDRESS)

    override val all by lazy {
        setOf(Type, Label, Address)
    }

    override val forMatching by lazy {
        // The GeneralMatch algorithm of the Contacts Provider only matches this field.
        setOf(Address)
    }
}

data class EventField internal constructor(override val columnName: String) : DataField() {
    override val mimeType: MimeType = MimeType.Event
}

class EventFields internal constructor() : AbstractDataFieldSet<EventField>() {

    @JvmField
    val Type = EventField(CommonDataKinds.Event.TYPE)

    @JvmField
    val Label = EventField(CommonDataKinds.Event.LABEL)

    @JvmField
    val Date = EventField(CommonDataKinds.Event.START_DATE)

    override val all by lazy {
        setOf(Type, Label, Date)
    }

    // The GeneralMatch algorithm of the Contacts Provider does not match any of these fields.
    override val forMatching = emptySet<EventField>()
}

data class GroupMembershipField internal constructor(override val columnName: String) :
    DataField() {
    override val mimeType: MimeType = MimeType.GroupMembership
}

class GroupMembershipFields internal constructor() : AbstractDataFieldSet<GroupMembershipField>() {

    @JvmField
    val GroupId = GroupMembershipField(CommonDataKinds.GroupMembership.GROUP_ROW_ID)

    override val all by lazy {
        setOf(GroupId)
    }

    // The GeneralMatch algorithm of the Contacts Provider does not match any of these fields.
    override val forMatching = emptySet<GroupMembershipField>()
}

data class ImField internal constructor(override val columnName: String) : DataField() {
    override val mimeType: MimeType = MimeType.Im
}

class ImFields internal constructor() : AbstractDataFieldSet<ImField>() {

    @JvmField
    val Protocol = ImField(CommonDataKinds.Im.PROTOCOL)

    @JvmField
    val CustomProtocol = ImField(CommonDataKinds.Im.CUSTOM_PROTOCOL)

    @JvmField
    val Data = ImField(CommonDataKinds.Im.DATA)

    override val all by lazy {
        setOf(Protocol, CustomProtocol, Data)
    }

    override val forMatching by lazy {
        // The GeneralMatch algorithm of the Contacts Provider actually matches the Data and
        // Protocol. We are excluding Protocol here because its value is a number, not actual
        // text (E.G. AIM's actual value in the DB is 0). These fields are typically used for the
        // custom Query, not BroadQuery (which uses GeneralMatch algorithm of the Contacts
        // Provider).
        // FIXME? Figure out how the GeneralMatch algorithm of the Contacts Provider matches
        // a (user input) text to a (constant) number. Perhaps it has an index table containing
        // a mapping of the Protocol number to its localized text? Or maybe it performs a query
        // on the text to find the corresponding Protocol number?
        setOf(Data /*, Protocol */)
    }
}

data class NameField internal constructor(override val columnName: String) : DataField() {
    override val mimeType: MimeType = MimeType.Name
}

class NameFields internal constructor() : AbstractDataFieldSet<NameField>() {

    @JvmField
    val DisplayName = NameField(CommonDataKinds.StructuredName.DISPLAY_NAME)

    @JvmField
    val GivenName = NameField(CommonDataKinds.StructuredName.GIVEN_NAME)

    @JvmField
    val MiddleName = NameField(CommonDataKinds.StructuredName.MIDDLE_NAME)

    @JvmField
    val FamilyName = NameField(CommonDataKinds.StructuredName.FAMILY_NAME)

    @JvmField
    val Prefix = NameField(CommonDataKinds.StructuredName.PREFIX)

    @JvmField
    val Suffix = NameField(CommonDataKinds.StructuredName.SUFFIX)

    @JvmField
    val PhoneticGivenName = NameField(CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME)

    @JvmField
    val PhoneticMiddleName = NameField(CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME)

    @JvmField
    val PhoneticFamilyName = NameField(CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME)

    override val all by lazy {
        setOf(
            DisplayName,
            GivenName, MiddleName, FamilyName,
            Prefix, Suffix,
            PhoneticGivenName, PhoneticMiddleName, PhoneticFamilyName
        )
    }

    override val forMatching by lazy {
        // The GeneralMatch algorithm of the Contacts Provider only matches these fields.
        setOf(DisplayName, PhoneticGivenName, PhoneticMiddleName, PhoneticFamilyName)
    }
}

data class NicknameField internal constructor(override val columnName: String) : DataField() {
    override val mimeType: MimeType = MimeType.Nickname
}

class NicknameFields internal constructor() : AbstractDataFieldSet<NicknameField>() {

    @JvmField
    val Name = NicknameField(CommonDataKinds.Nickname.NAME)

    override val all by lazy {
        setOf(Name)
    }

    override val forMatching by lazy {
        // The GeneralMatch algorithm of the Contacts Provider only matches this field.
        setOf(Name)
    }
}

data class NoteField internal constructor(override val columnName: String) : DataField() {
    override val mimeType: MimeType = MimeType.Note
}

class NoteFields internal constructor() : AbstractDataFieldSet<NoteField>() {

    @JvmField
    val Note = NoteField(CommonDataKinds.Note.NOTE)

    override val all by lazy {
        setOf(Note)
    }

    override val forMatching by lazy {
        // The GeneralMatch algorithm of the Contacts Provider only matches this field.
        setOf(Note)
    }
}

data class OrganizationField internal constructor(override val columnName: String) :
    DataField() {
    override val mimeType: MimeType = MimeType.Organization
}

class OrganizationFields internal constructor() : AbstractDataFieldSet<OrganizationField>() {

    @JvmField
    val Company = OrganizationField(CommonDataKinds.Organization.COMPANY)

    @JvmField
    val Title = OrganizationField(CommonDataKinds.Organization.TITLE)

    @JvmField
    val Department = OrganizationField(CommonDataKinds.Organization.DEPARTMENT)

    @JvmField
    val JobDescription =
        OrganizationField(CommonDataKinds.Organization.JOB_DESCRIPTION)

    @JvmField
    val OfficeLocation =
        OrganizationField(CommonDataKinds.Organization.OFFICE_LOCATION)

    @JvmField
    val Symbol = OrganizationField(CommonDataKinds.Organization.SYMBOL)

    @JvmField
    val PhoneticName = OrganizationField(CommonDataKinds.Organization.PHONETIC_NAME)

    override val all by lazy {
        setOf(
            Company, Title, Department, JobDescription, OfficeLocation, Symbol, PhoneticName
        )
    }

    override val forMatching by lazy {
        // The GeneralMatch algorithm of the Contacts Provider matches all fields.
        all
    }
}

data class PhoneField internal constructor(override val columnName: String) : DataField() {
    override val mimeType: MimeType = MimeType.Phone
}

class PhoneFields internal constructor() : AbstractDataFieldSet<PhoneField>() {

    @JvmField
    val Type = PhoneField(CommonDataKinds.Phone.TYPE)

    @JvmField
    val Label = PhoneField(CommonDataKinds.Phone.LABEL)

    @JvmField
    val Number = PhoneField(CommonDataKinds.Phone.NUMBER)

    @JvmField
    val NormalizedNumber = PhoneField(CommonDataKinds.Phone.NORMALIZED_NUMBER)

    override val all by lazy {
        setOf(Type, Label, Number, NormalizedNumber)
    }

    override val forMatching by lazy {
        // The GeneralMatch algorithm of the Contacts Provider only matches this field.
        setOf(Number)
    }
}

data class PhotoField internal constructor(override val columnName: String) :
    DataField() {
    override val mimeType: MimeType = MimeType.Photo
}

class PhotoFields internal constructor() : AbstractDataFieldSet<PhotoField>() {

    @JvmField
    val PhotoFileId = PhotoField(CommonDataKinds.Photo.PHOTO_FILE_ID)

    // Do not expose this field to consumers to avoid confusion. It is not included in the returned
    // Photo entities. The can include all to query for RawContacts with or without photos.
    internal val PhotoThumbnail = PhotoField(CommonDataKinds.Photo.PHOTO)

    /**
     * DO NOT use this property unless you intend to get the photo thumbnail binary data!
     */
    override val all by lazy {
        setOf(PhotoFileId, PhotoThumbnail)
    }

    // The GeneralMatch algorithm of the Contacts Provider does not match any of these fields.
    override val forMatching = emptySet<PhotoField>()
}

data class RelationField internal constructor(override val columnName: String) : DataField() {
    override val mimeType: MimeType = MimeType.Relation
}

class RelationFields internal constructor() : AbstractDataFieldSet<RelationField>() {

    @JvmField
    val Type = RelationField(CommonDataKinds.Relation.TYPE)

    @JvmField
    val Label = RelationField(CommonDataKinds.Relation.LABEL)

    @JvmField
    val Name = RelationField(CommonDataKinds.Relation.NAME)

    override val all by lazy {
        setOf(Type, Label, Name)
    }

    // The GeneralMatch algorithm of the Contacts Provider does not match any of these fields.
    // It kinda makes sense to match the Name BUT I'd rather match the behavior of the GeneralMatch
    // algorithm of the Contacts Provider.
    override val forMatching = emptySet<RelationField>()
}

data class SipAddressField internal constructor(override val columnName: String) : DataField() {
    override val mimeType: MimeType = MimeType.SipAddress
}

class SipAddressFields internal constructor() : AbstractDataFieldSet<SipAddressField>() {

    @JvmField
    val SipAddress = SipAddressField(CommonDataKinds.SipAddress.SIP_ADDRESS)

    override val all by lazy {
        setOf(SipAddress)
    }

    // The GeneralMatch algorithm of the Contacts Provider does not match any of these fields.
    // It kinda makes sense to match the SipAddress BUT I'd rather match the behavior of the
    // GeneralMatch algorithm of the Contacts Provider.
    override val forMatching = emptySet<SipAddressField>()
}

data class WebsiteField internal constructor(override val columnName: String) : DataField() {
    override val mimeType: MimeType = MimeType.Website
}

class WebsiteFields internal constructor() : AbstractDataFieldSet<WebsiteField>() {

    @JvmField
    val Url = WebsiteField(CommonDataKinds.Website.URL)

    override val all by lazy {
        setOf(Url)
    }

    // The GeneralMatch algorithm of the Contacts Provider does not match any of these fields.
    // It kinda makes sense to match the Url BUT I'd rather match the behavior of the GeneralMatch
    // algorithm of the Contacts Provider.
    override val forMatching = emptySet<WebsiteField>()
}

// region Custom Data Fields

/**
 * Base type of all custom data fields.
 *
 * ## Developer notes
 *
 * This had to be declared here instead of in the [contacts.core.entities.custom] package because
 * [DataField] is sealed.
 *
 * This is not sealed so that it can be extended by consumers.
 */
abstract class AbstractCustomDataField(
    /**
     * The name of this column. Must be one of [ColumnName].
     */
    columnName: ColumnName
) : DataField() {

    protected abstract val customMimeType: MimeType.Custom

    final override val mimeType: MimeType
        get() = customMimeType

    final override val columnName: String = columnName.value

    /**
     * Possible column names for custom data of a certain [mimeType].
     */
    enum class ColumnName(internal val value: String) {
        /**
         * Primary data of an entity. For example;
         *
         * - Name display name
         * - Phone number
         * - Formatted address
         * - Website url
         */
        // CommonDataKinds.CommonColumns.DATA
        DATA(Data.DATA1),

        /**
         * The type of data for entities that may have different variations. Column data value is by
         * convention an integer. For example;
         *
         * Phone number type
         *     - TYPE_HOME = 1
         *     - TYPE_MOBILE = 2
         *     - TYPE_WORK = 3
         *     - TYPE_CUSTOM = 0
         *
         * This is optional.
         *
         * ## Developer notes. Also for advanced consumers.
         *
         * DATA2 and DATA3 does not have to be used to only store types and labels. It can also be
         * used to store regular pieces of data. This can be useful if there are a lot of pieces of
         * data for an entity. For example, [NameFields] / [CommonDataKinds.StructuredName] uses
         * DATA2 for the given name and DATA3 for the family name.
         */
        // CommonDataKinds.CommonColumns.TYPE
        TYPE(Data.DATA2),

        /**
         * The label / alias / name of the custom [TYPE]. Only used by data using [TYPE]s,
         * specifically the custom type.
         *
         * Do not use if data has no custom type.
         *
         * ## Developer notes. Also for advanced consumers.
         *
         * DATA2 and DATA3 does not have to be used to only store types and labels. It can also be
         * used to store regular pieces of data. This can be useful if there are a lot of pieces of
         * data for an entity. For example, [NameFields] / [CommonDataKinds.StructuredName] uses
         * DATA2 for the given name and DATA3 for the family name.
         */
        // CommonDataKinds.CommonColumns.LABEL
        LABEL(Data.DATA3),

        /**
         * Another piece of data if the primary data [DATA] is not enough to describe the entity.
         *
         * For example, an address has the following components;
         *
         * - Formatted address (primary data, [DATA])
         * - Street (DATA4)
         * - PO box (DATA5)
         * - Neighborhood (DATA6)
         */
        DATA4(Data.DATA4),

        /**
         * Another piece of data if the primary data [DATA] is not enough to describe the entity.
         *
         * See [DATA4] for more documentation.
         */
        DATA5(Data.DATA5),

        /**
         * Another piece of data if the primary data [DATA] is not enough to describe the entity.
         *
         * See [DATA4] for more documentation.
         */
        DATA6(Data.DATA6),

        /**
         * Another piece of data if the primary data [DATA] is not enough to describe the entity.
         *
         * See [DATA4] for more documentation.
         */
        DATA7(Data.DATA7),

        /**
         * Another piece of data if the primary data [DATA] is not enough to describe the entity.
         *
         * See [DATA4] for more documentation.
         */
        DATA8(Data.DATA8),

        /**
         * Another piece of data if the primary data [DATA] is not enough to describe the entity.
         *
         * See [DATA4] for more documentation.
         */
        DATA9(Data.DATA9),

        /**
         * Another piece of data if the primary data [DATA] is not enough to describe the entity.
         *
         * See [DATA4] for more documentation.
         */
        DATA10(Data.DATA10),

        /**
         * Another piece of data if the primary data [DATA] is not enough to describe the entity.
         *
         * See [DATA4] for more documentation.
         */
        DATA11(Data.DATA11),

        /**
         * Another piece of data if the primary data [DATA] is not enough to describe the entity.
         *
         * See [DATA4] for more documentation.
         */
        DATA12(Data.DATA12),

        /**
         * Another piece of data if the primary data [DATA] is not enough to describe the entity.
         *
         * See [DATA4] for more documentation.
         */
        DATA13(Data.DATA13),

        /**
         * Another piece of data if the primary data [DATA] is not enough to describe the entity.
         *
         * See [DATA4] for more documentation.
         */
        DATA14(Data.DATA14),

        /**
         * Another piece of data if the primary data [DATA] is not enough to describe the entity.
         *
         * By convention, this field is used to store BLOBs (binary data).
         *
         * ## Do not use this for anything but binary data!
         *
         * If you use this for a simple string, users of you custom data that include these fields
         * in their queries may suffer from performance degradations. For example, the photo
         * thumbnails are stored in this field. So if your users include this field, include photo
         * thumbnails (and other binary data) may inadvertently get included by database cursors.
         * This could hurt CPU and memory usage during query processing.
         *
         * ## Do not add this to [AbstractCustomDataFieldSet.all]
         *
         * Similar to [PhotoFields.PhotoThumbnail], blob fields are not included in `all` fields
         * to prevent inadvertently including them in queries using default include all fields.
         * Users have to explicitly include these blob fields to ensure intentionality.
         */
        BLOB(Data.DATA15)
    }
}

/**
 * Base type of all custom data field sets.
 *
 * ## Developer notes
 *
 * This had to be declared here instead of in the [contacts.core.entities.custom] package because
 * [AbstractDataFieldSet] is sealed.
 */
abstract class AbstractCustomDataFieldSet<out T : AbstractCustomDataField> :
    AbstractDataFieldSet<T>()

// endregion

// endregion

// endregion

// region RawContacts Table Fields

data class RawContactsField internal constructor(
    override val columnName: String,
    override val required: Boolean = false
) : Field()

/**
 * Fields for RawContacts table operations.
 */
object RawContactsFields : FieldSet<RawContactsField>() {

    @JvmField
    val Id = RawContactsField(RawContacts._ID, required = true)

    @JvmField
    val ContactId = RawContactsField(RawContacts.CONTACT_ID, required = true)

    @JvmField
    val SourceId = RawContactsField(RawContacts.SOURCE_ID)

    @JvmField
    val DisplayNamePrimary = RawContactsField(RawContacts.DISPLAY_NAME_PRIMARY)

    @JvmField
    val DisplayNameAlt = RawContactsField(RawContacts.DISPLAY_NAME_ALTERNATIVE)

    @JvmField
    // From protected SyncColumns
    val AccountName = RawContactsField(RawContacts.ACCOUNT_NAME)

    @JvmField
    // From protected SyncColumns
    val AccountType = RawContactsField(RawContacts.ACCOUNT_TYPE)

    @JvmField
    val Options = RawContactsOptionsFields()

    internal val Deleted = RawContactsField(RawContacts.DELETED)

    /**
     * See [RequiredRawContactsFields].
     */
    @JvmField
    val Required = RequiredRawContactsFields

    override val all by lazy {
        mutableSetOf(
            Id, ContactId, SourceId,
            DisplayNamePrimary, DisplayNameAlt,
            AccountName, AccountType,
            Deleted
        ).apply {
            addAll(Options.all)
        }.toSet() // ensure that this is not modifiable at runtime
    }

    /**
     * Same as [all], but as a function. This makes it visible to Java consumers when accessing this
     * using the object reference directly.
     */
    @JvmStatic
    fun all() = all
}

/**
 * The set of raw contacts fields that are required, internally by this library, to be included in
 * all query results.
 */
object RequiredRawContactsFields : FieldSet<RawContactsField>() {

    override val all by lazy {
        setOf(RawContactsFields.Id, RawContactsFields.ContactId)
    }

    /**
     * Same as [all], but as a function. This makes it visible to Java consumers when accessing this
     * using the object reference directly.
     */
    @JvmStatic
    fun all() = all
}

// Contains the same underlying column names as DataContactsOptionsFields and ContactsOptionsFields
// but with a different Field type.
class RawContactsOptionsFields internal constructor() : FieldSet<RawContactsField>() {

    internal val Id = RawContactsField(RawContacts._ID, required = true)

    @JvmField
    val Starred = RawContactsField(RawContacts.STARRED)

    /* Deprecated in API 29 - contains useless value for all Android versions from the Play store.
    @JvmField
    val TimesContacted = RawContactsField(RawContacts.TIMES_CONTACTED)

    @JvmField
    val LastTimeContacted = RawContactsField(RawContacts.LAST_TIME_CONTACTED)
     */

    @JvmField
    val CustomRingtone = RawContactsField(RawContacts.CUSTOM_RINGTONE)

    @JvmField
    val SendToVoicemail = RawContactsField(RawContacts.SEND_TO_VOICEMAIL)

    override val all by lazy {
        setOf(
            Id, Starred, CustomRingtone, SendToVoicemail
        )
    }
}

// endregion

// region Contacts Table Fields

data class ContactsField internal constructor(
    override val columnName: String,
    override val required: Boolean = false
) : Field()

/**
 * Fields for Contacts table operations.
 */
object ContactsFields : FieldSet<ContactsField>() {

    @JvmField
    val Id = ContactsField(Contacts._ID, required = true)

    @JvmField
    val LookupKey = ContactsField(Data.LOOKUP_KEY)

    @JvmField
    val DisplayNamePrimary = ContactsField(Contacts.DISPLAY_NAME_PRIMARY)

    @JvmField
    val DisplayNameAlt = ContactsField(Contacts.DISPLAY_NAME_ALTERNATIVE)

    @JvmField
    val LastUpdatedTimestamp = ContactsField(Contacts.CONTACT_LAST_UPDATED_TIMESTAMP)

    @JvmField
    val Options = ContactsOptionsFields()

    // Do not include in all.
    internal val DisplayNameSource = ContactsField(Contacts.DISPLAY_NAME_SOURCE)

    // Do not include in all.
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    internal val NameRawContactId = ContactsField(Contacts.NAME_RAW_CONTACT_ID)

    @JvmField
    val PhotoUri = ContactsField(Contacts.PHOTO_URI)

    @JvmField
    val PhotoThumbnailUri = ContactsField(Contacts.PHOTO_THUMBNAIL_URI)

    internal val PhotoFileId = ContactsField(Contacts.PHOTO_FILE_ID)

    @JvmField
    val HasPhoneNumber = ContactsField(Contacts.HAS_PHONE_NUMBER)

    override val all by lazy {
        mutableSetOf(
            Id, LookupKey, DisplayNamePrimary, DisplayNameAlt, LastUpdatedTimestamp,
            PhotoUri, PhotoThumbnailUri, PhotoFileId, HasPhoneNumber
        ).apply {
            addAll(Options.all)
        }
    }

    /**
     * Same as [all], but as a function. This makes it visible to Java consumers when accessing this
     * using the object reference directly.
     */
    @JvmStatic
    fun all() = all
}

// Contains the same underlying column names as DataContactsOptionsFields and
// RawContactsOptionsFields but with a different Field type.
class ContactsOptionsFields internal constructor() : FieldSet<ContactsField>() {

    internal val Id = ContactsField(Contacts._ID, required = true)

    @JvmField
    val Starred = ContactsField(Contacts.STARRED)

    /* Deprecated in API 29 - contains useless value for all Android versions from the Play store.
    @JvmField
    val TimesContacted = ContactsField(Contacts.TIMES_CONTACTED)

    @JvmField
    val LastTimeContacted = ContactsField(Contacts.LAST_TIME_CONTACTED)
     */

    @JvmField
    val CustomRingtone = ContactsField(Contacts.CUSTOM_RINGTONE)

    @JvmField
    val SendToVoicemail = ContactsField(Contacts.SEND_TO_VOICEMAIL)

    override val all by lazy {
        setOf(
            Id, Starred, CustomRingtone, SendToVoicemail
        )
    }
}

// endregion

// region PhoneLookup Table Fields

data class PhoneLookupField internal constructor(
    override val columnName: String,
    override val required: Boolean = false
) : Field()

/**
 * Fields for PhoneLookup table operations.
 */
object PhoneLookupFields : FieldSet<PhoneLookupField>() {

    // This actually contains the Contact ID
    internal val Id = PhoneLookupField(PhoneLookup._ID)

    // Technically not needed because the Id contains the Contact ID but we should be using this
    // for supported API version for future-proofing.
    @TargetApi(Build.VERSION_CODES.N)
    internal val ContactId = PhoneLookupField(PhoneLookup.CONTACT_ID)

    override val all by lazy {
        // The ContactId is intentionally not included because it serves the same purpose as Id
        // and is not available for all API versions.
        setOf(Id)
    }
}

// endregion

// region Groups Table Fields

data class GroupsField internal constructor(
    override val columnName: String,
    override val required: Boolean = false
) : Field()

/**
 * Fields for Groups table operations.
 */
object GroupsFields : FieldSet<GroupsField>() {

    @JvmField
    val Id = GroupsField(Groups._ID, required = true)

    @JvmField
    val SystemId = GroupsField(Groups.SYSTEM_ID)

    @JvmField
    val Title = GroupsField(Groups.TITLE)

    @JvmField
    val ReadOnly = GroupsField(Groups.GROUP_IS_READ_ONLY)

    @JvmField
    val Favorites = GroupsField(Groups.FAVORITES)

    @JvmField
    val AutoAdd = GroupsField(Groups.AUTO_ADD)

    @JvmField
    // From protected SyncColumns
    val AccountName = GroupsField(Groups.ACCOUNT_NAME)

    @JvmField
    // From protected SyncColumns
    val AccountType = GroupsField(Groups.ACCOUNT_TYPE)

    internal val Deleted = GroupsField(Groups.DELETED)

    override val all by lazy {
        setOf(Id, SystemId, Title, ReadOnly, Favorites, AutoAdd, AccountName, AccountType, Deleted)
    }

    /**
     * Same as [all], but as a function. This makes it visible to Java consumers when accessing this
     * using the object reference directly.
     */
    @JvmStatic
    fun all() = all
}

// endregion

// region AggregationExceptions Table Fields

data class AggregationExceptionsField internal constructor(override val columnName: String) :
    Field()

/**
 * Fields for AggregationExceptions table operations.
 */
internal object AggregationExceptionsFields : FieldSet<AggregationExceptionsField>() {

    val Type = AggregationExceptionsField(AggregationExceptions.TYPE)

    val RawContactId1 = AggregationExceptionsField(AggregationExceptions.RAW_CONTACT_ID1)

    val RawContactId2 = AggregationExceptionsField(AggregationExceptions.RAW_CONTACT_ID2)

    override val all by lazy {
        setOf(Type, RawContactId1, RawContactId2)
    }
}

// endregion

// region Blocked Number Table Fields

data class BlockedNumbersField internal constructor(
    override val columnName: String,
    override val required: Boolean = false
) : Field()

/**
 * Fields for Blocked Numbers table operations.
 */
// [ANDROID X] @RequiresApi (not using annotation to avoid dependency on androidx.annotation)
@TargetApi(Build.VERSION_CODES.N)
object BlockedNumbersFields : FieldSet<BlockedNumbersField>() {

    @JvmField
    val Id = BlockedNumbersField(BlockedNumberContract.BlockedNumbers.COLUMN_ID, required = true)

    @JvmField
    val Number = BlockedNumbersField(BlockedNumberContract.BlockedNumbers.COLUMN_ORIGINAL_NUMBER)

    @JvmField
    val NormalizedNumber =
        BlockedNumbersField(BlockedNumberContract.BlockedNumbers.COLUMN_E164_NUMBER)

    override val all by lazy {
        setOf(Id, Number, NormalizedNumber)
    }

    /**
     * Same as [all], but as a function. This makes it visible to Java consumers when accessing this
     * using the object reference directly.
     */
    @JvmStatic
    fun all() = all
}

// endregion

// region SIM Table Fields

data class SimContactsField internal constructor(
    override val columnName: String,
    override val required: Boolean = false
) : Field()

/**
 * Fields for SIM table operations.
 */
// Given that we are not adding include, where, and orderBy, functions in SIM queries due to
// projection, selection, and order not being supported, there is no need to expose this to
// consumers.
internal object SimContactsFields : FieldSet<SimContactsField>() {

    val Id = SimContactsField("_id", required = true)

    val Name = SimContactsField("name")

    /**
     * This is only used for populating the [Name] during insert operations. For some reason,
     * using the [Name] when inserting into the SIM card table does not set the name but this does.
     *
     * I do see it in the com.android.internal.telephony.IccProvider.java
     *
     * Do not include this in [all]!
     */
    val Tag = SimContactsField("tag")

    val NewTag = SimContactsField("newTag")

    val Number = SimContactsField("number")

    val NewNumber = SimContactsField("newNumber")

    // Not supporting emails until proper application-level support for it is implemented by Android.
    // val Emails = SimContactsField("emails")
    // val NewEmails = SimContactsField("newEmails")

    override val all by lazy {
        // Only added fields used for queries
        setOf(Id, Name, Number)
    }

    /**
     * Same as [all], but as a function. This makes it visible to Java consumers when accessing this
     * using the object reference directly.
     */
    @JvmStatic
    fun all() = all
}

// endregion