@file:Suppress("PropertyName", "MemberVisibilityCanBePrivate", "unused")

package com.vestrel00.contacts

import android.annotation.TargetApi
import android.os.Build
import android.provider.BaseColumns
import android.provider.ContactsContract
import android.provider.ContactsContract.Data
import android.provider.ContactsContract.RawContacts
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.MimeType.SIP_ADDRESS
import com.vestrel00.contacts.entities.MimeType.UNKNOWN
import android.provider.ContactsContract.CommonDataKinds.Email as EmailColumns
import android.provider.ContactsContract.CommonDataKinds.Event as EventColumns
import android.provider.ContactsContract.CommonDataKinds.GroupMembership as GroupMembershipColumns
import android.provider.ContactsContract.CommonDataKinds.Im as ImColumns
import android.provider.ContactsContract.CommonDataKinds.Nickname as NicknameColumns
import android.provider.ContactsContract.CommonDataKinds.Note as NoteColumns
import android.provider.ContactsContract.CommonDataKinds.Organization as CompanyColumns
import android.provider.ContactsContract.CommonDataKinds.Phone as PhoneColumns
import android.provider.ContactsContract.CommonDataKinds.Photo as PhotoColumns
import android.provider.ContactsContract.CommonDataKinds.Relation as RelationColumns
import android.provider.ContactsContract.CommonDataKinds.SipAddress as SipAddressColumns
import android.provider.ContactsContract.CommonDataKinds.StructuredName as NameColumns
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal as AddressColumns
import android.provider.ContactsContract.CommonDataKinds.Website as WebsiteColumns
import android.provider.ContactsContract.Groups as GroupColumns

/**
 * Represents a database field(s) / column(s) that maps to a Contact attribute.
 */
sealed class Field

/**
 * Represents a database field / column that maps to a Contact attribute.
 */
class AbstractField internal constructor(
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
 * Contains all Contact Data table [Field]s.
 *
 * ## Developer Notes
 *
 * This is not named "DataFields" for several reasons;
 *
 * 1. Consumers are only exposed Data table fields. There is no need for them to know the fields
 *    of the other tables.
 * 2. Fields is shorter than DataFields. This is important for keeping lines short.
 */
// Note: All of this is condensed inside one file / class for (Kotlin & Java) consumer convenience.
// The structure of this class and the Kotlin language features that are used here are to cater to
// both Java and Kotlin consumers, whilst still attempting to keep the code structured in idiomatic
// Kotlin fashion (as much as possible).
object Fields {

    @JvmField
    val Address = AddressFields()

    // Do not add this to AllFields as this is a different table from the Data table.
    internal val AggregationExceptions = AggregationExceptionsFields()

    @JvmField
    val Company = CompanyFields()

    @JvmField
    val Contact = ContactFields()

    // Do not add this to AllFields as this is a different table from the Data table.
    internal val Contacts = ContactsFields()

    @JvmField
    val Email = EmailFields()

    @JvmField
    val Event = EventFields()

    // Do not add this to AllFields as this is a different table from the Data table.
    internal val Group = GroupFields()

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
    val Phone = PhoneFields()

    // Do not add Photo to AllFields even though it is part of the Data table.
    internal val Photo = PhotoFields()

    @JvmField
    val RawContact = RawContactFields()

    // Do not add this to AllFields as this is a different table from the Data table.
    internal val RawContacts = RawContactsFields()

    @JvmField
    val Relation = RelationFields()

    @JvmField
    val SipAddress = SipAddressFields()

    @JvmField
    val Website = WebsiteFields()

    internal val Required = Required()

    /**
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
     * Same as [All] except this is safe for matching in queries. This is useful for matching text
     * that the user is typing in a search field.
     */
    @JvmField
    val AllForMatching = AllForMatchingFields()
}

class AllFields : FieldSet(UNKNOWN) {
    override val fields: Set<AbstractField> = mutableSetOf<AbstractField>().apply {
        addAll(Fields.Address.fields)
        addAll(Fields.Company.fields)
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
        addAll(Fields.Phone.fields)
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
        addAll(Fields.Company.fields)
        add(Fields.Contact.DisplayName)
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
        addAll(Fields.Phone.fields.asSequence().minus(Fields.Phone.Type))
        // addAll(Fields.RawContact.fields)
        addAll(Fields.Relation.fields.asSequence().minus(Fields.Relation.Type))
        // addAll(Fields.Required.fields)
        addAll(Fields.SipAddress.fields)
        addAll(Fields.Website.fields)
    }.toSet()
}

/**
 * All of the Data table fields required when constructing Data entities retrieved from a Query.
 */
internal class Required : FieldSet(UNKNOWN) {
    override val fields: Set<AbstractField> = setOf(
        Fields.Id,
        Fields.Contact.Id,
        Fields.RawContact.Id,
        Fields.IsPrimary,
        Fields.IsSuperPrimary,
        Fields.MimeType
    )
}

class AddressFields : FieldSet(MimeType.ADDRESS) {

    @JvmField
    val Type = AbstractField(AddressColumns.TYPE, mimeType)

    @JvmField
    val Label = AbstractField(AddressColumns.LABEL, mimeType)

    @JvmField
    val FormattedAddress = AbstractField(AddressColumns.FORMATTED_ADDRESS, mimeType)

    @JvmField
    val Street = AbstractField(AddressColumns.STREET, mimeType)

    @JvmField
    val PoBox = AbstractField(AddressColumns.POBOX, mimeType)

    @JvmField
    val Neighborhood = AbstractField(AddressColumns.NEIGHBORHOOD, mimeType)

    @JvmField
    val City = AbstractField(AddressColumns.CITY, mimeType)

    @JvmField
    val Region = AbstractField(AddressColumns.REGION, mimeType)

    @JvmField
    val PostCode = AbstractField(AddressColumns.POSTCODE, mimeType)

    @JvmField
    val Country = AbstractField(AddressColumns.COUNTRY, mimeType)

    override val fields = setOf(
        Type, Label, FormattedAddress,
        Street, PoBox, Neighborhood,
        City, Region, PostCode, Country
    )
}

/*
 * This and all of its fields are used for the AggregationsExceptions table operations!
 *
 * This is technically not the most correct place to put this but it is the simplest and most
 * convenient place.
 */
internal class AggregationExceptionsFields : FieldSet(UNKNOWN) {

    val Type = AbstractField(ContactsContract.AggregationExceptions.TYPE, mimeType)

    val RawContactId1 =
        AbstractField(ContactsContract.AggregationExceptions.RAW_CONTACT_ID1, mimeType)

    val RawContactId2 =
        AbstractField(ContactsContract.AggregationExceptions.RAW_CONTACT_ID2, mimeType)

    override val fields = setOf(Type, RawContactId1, RawContactId2)
}

class CompanyFields : FieldSet(MimeType.COMPANY) {

    @JvmField
    val Company = AbstractField(CompanyColumns.COMPANY, mimeType)

    @JvmField
    val Title = AbstractField(CompanyColumns.TITLE, mimeType)

    @JvmField
    val Department = AbstractField(CompanyColumns.DEPARTMENT, mimeType)

    @JvmField
    val JobDescription = AbstractField(CompanyColumns.JOB_DESCRIPTION, mimeType)

    @JvmField
    val OfficeLocation = AbstractField(CompanyColumns.OFFICE_LOCATION, mimeType)

    @JvmField
    val Symbol = AbstractField(CompanyColumns.SYMBOL, mimeType)

    @JvmField
    val PhoneticName = AbstractField(CompanyColumns.PHONETIC_NAME, mimeType)

    override val fields = setOf(
        Company, Title, Department, JobDescription, OfficeLocation,
        Symbol, PhoneticName
    )
}

class ContactFields : FieldSet(UNKNOWN) {

    // The Data.CONTACT_ID, which is not the same as the column name Contacts._ID. However, the
    // values are the same. This is only meant to be used for Data table operations.
    @JvmField
    val Id = AbstractField(Data.CONTACT_ID, mimeType)

    @JvmField
    val DisplayName = AbstractField(Data.DISPLAY_NAME_PRIMARY, mimeType)

    @JvmField
    val LastUpdatedTimestamp = AbstractField(Data.CONTACT_LAST_UPDATED_TIMESTAMP, mimeType)

    override val fields = setOf(Id, DisplayName, LastUpdatedTimestamp)
}

/*
 * This and all of its fields are used for the Contacts table operations!
 *
 * This is technically not the most correct place to put this but it is the simplest and most
 * convenient place.
 */
internal class ContactsFields : FieldSet(UNKNOWN) {

    val Id = AbstractField(ContactsContract.Contacts._ID, mimeType)

    val PhotoUri = AbstractField(ContactsContract.Contacts.PHOTO_URI, mimeType)

    val PhotoThumbnailUri =
        AbstractField(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI, mimeType)

    val PhotoFileId = AbstractField(ContactsContract.Contacts.PHOTO_FILE_ID, mimeType)

    val DisplayNameSource = AbstractField(ContactsContract.Contacts.DISPLAY_NAME_SOURCE, mimeType)

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    val NameRawContactId = AbstractField(ContactsContract.Contacts.NAME_RAW_CONTACT_ID, mimeType)

    override val fields = setOf(Id, PhotoUri, PhotoThumbnailUri, PhotoFileId)
}

class EmailFields : FieldSet(MimeType.EMAIL) {

    @JvmField
    val Type = AbstractField(EmailColumns.TYPE, mimeType)

    @JvmField
    val Label = AbstractField(EmailColumns.LABEL, mimeType)

    @JvmField
    val Address = AbstractField(EmailColumns.ADDRESS, mimeType)

    override val fields = setOf(Type, Label, Address)
}

class EventFields : FieldSet(MimeType.EVENT) {

    @JvmField
    val Type = AbstractField(EventColumns.TYPE, mimeType)

    @JvmField
    val Label = AbstractField(EventColumns.LABEL, mimeType)

    @JvmField
    val Date = AbstractField(EventColumns.START_DATE, mimeType)

    override val fields = setOf(Type, Label, Date)
}

/*
 * This and all of its fields are used for the Groups table operations!
 *
 * This is technically not the most correct place to put this but it is the simplest and most
 * convenient place.
 */
internal class GroupFields : FieldSet(UNKNOWN) {

    val Id = AbstractField(GroupColumns._ID, mimeType)

    val Title = AbstractField(GroupColumns.TITLE, mimeType)

    val ReadOnly = AbstractField(GroupColumns.GROUP_IS_READ_ONLY, mimeType)

    val Favorites = AbstractField(GroupColumns.FAVORITES, mimeType)

    val AutoAdd = AbstractField(GroupColumns.AUTO_ADD, mimeType)

    val AccountName = AbstractField(GroupColumns.ACCOUNT_NAME, mimeType)

    val AccountType = AbstractField(GroupColumns.ACCOUNT_TYPE, mimeType)

    override val fields = setOf(Id, Title, ReadOnly, Favorites, AutoAdd, AccountName, AccountType)
}

class GroupMembershipFields : FieldSet(MimeType.GROUP_MEMBERSHIP) {

    @JvmField
    val GroupId = AbstractField(GroupMembershipColumns.GROUP_ROW_ID, mimeType)

    override val fields = setOf(GroupId)
}

class ImFields : FieldSet(MimeType.IM) {

    @JvmField
    val Protocol = AbstractField(ImColumns.PROTOCOL, mimeType)

    @JvmField
    val CustomProtocol = AbstractField(ImColumns.CUSTOM_PROTOCOL, mimeType)

    @JvmField
    val Data = AbstractField(ImColumns.DATA, mimeType)

    override val fields = setOf(Protocol, CustomProtocol, Data)
}

class NameFields : FieldSet(MimeType.NAME) {

    @JvmField
    val DisplayName = AbstractField(NameColumns.DISPLAY_NAME, mimeType)

    @JvmField
    val GivenName = AbstractField(NameColumns.GIVEN_NAME, mimeType)

    @JvmField
    val MiddleName = AbstractField(NameColumns.MIDDLE_NAME, mimeType)

    @JvmField
    val FamilyName = AbstractField(NameColumns.FAMILY_NAME, mimeType)

    @JvmField
    val Prefix = AbstractField(NameColumns.PREFIX, mimeType)

    @JvmField
    val Suffix = AbstractField(NameColumns.SUFFIX, mimeType)

    @JvmField
    val PhoneticGivenName = AbstractField(NameColumns.PHONETIC_GIVEN_NAME, mimeType)

    @JvmField
    val PhoneticMiddleName = AbstractField(NameColumns.PHONETIC_MIDDLE_NAME, mimeType)

    @JvmField
    val PhoneticFamilyName = AbstractField(NameColumns.PHONETIC_FAMILY_NAME, mimeType)

    override val fields = setOf(
        DisplayName,
        GivenName, MiddleName, FamilyName,
        Prefix, Suffix,
        PhoneticGivenName, PhoneticMiddleName, PhoneticFamilyName
    )
}

class NicknameFields : FieldSet(MimeType.NICKNAME) {

    @JvmField
    val Name = AbstractField(NicknameColumns.NAME, mimeType)

    override val fields = setOf(Name)
}

class NoteFields : FieldSet(MimeType.NOTE) {

    @JvmField
    val Note = AbstractField(NoteColumns.NOTE, mimeType)

    override val fields = setOf(Note)
}

class OptionsFields : FieldSet(UNKNOWN) {

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
        Starred, TimesContacted, LastTimeContacted, CustomRingtone, SendToVoicemail
    )
}

class PhoneFields : FieldSet(MimeType.PHONE) {

    @JvmField
    val Type = AbstractField(PhoneColumns.TYPE, mimeType)

    @JvmField
    val Label = AbstractField(PhoneColumns.LABEL, mimeType)

    @JvmField
    val Number = AbstractField(PhoneColumns.NUMBER, mimeType)

    @JvmField
    val NormalizedNumber = AbstractField(PhoneColumns.NORMALIZED_NUMBER, mimeType)

    override val fields = setOf(Type, Label, Number, NormalizedNumber)
}

internal class PhotoFields : FieldSet(MimeType.PHOTO) {

    val PhotoFileId = AbstractField(PhotoColumns.PHOTO_FILE_ID, mimeType)

    val PhotoThumbnail = AbstractField(PhotoColumns.PHOTO, mimeType)

    override val fields = setOf(PhotoFileId, PhotoThumbnail)
}

class RawContactFields : FieldSet(UNKNOWN) {

    val Id = AbstractField(Data.RAW_CONTACT_ID, mimeType)

    override val fields = setOf(Id)
}

/*
 * This and all of its fields are used for the RawContacts table operations!
 *
 * This is technically not the most correct place to put this but it is the simplest and most
 * convenient place.
 */
internal class RawContactsFields : FieldSet(UNKNOWN) {

    val Id = AbstractField(RawContacts._ID, mimeType)

    val ContactId = AbstractField(RawContacts.CONTACT_ID, mimeType)

    val AccountName = AbstractField(RawContacts.ACCOUNT_NAME, mimeType)

    val AccountType = AbstractField(RawContacts.ACCOUNT_TYPE, mimeType)

    override val fields = setOf(Id, ContactId, AccountName, AccountType)
}

class RelationFields : FieldSet(MimeType.RELATION) {

    @JvmField
    val Type = AbstractField(RelationColumns.TYPE, mimeType)

    @JvmField
    val Label = AbstractField(RelationColumns.LABEL, mimeType)

    @JvmField
    val Name = AbstractField(RelationColumns.NAME, mimeType)

    override val fields = setOf(Type, Label, Name)
}

class SipAddressFields : FieldSet(SIP_ADDRESS) {

    @JvmField
    val SipAddress = AbstractField(SipAddressColumns.SIP_ADDRESS, mimeType)

    override val fields = setOf(SipAddress)
}

class WebsiteFields : FieldSet(MimeType.WEBSITE) {

    @JvmField
    val Url = AbstractField(WebsiteColumns.URL, mimeType)

    override val fields = setOf(Url)
}
