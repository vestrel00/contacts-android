@file:Suppress("PropertyName", "MemberVisibilityCanBePrivate", "unused")

package com.vestrel00.contacts

import android.annotation.TargetApi
import android.os.Build
import android.provider.BaseColumns
import android.provider.ContactsContract.*
import android.provider.ContactsContract.Contacts
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.MimeType.UNKNOWN

/**
 * Represents a database field(s) / column(s) that maps to a Contact attribute.
 */
sealed class Field

/**
 * Represents a database field / column that maps to a Contact attribute.
 */
// It is important that this is a data class because its equals function is used.
// Implement the equals and hashcode functions if removing the data qualifier!
data class AbstractField internal constructor(
    internal val columnName: String,
    internal val mimeType: MimeType
) : Field()

/**
 * Holds a set of [AbstractField]s.
 */
abstract class FieldSet internal constructor(
    internal val mimeType: MimeType
) : Field() {
    internal abstract val fields: Set<AbstractField>
}

/**
 * Contains all Data table [Field]s, including columns from other tables that are joined.
 *
 * These are mainly used for queries.
 */
object Fields {

    /*
     * ## Developer Notes
     *
     * This is not named "DataFields" for several reasons;
     *
     * 1. Consumers are only exposed Data table fields. There is no need for them to know the fields
     *    of the other tables.
     * 2. Fields is shorter than DataFields. This is important for keeping lines short.
     *
     * All of this is condensed inside one file / class for (Kotlin & Java) consumer convenience.
     * The structure of this class and the Kotlin language features that are used here are to cater
     * to both Java and Kotlin consumers, whilst still attempting to keep the code structured in
     * a Kotlin-first fashion.
     */

    @JvmField
    val Address = AddressFields()

    @JvmField
    val Contact = ContactFields()

    @JvmField
    val Email = EmailFields()

    @JvmField
    val Event = EventFields()

    @JvmField
    val GroupMembership = GroupMembershipFields()

    internal val Id = AbstractField(BaseColumns._ID, UNKNOWN)

    @JvmField
    val Im = ImFields()

    @JvmField
    val IsPrimary = AbstractField(Data.IS_PRIMARY, UNKNOWN)

    @JvmField
    val IsSuperPrimary = AbstractField(Data.IS_SUPER_PRIMARY, UNKNOWN)

    internal val MimeType = AbstractField(Data.MIMETYPE, UNKNOWN)

    @JvmField
    val Name = NameFields()

    @JvmField
    val Nickname = NicknameFields()

    @JvmField
    val Note = NoteFields()

    @JvmField
    val Options = OptionsFields()

    @JvmField
    val Organization = OrganizationFields()

    @JvmField
    val Phone = PhoneFields()

    internal val Photo = PhotoFields()

    @JvmField
    val RawContact = RawContactFields()

    @JvmField
    val Relation = RelationFields()

    @JvmField
    val SipAddress = SipAddressFields()

    @JvmField
    val Website = WebsiteFields()

    /**
     * All of the required fields that are always included in every query. This is only useful for
     * specifying includes.
     */
    @JvmField
    val Required = RequiredFields()

    /**
     * All fields that may be included in a query. This is only useful for specifying includes.
     *
     * Be careful with using this for queries. This field set includes the following fields, which
     * may lead to unintentional query matches (especially when matching numbers);
     *
     * - Types, e.g. [EmailFields.Type] (stored as integers in the DB)
     * - IDs, e.g. [ContactFields.Id] (stored as integers in the DB)
     * - [GroupMembershipFields] (stored as integers in the DB)
     * - [OptionsFields]
     *
     * Use [AllForMatching] instead, which does not include the above fields.
     */
    // This needs to be defined at the bottom because it depends on all of the above
    @JvmField
    val All = AllFields()

    /**
     * Same as [All] except this is safe for matching in queries. This is useful in creating where
     * clauses that matches text that the user is typing in a search field.
     */
    @JvmField
    val AllForMatching = AllForMatchingFields()
}

/**
 * All of the Data table fields required when constructing Data entities retrieved from a Query.
 */
class RequiredFields : FieldSet(UNKNOWN) {
    override val fields: Set<AbstractField> = setOf(
        Fields.Id,
        Fields.RawContact.Id,
        Fields.Contact.Id,
        Fields.MimeType,
        Fields.IsPrimary,
        Fields.IsSuperPrimary
    )
}

class AllFields : FieldSet(UNKNOWN) {
    override val fields: Set<AbstractField> = mutableSetOf<AbstractField>().apply {
        addAll(Fields.Address.fields)
        addAll(Fields.Contact.fields)
        addAll(Fields.Email.fields)
        addAll(Fields.Event.fields)
        addAll(Fields.GroupMembership.fields)
        add(Fields.Id)
        addAll(Fields.Im.fields)
        add(Fields.IsPrimary)
        add(Fields.IsSuperPrimary)
        add(Fields.MimeType)
        addAll(Fields.Name.fields)
        addAll(Fields.Nickname.fields)
        addAll(Fields.Note.fields)
        addAll(Fields.Options.fields)
        addAll(Fields.Organization.fields)
        addAll(Fields.Phone.fields)
        addAll(Fields.Photo.fields)
        addAll(Fields.RawContact.fields)
        addAll(Fields.Relation.fields)
        addAll(Fields.Required.fields)
        addAll(Fields.SipAddress.fields)
        addAll(Fields.Website.fields)
    }.toSet()
}

class AllForMatchingFields : FieldSet(UNKNOWN) {
    override val fields: Set<AbstractField> = mutableSetOf<AbstractField>().apply {
        addAll(Fields.Address.fields.asSequence().minus(Fields.Address.Type))
        addAll(
            Fields.Contact.fields.asSequence()
                .minus(Fields.Contact.Id)
                .minus(Fields.Contact.LastUpdatedTimestamp)
        )
        addAll(Fields.Email.fields.asSequence().minus(Fields.Email.Type))
        addAll(Fields.Event.fields.asSequence().minus(Fields.Event.Type))
        // addAll(Fields.GroupMembership.fields)
        // add(Fields.Id)
        addAll(Fields.Im.fields.asSequence().minus(Fields.Im.Protocol))
        // add(Fields.IsPrimary)
        // add(Fields.IsSuperPrimary)
        // add(Fields.MimeType)
        addAll(Fields.Name.fields)
        addAll(Fields.Nickname.fields)
        addAll(Fields.Note.fields)
        // addAll(Fields.Options.fields)
        addAll(Fields.Organization.fields)
        addAll(Fields.Phone.fields.asSequence().minus(Fields.Phone.Type))
        // addAll(Fields.Photo.fields)
        // addAll(Fields.RawContact.fields)
        addAll(Fields.Relation.fields.asSequence().minus(Fields.Relation.Type))
        // addAll(Fields.Required.fields)
        addAll(Fields.SipAddress.fields)
        addAll(Fields.Website.fields)
    }.toSet()
}

class AddressFields : FieldSet(MimeType.ADDRESS) {

    @JvmField
    val Type = AbstractField(CommonDataKinds.StructuredPostal.TYPE, mimeType)

    @JvmField
    val Label = AbstractField(CommonDataKinds.StructuredPostal.LABEL, mimeType)

    @JvmField
    val FormattedAddress =
        AbstractField(CommonDataKinds.StructuredPostal.FORMATTED_ADDRESS, mimeType)

    @JvmField
    val Street = AbstractField(CommonDataKinds.StructuredPostal.STREET, mimeType)

    @JvmField
    val PoBox = AbstractField(CommonDataKinds.StructuredPostal.POBOX, mimeType)

    @JvmField
    val Neighborhood = AbstractField(CommonDataKinds.StructuredPostal.NEIGHBORHOOD, mimeType)

    @JvmField
    val City = AbstractField(CommonDataKinds.StructuredPostal.CITY, mimeType)

    @JvmField
    val Region = AbstractField(CommonDataKinds.StructuredPostal.REGION, mimeType)

    @JvmField
    val PostCode = AbstractField(CommonDataKinds.StructuredPostal.POSTCODE, mimeType)

    @JvmField
    val Country = AbstractField(CommonDataKinds.StructuredPostal.COUNTRY, mimeType)

    override val fields = setOf(
        Type, Label, FormattedAddress,
        Street, PoBox, Neighborhood,
        City, Region, PostCode, Country
    )
}

/**
 * Fields for AggregationExceptions table operations.
 */
internal object AggregationExceptionsFields : FieldSet(UNKNOWN) {

    val Type = AbstractField(AggregationExceptions.TYPE, mimeType)

    val RawContactId1 = AbstractField(AggregationExceptions.RAW_CONTACT_ID1, mimeType)

    val RawContactId2 = AbstractField(AggregationExceptions.RAW_CONTACT_ID2, mimeType)

    override val fields = setOf(Type, RawContactId1, RawContactId2)
}

class ContactFields : FieldSet(UNKNOWN) {

    // The Data.CONTACT_ID, which is not the same as the column name Contacts._ID. This is only
    // meant to be used for Data table operations.
    @JvmField
    val Id = AbstractField(Data.CONTACT_ID, mimeType)

    @JvmField
    val DisplayNamePrimary = AbstractField(Data.DISPLAY_NAME_PRIMARY, mimeType)

    @JvmField
    val DisplayNameAlt = AbstractField(Data.DISPLAY_NAME_ALTERNATIVE, mimeType)

    @JvmField
    val LastUpdatedTimestamp = AbstractField(Data.CONTACT_LAST_UPDATED_TIMESTAMP, mimeType)

    override val fields = setOf(Id, DisplayNamePrimary, DisplayNameAlt, LastUpdatedTimestamp)
}

/**
 * Fields for Contacts table operations.
 */
object ContactsFields : FieldSet(UNKNOWN) {

    @JvmField
    val Id = AbstractField(Contacts._ID, mimeType)

    @JvmField
    val DisplayNamePrimary = AbstractField(Contacts.DISPLAY_NAME_PRIMARY, mimeType)

    @JvmField
    val DisplayNameAlt = AbstractField(Contacts.DISPLAY_NAME_ALTERNATIVE, mimeType)

    // Do not include in fields.
    internal val DisplayNameSource = AbstractField(Contacts.DISPLAY_NAME_SOURCE, mimeType)

    // Do not include in fields.
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    internal val NameRawContactId = AbstractField(Contacts.NAME_RAW_CONTACT_ID, mimeType)

    @JvmField
    val LastUpdatedTimestamp = AbstractField(Contacts.CONTACT_LAST_UPDATED_TIMESTAMP, mimeType)

    internal val PhotoUri = AbstractField(Contacts.PHOTO_URI, mimeType)

    internal val PhotoThumbnailUri = AbstractField(Contacts.PHOTO_THUMBNAIL_URI, mimeType)

    internal val PhotoFileId = AbstractField(Contacts.PHOTO_FILE_ID, mimeType)

    override val fields = setOf(
        Id, DisplayNamePrimary, DisplayNameAlt, LastUpdatedTimestamp,
        PhotoUri, PhotoThumbnailUri, PhotoFileId
    )
}

class EmailFields : FieldSet(MimeType.EMAIL) {

    @JvmField
    val Type = AbstractField(CommonDataKinds.Email.TYPE, mimeType)

    @JvmField
    val Label = AbstractField(CommonDataKinds.Email.LABEL, mimeType)

    @JvmField
    val Address = AbstractField(CommonDataKinds.Email.ADDRESS, mimeType)

    override val fields = setOf(Type, Label, Address)
}

internal object EmptyFieldSet : FieldSet(UNKNOWN) {
    override val fields: Set<AbstractField> = emptySet()
}

class EventFields : FieldSet(MimeType.EVENT) {

    @JvmField
    val Type = AbstractField(CommonDataKinds.Event.TYPE, mimeType)

    @JvmField
    val Label = AbstractField(CommonDataKinds.Event.LABEL, mimeType)

    @JvmField
    val Date = AbstractField(CommonDataKinds.Event.START_DATE, mimeType)

    override val fields = setOf(Type, Label, Date)
}

/**
 * Fields for Groups table operations.
 */
object GroupsFields : FieldSet(UNKNOWN) {

    val Id = AbstractField(Groups._ID, mimeType)

    val Title = AbstractField(Groups.TITLE, mimeType)

    val ReadOnly = AbstractField(Groups.GROUP_IS_READ_ONLY, mimeType)

    val Favorites = AbstractField(Groups.FAVORITES, mimeType)

    val AutoAdd = AbstractField(Groups.AUTO_ADD, mimeType)

    // From protected SyncColumns
    val AccountName = AbstractField(Groups.ACCOUNT_NAME, mimeType)

    // From protected SyncColumns
    val AccountType = AbstractField(Groups.ACCOUNT_TYPE, mimeType)

    override val fields = setOf(Id, Title, ReadOnly, Favorites, AutoAdd, AccountName, AccountType)
}

class GroupMembershipFields : FieldSet(MimeType.GROUP_MEMBERSHIP) {

    @JvmField
    val GroupId = AbstractField(CommonDataKinds.GroupMembership.GROUP_ROW_ID, mimeType)

    override val fields = setOf(GroupId)
}

class ImFields : FieldSet(MimeType.IM) {

    @JvmField
    val Protocol = AbstractField(CommonDataKinds.Im.PROTOCOL, mimeType)

    @JvmField
    val CustomProtocol = AbstractField(CommonDataKinds.Im.CUSTOM_PROTOCOL, mimeType)

    @JvmField
    val Data = AbstractField(CommonDataKinds.Im.DATA, mimeType)

    override val fields = setOf(Protocol, CustomProtocol, Data)
}

class NameFields : FieldSet(MimeType.NAME) {

    @JvmField
    val DisplayName = AbstractField(CommonDataKinds.StructuredName.DISPLAY_NAME, mimeType)

    @JvmField
    val GivenName = AbstractField(CommonDataKinds.StructuredName.GIVEN_NAME, mimeType)

    @JvmField
    val MiddleName = AbstractField(CommonDataKinds.StructuredName.MIDDLE_NAME, mimeType)

    @JvmField
    val FamilyName = AbstractField(CommonDataKinds.StructuredName.FAMILY_NAME, mimeType)

    @JvmField
    val Prefix = AbstractField(CommonDataKinds.StructuredName.PREFIX, mimeType)

    @JvmField
    val Suffix = AbstractField(CommonDataKinds.StructuredName.SUFFIX, mimeType)

    @JvmField
    val PhoneticGivenName =
        AbstractField(CommonDataKinds.StructuredName.PHONETIC_GIVEN_NAME, mimeType)

    @JvmField
    val PhoneticMiddleName =
        AbstractField(CommonDataKinds.StructuredName.PHONETIC_MIDDLE_NAME, mimeType)

    @JvmField
    val PhoneticFamilyName =
        AbstractField(CommonDataKinds.StructuredName.PHONETIC_FAMILY_NAME, mimeType)

    override val fields = setOf(
        DisplayName,
        GivenName, MiddleName, FamilyName,
        Prefix, Suffix,
        PhoneticGivenName, PhoneticMiddleName, PhoneticFamilyName
    )
}

class NicknameFields : FieldSet(MimeType.NICKNAME) {

    @JvmField
    val Name = AbstractField(CommonDataKinds.Nickname.NAME, mimeType)

    override val fields = setOf(Name)
}

class NoteFields : FieldSet(MimeType.NOTE) {

    @JvmField
    val Note = AbstractField(CommonDataKinds.Note.NOTE, mimeType)

    override val fields = setOf(Note)
}

class OptionsFields : FieldSet(UNKNOWN) {

    internal val Id = AbstractField(Data._ID, mimeType)

    @JvmField
    val Starred = AbstractField(Data.STARRED, mimeType)

    @JvmField
    val TimesContacted = AbstractField(Data.TIMES_CONTACTED, mimeType)

    @JvmField
    val LastTimeContacted = AbstractField(Data.LAST_TIME_CONTACTED, mimeType)

    @JvmField
    val CustomRingtone = AbstractField(Data.CUSTOM_RINGTONE, mimeType)

    @JvmField
    val SendToVoicemail = AbstractField(Data.SEND_TO_VOICEMAIL, mimeType)

    override val fields = setOf(
        Id, Starred, TimesContacted, LastTimeContacted, CustomRingtone, SendToVoicemail
    )
}

class OrganizationFields : FieldSet(MimeType.ORGANIZATION) {

    @JvmField
    val Company = AbstractField(CommonDataKinds.Organization.COMPANY, mimeType)

    @JvmField
    val Title = AbstractField(CommonDataKinds.Organization.TITLE, mimeType)

    @JvmField
    val Department = AbstractField(CommonDataKinds.Organization.DEPARTMENT, mimeType)

    @JvmField
    val JobDescription = AbstractField(CommonDataKinds.Organization.JOB_DESCRIPTION, mimeType)

    @JvmField
    val OfficeLocation = AbstractField(CommonDataKinds.Organization.OFFICE_LOCATION, mimeType)

    @JvmField
    val Symbol = AbstractField(CommonDataKinds.Organization.SYMBOL, mimeType)

    @JvmField
    val PhoneticName = AbstractField(CommonDataKinds.Organization.PHONETIC_NAME, mimeType)

    override val fields = setOf(
        Company, Title, Department, JobDescription, OfficeLocation, Symbol, PhoneticName
    )
}

class PhoneFields : FieldSet(MimeType.PHONE) {

    @JvmField
    val Type = AbstractField(CommonDataKinds.Phone.TYPE, mimeType)

    @JvmField
    val Label = AbstractField(CommonDataKinds.Phone.LABEL, mimeType)

    @JvmField
    val Number = AbstractField(CommonDataKinds.Phone.NUMBER, mimeType)

    @JvmField
    val NormalizedNumber = AbstractField(CommonDataKinds.Phone.NORMALIZED_NUMBER, mimeType)

    override val fields = setOf(Type, Label, Number, NormalizedNumber)
}

internal class PhotoFields : FieldSet(MimeType.PHOTO) {

    val PhotoFileId = AbstractField(CommonDataKinds.Photo.PHOTO_FILE_ID, mimeType)

    val PhotoThumbnail = AbstractField(CommonDataKinds.Photo.PHOTO, mimeType)

    override val fields = setOf(PhotoFileId, PhotoThumbnail)
}

class RawContactFields : FieldSet(UNKNOWN) {

    val Id = AbstractField(Data.RAW_CONTACT_ID, mimeType)

    override val fields = setOf(Id)
}

/**
 * Fields for RawContacts table operations.
 */
object RawContactsFields : FieldSet(UNKNOWN) {

    val Id = AbstractField(RawContacts._ID, mimeType)

    val ContactId = AbstractField(RawContacts.CONTACT_ID, mimeType)

    val DisplayNamePrimary = AbstractField(RawContacts.DISPLAY_NAME_PRIMARY, mimeType)

    val DisplayNameAlt = AbstractField(RawContacts.DISPLAY_NAME_ALTERNATIVE, mimeType)

    // From protected SyncColumns
    val AccountName = AbstractField(RawContacts.ACCOUNT_NAME, mimeType)

    // From protected SyncColumns
    val AccountType = AbstractField(RawContacts.ACCOUNT_TYPE, mimeType)

    override val fields = setOf(
        Id, ContactId, DisplayNamePrimary, DisplayNameAlt, AccountName, AccountType
    )
}

class RelationFields : FieldSet(MimeType.RELATION) {

    @JvmField
    val Type = AbstractField(CommonDataKinds.Relation.TYPE, mimeType)

    @JvmField
    val Label = AbstractField(CommonDataKinds.Relation.LABEL, mimeType)

    @JvmField
    val Name = AbstractField(CommonDataKinds.Relation.NAME, mimeType)

    override val fields = setOf(Type, Label, Name)
}

class SipAddressFields : FieldSet(MimeType.SIP_ADDRESS) {

    @JvmField
    val SipAddress = AbstractField(CommonDataKinds.SipAddress.SIP_ADDRESS, mimeType)

    override val fields = setOf(SipAddress)
}

class WebsiteFields : FieldSet(MimeType.WEBSITE) {

    @JvmField
    val Url = AbstractField(CommonDataKinds.Website.URL, mimeType)

    override val fields = setOf(Url)
}
