@file:Suppress("PropertyName")

package com.vestrel00.contacts

import android.annotation.TargetApi
import android.os.Build
import android.provider.ContactsContract.*
import android.provider.ContactsContract.Contacts
import com.vestrel00.contacts.entities.MimeType

// region Field Interfaces

/**
 * Represents a database field / column.
 *
 * All concrete implementations of this must be data classes or implement equals and hashCode.
 */
sealed class Field {
    internal abstract val columnName: String

    // Force concrete implementations to implements equals and hashCode
    abstract override fun equals(other: Any?): Boolean

    abstract override fun hashCode(): Int
}

/**
 * Holds a set of [Field]s.
 */
sealed class FieldSet<T : Field> {
    abstract val all: Set<T>
}

// endregion

// region Data Table Fields

sealed class AbstractDataField : Field()

data class DataField internal constructor(override val columnName: String) : AbstractDataField()

/**
 * Contains all fields / columns that are accessible via the Data table with joins from the
 * RawContacts and Contacts tables (ContactsContract.DataColumnsWithJoins).
 *
 * For a shorter name, use [F] (useful for Kotlin-ers).
 *
 * The real (more technically correct) name of this object is [DataFields]. The name "Fields" is
 * used here so that Java consumers may be able to access these most commonly used fields using a
 * shorter name than "DataFields".
 *
 * ## Developer Notes
 *
 * All fields declared within this object such as [Fields.Address] are classes, not objects, so that
 * Java consumers may be able to access the fields within them. For example, if [AddressFields] is
 * an object instead of a class, then [AddressFields.City] (and all other fields) will not be
 * visible to Java consumers via this object.
 */
object Fields : FieldSet<AbstractDataField>() {

    @JvmField
    val Address = AddressFields()

    @JvmField
    val Contact = DataContactsFields()

    @JvmField
    val DataId = DataField(Data._ID)

    @JvmField
    val Email = EmailFields()

    @JvmField
    val Event = EventFields()

    @JvmField
    val GroupMembership = GroupMembershipFields()

    @JvmField
    val Im = ImFields()

    @JvmField
    val IsPrimary = DataField(Data.IS_PRIMARY)

    @JvmField
    val IsSuperPrimary = DataField(Data.IS_SUPER_PRIMARY)

    internal val MimeType = DataField(Data.MIMETYPE)

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

    internal val Photo = PhotoFields()

    @JvmField
    val RawContact = DataRawContactsFields()

    @JvmField
    val Relation = RelationFields()

    @JvmField
    val SipAddress = SipAddressFields()

    @JvmField
    val Website = WebsiteFields()

    @JvmField
    val Required = RequiredDataFields

    /**
     * Contains [Fields.all] excluding fields such as;
     *
     * - Types, e.g. [EmailFields.Type] (stored as integers in the DB)
     * - IDs, e.g. [DataContactsFields.Id] (stored as integers in the DB)
     * - [GroupMembershipFields] (stored as integers in the DB)
     * - [DataContactsOptionsFields]
     *
     * This is safe for matching in queries. This is useful in creating where clauses that matches
     * text that the user is typing in a search field. For example, if the user is typing in some
     * numbers trying to find a phone number, this will not match the above fields.
     */
    @JvmField
    val ForMatching = DataFieldsForMatching

    /**
     * All fields that may be included in a query. This is useful for specifying includes.
     *
     * Be careful with using this for queries. This field set includes the following fields, which
     * may lead to unintentional query matches (especially when matching numbers);
     *
     * - Types, e.g. [EmailFields.Type] (stored as integers in the DB)
     * - IDs, e.g. [DataContactsFields.Id] (stored as integers in the DB)
     * - [GroupMembershipFields] (stored as integers in the DB)
     * - [DataContactsOptionsFields]
     *
     * Use [ForMatching] instead, which does not include the above fields.
     */
    override val all = mutableSetOf<AbstractDataField>().apply {
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
        addAll(Photo.all)
        addAll(RawContact.all)
        addAll(Relation.all)
        addAll(SipAddress.all)
        addAll(Website.all)
    }.toSet() // ensure that this is not modifiable at runtime
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

object RequiredDataFields : FieldSet<AbstractDataField>() {
    override val all = setOf(
        Fields.DataId,
        Fields.RawContact.Id,
        Fields.Contact.Id,
        Fields.MimeType,
        Fields.IsPrimary,
        Fields.IsSuperPrimary
    )
}

object DataFieldsForMatching : FieldSet<AbstractDataField>() {
    override val all = mutableSetOf<AbstractDataField>().apply {
        addAll(Fields.Address.all.asSequence().minus(Fields.Address.Type))
        addAll(
            Fields.Contact.all.asSequence()
                .minus(Fields.Contact.Id)
                .minus(Fields.Contact.LastUpdatedTimestamp)
        )
        // add(Fields.DataId)
        addAll(Fields.Email.all.asSequence().minus(Fields.Email.Type))
        addAll(Fields.Event.all.asSequence().minus(Fields.Event.Type))
        // addAll(Fields.GroupMembership.all)
        addAll(Fields.Im.all.asSequence().minus(Fields.Im.Protocol))
        // add(Fields.IsPrimary)
        // add(Fields.IsSuperPrimary)
        // add(Fields.MimeType)
        addAll(Fields.Name.all)
        addAll(Fields.Nickname.all)
        addAll(Fields.Note.all)
        // addAll(Fields.Options.all)
        addAll(Fields.Organization.all)
        addAll(Fields.Phone.all.asSequence().minus(Fields.Phone.Type))
        // addAll(Fields.Photo.all)
        // addAll(Fields.RawContact.all)
        addAll(Fields.Relation.all.asSequence().minus(Fields.Relation.Type))
        // addAll(Fields.Required.all)
        addAll(Fields.SipAddress.all)
        addAll(Fields.Website.all)
    }.toSet() // ensure that this is not modifiable at runtime
}

// endregion

// region Joined Data Fields

data class DataContactsField internal constructor(override val columnName: String) :
    AbstractDataField()

class DataContactsFields internal constructor() : FieldSet<DataContactsField>() {

    // The Data.CONTACT_ID, which is not the same as the column name Contacts._ID. This is only
    // meant to be used for Data table operations.
    @JvmField
    val Id = DataContactsField(Data.CONTACT_ID)

    @JvmField
    val DisplayNamePrimary = DataContactsField(Data.DISPLAY_NAME_PRIMARY)

    @JvmField
    val DisplayNameAlt = DataContactsField(Data.DISPLAY_NAME_ALTERNATIVE)

    @JvmField
    val LastUpdatedTimestamp = DataContactsField(Data.CONTACT_LAST_UPDATED_TIMESTAMP)

    @JvmField
    val Options = DataContactsOptionsFields()

    override val all = mutableSetOf(
        Id, DisplayNamePrimary, DisplayNameAlt, LastUpdatedTimestamp
    ).apply {
        addAll(Options.all)
    }.toSet()
}

// Contains the same underlying column names as RawContactsOptionsFields and ContactsOptionsFields
// but with a different Field type.
class DataContactsOptionsFields internal constructor() : FieldSet<DataContactsField>() {

    internal val Id = DataContactsField(Data._ID)

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

    override val all = setOf(
        Id, Starred, CustomRingtone, SendToVoicemail
    )
}

data class DataRawContactsField internal constructor(override val columnName: String) :
    AbstractDataField()

class DataRawContactsFields internal constructor() : FieldSet<DataRawContactsField>() {

    @JvmField
    val Id = DataRawContactsField(Data.RAW_CONTACT_ID)

    override val all = setOf(Id)
}

// endregion

// region Common Data Fields

sealed class CommonDataField : AbstractDataField() {
    internal abstract val mimeType: MimeType
}

internal object EmptyCommonDataFields : FieldSet<CommonDataField>() {
    override val all: Set<CommonDataField> = emptySet()
}

data class AddressField internal constructor(override val columnName: String) : CommonDataField() {
    override val mimeType: MimeType = MimeType.ADDRESS
}

class AddressFields internal constructor() : FieldSet<AddressField>() {

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

    override val all = setOf(
        Type, Label, FormattedAddress,
        Street, PoBox, Neighborhood,
        City, Region, PostCode, Country
    )
}

data class EmailField internal constructor(override val columnName: String) : CommonDataField() {
    override val mimeType: MimeType = MimeType.EMAIL
}

class EmailFields internal constructor() : FieldSet<EmailField>() {

    @JvmField
    val Type = EmailField(CommonDataKinds.Email.TYPE)

    @JvmField
    val Label = EmailField(CommonDataKinds.Email.LABEL)

    @JvmField
    val Address = EmailField(CommonDataKinds.Email.ADDRESS)

    override val all = setOf(Type, Label, Address)
}

data class EventField internal constructor(override val columnName: String) : CommonDataField() {
    override val mimeType: MimeType = MimeType.EVENT
}

class EventFields internal constructor() : FieldSet<EventField>() {

    @JvmField
    val Type = EventField(CommonDataKinds.Event.TYPE)

    @JvmField
    val Label = EventField(CommonDataKinds.Event.LABEL)

    @JvmField
    val Date = EventField(CommonDataKinds.Event.START_DATE)

    override val all = setOf(Type, Label, Date)
}

data class GroupMembershipField internal constructor(override val columnName: String) :
    CommonDataField() {
    override val mimeType: MimeType = MimeType.GROUP_MEMBERSHIP
}

class GroupMembershipFields internal constructor() : FieldSet<GroupMembershipField>() {

    @JvmField
    val GroupId = GroupMembershipField(CommonDataKinds.GroupMembership.GROUP_ROW_ID)

    override val all = setOf(GroupId)
}

data class ImField internal constructor(override val columnName: String) : CommonDataField() {
    override val mimeType: MimeType = MimeType.IM
}

class ImFields internal constructor() : FieldSet<ImField>() {

    @JvmField
    val Protocol = ImField(CommonDataKinds.Im.PROTOCOL)

    @JvmField
    val CustomProtocol = ImField(CommonDataKinds.Im.CUSTOM_PROTOCOL)

    @JvmField
    val Data = ImField(CommonDataKinds.Im.DATA)

    override val all = setOf(Protocol, CustomProtocol, Data)
}

data class NameField internal constructor(override val columnName: String) : CommonDataField() {
    override val mimeType: MimeType = MimeType.NAME
}

class NameFields internal constructor() : FieldSet<NameField>() {

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

    override val all = setOf(
        DisplayName,
        GivenName, MiddleName, FamilyName,
        Prefix, Suffix,
        PhoneticGivenName, PhoneticMiddleName, PhoneticFamilyName
    )
}

data class NicknameField internal constructor(override val columnName: String) : CommonDataField() {
    override val mimeType: MimeType = MimeType.NICKNAME
}

class NicknameFields internal constructor() : FieldSet<NicknameField>() {

    @JvmField
    val Name = NicknameField(CommonDataKinds.Nickname.NAME)

    override val all = setOf(Name)
}

data class NoteField internal constructor(override val columnName: String) : CommonDataField() {
    override val mimeType: MimeType = MimeType.NOTE
}

class NoteFields internal constructor() : FieldSet<NoteField>() {

    @JvmField
    val Note = NoteField(CommonDataKinds.Note.NOTE)

    override val all = setOf(Note)
}

data class OrganizationField internal constructor(override val columnName: String) :
    CommonDataField() {
    override val mimeType: MimeType = MimeType.ORGANIZATION
}

class OrganizationFields internal constructor() : FieldSet<OrganizationField>() {

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

    override val all = setOf(
        Company, Title, Department, JobDescription, OfficeLocation, Symbol, PhoneticName
    )
}

data class PhoneField internal constructor(override val columnName: String) : CommonDataField() {
    override val mimeType: MimeType = MimeType.PHONE
}

class PhoneFields internal constructor() : FieldSet<PhoneField>() {

    @JvmField
    val Type = PhoneField(CommonDataKinds.Phone.TYPE)

    @JvmField
    val Label = PhoneField(CommonDataKinds.Phone.LABEL)

    @JvmField
    val Number = PhoneField(CommonDataKinds.Phone.NUMBER)

    @JvmField
    val NormalizedNumber = PhoneField(CommonDataKinds.Phone.NORMALIZED_NUMBER)

    override val all = setOf(Type, Label, Number, NormalizedNumber)
}

internal data class PhotoField internal constructor(override val columnName: String) :
    CommonDataField() {
    override val mimeType: MimeType = MimeType.PHOTO
}

internal class PhotoFields internal constructor() : FieldSet<PhotoField>() {

    val PhotoFileId = PhotoField(CommonDataKinds.Photo.PHOTO_FILE_ID)

    val PhotoThumbnail = PhotoField(CommonDataKinds.Photo.PHOTO)

    override val all = setOf(PhotoFileId, PhotoThumbnail)
}

data class RelationField internal constructor(override val columnName: String) : CommonDataField() {
    override val mimeType: MimeType = MimeType.RELATION
}

class RelationFields internal constructor() : FieldSet<RelationField>() {

    @JvmField
    val Type = RelationField(CommonDataKinds.Relation.TYPE)

    @JvmField
    val Label = RelationField(CommonDataKinds.Relation.LABEL)

    @JvmField
    val Name = RelationField(CommonDataKinds.Relation.NAME)

    override val all = setOf(Type, Label, Name)
}

data class SipAddressField internal constructor(override val columnName: String) :
    CommonDataField() {
    override val mimeType: MimeType = MimeType.SIP_ADDRESS
}

class SipAddressFields internal constructor() : FieldSet<SipAddressField>() {

    @JvmField
    val SipAddress = SipAddressField(CommonDataKinds.SipAddress.SIP_ADDRESS)

    override val all = setOf(SipAddress)
}

data class WebsiteField internal constructor(override val columnName: String) : CommonDataField() {
    override val mimeType: MimeType = MimeType.WEBSITE
}

class WebsiteFields internal constructor() : FieldSet<WebsiteField>() {

    @JvmField
    val Url = WebsiteField(CommonDataKinds.Website.URL)

    override val all = setOf(Url)
}

// endregion

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

    override val all = setOf(Type, RawContactId1, RawContactId2)
}

// endregion

// region Contacts Table Fields

data class ContactsField internal constructor(override val columnName: String) : Field()

/**
 * Fields for Contacts table operations.
 */
object ContactsFields : FieldSet<ContactsField>() {

    @JvmField
    val Id = ContactsField(Contacts._ID)

    @JvmField
    val DisplayNamePrimary = ContactsField(Contacts.DISPLAY_NAME_PRIMARY)

    @JvmField
    val DisplayNameAlt = ContactsField(Contacts.DISPLAY_NAME_ALTERNATIVE)

    @JvmField
    val LastUpdatedTimestamp = ContactsField(Contacts.CONTACT_LAST_UPDATED_TIMESTAMP)

    @JvmField
    val Options = ContactsOptionsFields()

    // Do not include in fields.
    internal val DisplayNameSource = ContactsField(Contacts.DISPLAY_NAME_SOURCE)

    // Do not include in fields.
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    internal val NameRawContactId = ContactsField(Contacts.NAME_RAW_CONTACT_ID)

    internal val PhotoUri = ContactsField(Contacts.PHOTO_URI)

    internal val PhotoThumbnailUri = ContactsField(Contacts.PHOTO_THUMBNAIL_URI)

    internal val PhotoFileId = ContactsField(Contacts.PHOTO_FILE_ID)

    override val all = mutableSetOf(
        Id, DisplayNamePrimary, DisplayNameAlt, LastUpdatedTimestamp,
        PhotoUri, PhotoThumbnailUri, PhotoFileId
    ).apply {
        addAll(Options.all)
    }
}

// Contains the same underlying column names as DataContactsOptionsFields and
// RawContactsOptionsFields but with a different Field type.
class ContactsOptionsFields internal constructor() : FieldSet<ContactsField>() {

    internal val Id = ContactsField(Contacts._ID)

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

    override val all = setOf(
        Id, Starred, CustomRingtone, SendToVoicemail
    )
}

// endregion

// region Groups Table Fields

data class GroupsField internal constructor(override val columnName: String) : Field()

/**
 * Fields for Groups table operations.
 */
object GroupsFields : FieldSet<GroupsField>() {

    @JvmField
    val Id = GroupsField(Groups._ID)

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

    override val all = setOf(Id, Title, ReadOnly, Favorites, AutoAdd, AccountName, AccountType)
}

// endregion

// region RawContacts Table Fields

data class RawContactsField internal constructor(override val columnName: String) : Field()

/**
 * Fields for RawContacts table operations.
 */
object RawContactsFields : FieldSet<RawContactsField>() {

    @JvmField
    val Id = RawContactsField(RawContacts._ID)

    @JvmField
    val ContactId = RawContactsField(RawContacts.CONTACT_ID)

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

    override val all = mutableSetOf(
        Id, ContactId, DisplayNamePrimary, DisplayNameAlt, AccountName, AccountType
    ).apply {
        addAll(Options.all)
    }.toSet()
}

// Contains the same underlying column names as DataContactsOptionsFields and ContactsOptionsFields
// but with a different Field type.
class RawContactsOptionsFields internal constructor() : FieldSet<RawContactsField>() {

    internal val Id = RawContactsField(RawContacts._ID)

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

    override val all = setOf(
        Id, Starred, CustomRingtone, SendToVoicemail
    )
}

// endregion