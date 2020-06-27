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

/*
 * A note about Data fields.
 *
 * I actually had a more granular / typed hierarchy (generics) setup when it came to DataFields.
 *
 * AbstractDataField
 *   - DataField
 *   - JoinedDataField
 *   - CommonDataField
 *     - AddressField
 *     - EmailField
 *     - ...
 *
 * However, it didn't really work out because of generics restrictions and type erasure issues.
 * So, I settled for just a concrete DataField even though it meant repeatedly passing in the same
 * mimeType for related fields. Besides, there really isn't a need for that much granularity.
 */

data class DataField internal constructor(
    override val columnName: String,
    internal val mimeType: MimeType = MimeType.UNKNOWN
) : Field()

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
object Fields : FieldSet<DataField>() {

    @JvmField
    val Address = AddressFields()

    @JvmField
    val Contact = JoinedContactsFields()

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
    val Options = JoinedOptionsFields()

    @JvmField
    val Organization = OrganizationFields()

    @JvmField
    val Phone = PhoneFields()

    internal val Photo = PhotoFields()

    @JvmField
    val RawContact = JoinedRawContactsFields()

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
     * - IDs, e.g. [JoinedContactsFields.Id] (stored as integers in the DB)
     * - [GroupMembershipFields] (stored as integers in the DB)
     * - [JoinedOptionsFields]
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
     * - IDs, e.g. [JoinedContactsFields.Id] (stored as integers in the DB)
     * - [GroupMembershipFields] (stored as integers in the DB)
     * - [JoinedOptionsFields]
     *
     * Use [ForMatching] instead, which does not include the above fields.
     */
    override val all = mutableSetOf<DataField>().apply {
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
        addAll(Options.all)
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

object RequiredDataFields : FieldSet<DataField>() {
    override val all = setOf(
        Fields.DataId,
        Fields.RawContact.Id,
        Fields.Contact.Id,
        Fields.MimeType,
        Fields.IsPrimary,
        Fields.IsSuperPrimary
    )
}

object DataFieldsForMatching : FieldSet<DataField>() {
    override val all = mutableSetOf<DataField>().apply {
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

internal object EmptyDataFields : FieldSet<DataField>() {
    override val all: Set<DataField> = emptySet()
}

// endregion

// region Common Data Fields

class AddressFields internal constructor() : FieldSet<DataField>() {

    @JvmField
    val Type = DataField(CommonDataKinds.StructuredPostal.TYPE, MimeType.ADDRESS)

    @JvmField
    val Label = DataField(CommonDataKinds.StructuredPostal.LABEL, MimeType.ADDRESS)

    @JvmField
    val FormattedAddress =
        DataField(CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, MimeType.ADDRESS)

    @JvmField
    val Street = DataField(CommonDataKinds.StructuredPostal.STREET, MimeType.ADDRESS)

    @JvmField
    val PoBox = DataField(CommonDataKinds.StructuredPostal.POBOX, MimeType.ADDRESS)

    @JvmField
    val Neighborhood = DataField(CommonDataKinds.StructuredPostal.NEIGHBORHOOD, MimeType.ADDRESS)

    @JvmField
    val City = DataField(CommonDataKinds.StructuredPostal.CITY, MimeType.ADDRESS)

    @JvmField
    val Region = DataField(CommonDataKinds.StructuredPostal.REGION, MimeType.ADDRESS)

    @JvmField
    val PostCode = DataField(CommonDataKinds.StructuredPostal.POSTCODE, MimeType.ADDRESS)

    @JvmField
    val Country = DataField(CommonDataKinds.StructuredPostal.COUNTRY, MimeType.ADDRESS)

    override val all = setOf(
        Type, Label, FormattedAddress,
        Street, PoBox, Neighborhood,
        City, Region, PostCode, Country
    )
}

class EmailFields internal constructor() : FieldSet<DataField>() {

    @JvmField
    val Type = DataField(CommonDataKinds.Email.TYPE, MimeType.EMAIL)

    @JvmField
    val Label = DataField(CommonDataKinds.Email.LABEL, MimeType.EMAIL)

    @JvmField
    val Address = DataField(CommonDataKinds.Email.ADDRESS, MimeType.EMAIL)

    override val all = setOf(Type, Label, Address)
}

class EventFields internal constructor() : FieldSet<DataField>() {

    @JvmField
    val Type = DataField(CommonDataKinds.Event.TYPE, MimeType.EVENT)

    @JvmField
    val Label = DataField(CommonDataKinds.Event.LABEL, MimeType.EVENT)

    @JvmField
    val Date = DataField(CommonDataKinds.Event.START_DATE, MimeType.EVENT)

    override val all = setOf(Type, Label, Date)
}

class GroupMembershipFields internal constructor() : FieldSet<DataField>() {

    @JvmField
    val GroupId = DataField(CommonDataKinds.GroupMembership.GROUP_ROW_ID, MimeType.GROUP_MEMBERSHIP)

    override val all = setOf(GroupId)
}

class ImFields internal constructor() : FieldSet<DataField>() {

    @JvmField
    val Protocol = DataField(CommonDataKinds.Im.PROTOCOL, MimeType.IM)

    @JvmField
    val CustomProtocol = DataField(CommonDataKinds.Im.CUSTOM_PROTOCOL, MimeType.IM)

    @JvmField
    val Data = DataField(CommonDataKinds.Im.DATA, MimeType.IM)

    override val all = setOf(Protocol, CustomProtocol, Data)
}

class NameFields internal constructor() : FieldSet<DataField>() {

    @JvmField
    val DisplayName = DataField(CommonDataKinds.StructuredName.DISPLAY_NAME, MimeType.NAME)

    @JvmField
    val GivenName = DataField(CommonDataKinds.StructuredName.GIVEN_NAME, MimeType.NAME)

    @JvmField
    val MiddleName = DataField(CommonDataKinds.StructuredName.MIDDLE_NAME, MimeType.NAME)

    @JvmField
    val FamilyName = DataField(CommonDataKinds.StructuredName.FAMILY_NAME, MimeType.NAME)

    @JvmField
    val Prefix = DataField(CommonDataKinds.StructuredName.PREFIX, MimeType.NAME)

    @JvmField
    val Suffix = DataField(CommonDataKinds.StructuredName.SUFFIX, MimeType.NAME)

    @JvmField
    val PhoneticGivenName =
        DataField(CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME, MimeType.NAME)

    @JvmField
    val PhoneticMiddleName =
        DataField(CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME, MimeType.NAME)

    @JvmField
    val PhoneticFamilyName =
        DataField(CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME, MimeType.NAME)

    override val all = setOf(
        DisplayName,
        GivenName, MiddleName, FamilyName,
        Prefix, Suffix,
        PhoneticGivenName, PhoneticMiddleName, PhoneticFamilyName
    )
}

class NicknameFields internal constructor() : FieldSet<DataField>() {

    @JvmField
    val Name = DataField(CommonDataKinds.Nickname.NAME, MimeType.NICKNAME)

    override val all = setOf(Name)
}

class NoteFields internal constructor() : FieldSet<DataField>() {

    @JvmField
    val Note = DataField(CommonDataKinds.Note.NOTE, MimeType.NOTE)

    override val all = setOf(Note)
}

class OrganizationFields internal constructor() : FieldSet<DataField>() {

    @JvmField
    val Company = DataField(CommonDataKinds.Organization.COMPANY, MimeType.ORGANIZATION)

    @JvmField
    val Title = DataField(CommonDataKinds.Organization.TITLE, MimeType.ORGANIZATION)

    @JvmField
    val Department = DataField(CommonDataKinds.Organization.DEPARTMENT, MimeType.ORGANIZATION)

    @JvmField
    val JobDescription =
        DataField(CommonDataKinds.Organization.JOB_DESCRIPTION, MimeType.ORGANIZATION)

    @JvmField
    val OfficeLocation =
        DataField(CommonDataKinds.Organization.OFFICE_LOCATION, MimeType.ORGANIZATION)

    @JvmField
    val Symbol = DataField(CommonDataKinds.Organization.SYMBOL, MimeType.ORGANIZATION)

    @JvmField
    val PhoneticName = DataField(CommonDataKinds.Organization.PHONETIC_NAME, MimeType.ORGANIZATION)

    override val all = setOf(
        Company, Title, Department, JobDescription, OfficeLocation, Symbol, PhoneticName
    )
}

class PhoneFields internal constructor() : FieldSet<DataField>() {

    @JvmField
    val Type = DataField(CommonDataKinds.Phone.TYPE, MimeType.PHONE)

    @JvmField
    val Label = DataField(CommonDataKinds.Phone.LABEL, MimeType.PHONE)

    @JvmField
    val Number = DataField(CommonDataKinds.Phone.NUMBER, MimeType.PHONE)

    @JvmField
    val NormalizedNumber = DataField(CommonDataKinds.Phone.NORMALIZED_NUMBER, MimeType.PHONE)

    override val all = setOf(Type, Label, Number, NormalizedNumber)
}

internal class PhotoFields internal constructor() : FieldSet<DataField>() {

    val PhotoFileId = DataField(CommonDataKinds.Photo.PHOTO_FILE_ID, MimeType.PHOTO)

    val PhotoThumbnail = DataField(CommonDataKinds.Photo.PHOTO, MimeType.PHOTO)

    override val all = setOf(PhotoFileId, PhotoThumbnail)
}

class RelationFields internal constructor() : FieldSet<DataField>() {

    @JvmField
    val Type = DataField(CommonDataKinds.Relation.TYPE, MimeType.RELATION)

    @JvmField
    val Label = DataField(CommonDataKinds.Relation.LABEL, MimeType.RELATION)

    @JvmField
    val Name = DataField(CommonDataKinds.Relation.NAME, MimeType.RELATION)

    override val all = setOf(Type, Label, Name)
}

class SipAddressFields internal constructor() : FieldSet<DataField>() {

    @JvmField
    val SipAddress = DataField(CommonDataKinds.SipAddress.SIP_ADDRESS, MimeType.SIP_ADDRESS)

    override val all = setOf(SipAddress)
}

class WebsiteFields internal constructor() : FieldSet<DataField>() {

    @JvmField
    val Url = DataField(CommonDataKinds.Website.URL, MimeType.WEBSITE)

    override val all = setOf(Url)
}

// endregion

// region Joined Fields

class JoinedContactsFields internal constructor() : FieldSet<DataField>() {

    // The Data.CONTACT_ID, which is not the same as the column name Contacts._ID. This is only
    // meant to be used for Data table operations.
    @JvmField
    val Id = DataField(Data.CONTACT_ID)

    @JvmField
    val DisplayNamePrimary = DataField(Data.DISPLAY_NAME_PRIMARY)

    @JvmField
    val DisplayNameAlt = DataField(Data.DISPLAY_NAME_ALTERNATIVE)

    @JvmField
    val LastUpdatedTimestamp = DataField(Data.CONTACT_LAST_UPDATED_TIMESTAMP)

    override val all = setOf(Id, DisplayNamePrimary, DisplayNameAlt, LastUpdatedTimestamp)
}

class JoinedOptionsFields internal constructor() : FieldSet<DataField>() {

    internal val Id = DataField(Data._ID)

    @JvmField
    val Starred = DataField(Data.STARRED)

    @JvmField
    val TimesContacted = DataField(Data.TIMES_CONTACTED)

    @JvmField
    val LastTimeContacted = DataField(Data.LAST_TIME_CONTACTED)

    @JvmField
    val CustomRingtone = DataField(Data.CUSTOM_RINGTONE)

    @JvmField
    val SendToVoicemail = DataField(Data.SEND_TO_VOICEMAIL)

    override val all = setOf(
        Id, Starred, TimesContacted, LastTimeContacted, CustomRingtone, SendToVoicemail
    )
}

class JoinedRawContactsFields internal constructor() : FieldSet<DataField>() {

    val Id = DataField(Data.RAW_CONTACT_ID)

    override val all = setOf(Id)
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

    // Do not include in fields.
    internal val DisplayNameSource = ContactsField(Contacts.DISPLAY_NAME_SOURCE)

    // Do not include in fields.
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    internal val NameRawContactId = ContactsField(Contacts.NAME_RAW_CONTACT_ID)

    @JvmField
    val LastUpdatedTimestamp = ContactsField(Contacts.CONTACT_LAST_UPDATED_TIMESTAMP)

    internal val PhotoUri = ContactsField(Contacts.PHOTO_URI)

    internal val PhotoThumbnailUri = ContactsField(Contacts.PHOTO_THUMBNAIL_URI)

    internal val PhotoFileId = ContactsField(Contacts.PHOTO_FILE_ID)

    override val all = setOf(
        Id, DisplayNamePrimary, DisplayNameAlt, LastUpdatedTimestamp,
        PhotoUri, PhotoThumbnailUri, PhotoFileId
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

    override val all = setOf(
        Id, ContactId, DisplayNamePrimary, DisplayNameAlt, AccountName, AccountType
    )
}

// endregion