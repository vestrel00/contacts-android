@file:Suppress("FunctionName")

package contacts.core.util

import contacts.core.*

// Provides a bunch of extensions for shortening construction of Where instances.
// AKA Kotlin DSL goodness! Yummy stuff! So delicious! Mouth-watering!

/*
 * ## DISCLAIMER! Elitist warning!
 *
 * Okay, look. Let me get real with you. Because I know someone will point this out. So, I'm
 * writing this comment to say that "I know, but I'm still doing it this way".
 *
 * I know... that I could reduce all of the extensions defined in this file in two;
 *
 * 1. inline fun <T : Field, S : FieldSet<T>> S.where(where: S.() -> Where<T>): Where<T> = where(this)
 * 2. inline fun <T : Field> T.where(where: T.() -> Where<T>): Where<T> = where(this)
 *
 * This would let consumers do stuff like,
 *
 * ```
 * .where{ Email.Address.where { startsWith("a") and endsWith("gmail.com") } }
 * ```
 *
 * However, do you see that small, but glaring imperfection? The ".where" is required because
 * functions have to have names (extensions are no exception)! Consumers might as well just do...
 *
 * ```
 * .where{ Email.Address.run { startsWith("a") and endsWith("gmail.com") } }
 * ```
 *
 * It's the same thing! Those functions is just run with a different name!
 *
 * So, I'm choosing to write a bunch of extensions to get "sugar, spice, and everything nice"!
 *
 * We can probably use annotations and code-generation to generation these functions for us.
 * However, I think it'd take more time writing the code generator than just writing these manually.
 *
 * ## Inline
 *
 * These syntactic sugar are unusable by Java users. Therefore, we should inline to not negatively
 * impact runtime performance.
 */

// region Data Table Fields

// region AddressFields

inline fun Fields.Address(where: AddressFields.() -> Where<AddressField>): Where<AddressField> =
    where(Address)

inline fun AddressFields.Type(where: AddressField.() -> Where<AddressField>): Where<AddressField> =
    where(Type)

inline fun AddressFields.Label(where: AddressField.() -> Where<AddressField>): Where<AddressField> =
    where(Label)

inline fun AddressFields.FormattedAddress(
    where: AddressField.() -> Where<AddressField>
): Where<AddressField> = where(FormattedAddress)

inline fun AddressFields.Street(where: AddressField.() -> Where<AddressField>): Where<AddressField> =
    where(Street)

inline fun AddressFields.PoBox(where: AddressField.() -> Where<AddressField>): Where<AddressField> =
    where(PoBox)

inline fun AddressFields.Neighborhood(
    where: AddressField.() -> Where<AddressField>
): Where<AddressField> = where(Neighborhood)

inline fun AddressFields.City(where: AddressField.() -> Where<AddressField>): Where<AddressField> =
    where(City)

inline fun AddressFields.Region(
    where: AddressField.() -> Where<AddressField>
): Where<AddressField> = where(Region)

inline fun AddressFields.PostCode(
    where: AddressField.() -> Where<AddressField>
): Where<AddressField> = where(PostCode)

inline fun AddressFields.Country(
    where: AddressField.() -> Where<AddressField>
): Where<AddressField> = where(Country)

// endregion

// region DataContactsFields

inline fun Fields.Contact(
    where: DataContactsFields.() -> Where<DataContactsField>
): Where<DataContactsField> = where(Contact)

inline fun DataContactsFields.Id(
    where: DataContactsField.() -> Where<DataContactsField>
): Where<DataContactsField> = where(Id)

inline fun DataContactsFields.DisplayNamePrimary(
    where: DataContactsField.() -> Where<DataContactsField>
): Where<DataContactsField> = where(DisplayNamePrimary)

inline fun DataContactsFields.DisplayNameAlt(
    where: DataContactsField.() -> Where<DataContactsField>
): Where<DataContactsField> = where(DisplayNameAlt)

inline fun DataContactsFields.LastUpdatedTimestamp(
    where: DataContactsField.() -> Where<DataContactsField>
): Where<DataContactsField> = where(LastUpdatedTimestamp)

// region DataContactsOptionsFields

inline fun DataContactsFields.Options(
    where: DataContactsOptionsFields.() -> Where<DataContactsField>
): Where<DataContactsField> = where(Options)

internal inline fun DataContactsOptionsFields.Id(
    where: DataContactsField.() -> Where<DataContactsField>
): Where<DataContactsField> = where(Id)

inline fun DataContactsOptionsFields.Starred(
    where: DataContactsField.() -> Where<DataContactsField>
): Where<DataContactsField> = where(Starred)

inline fun DataContactsOptionsFields.CustomRingtone(
    where: DataContactsField.() -> Where<DataContactsField>
): Where<DataContactsField> = where(CustomRingtone)

inline fun DataContactsOptionsFields.SendToVoicemail(
    where: DataContactsField.() -> Where<DataContactsField>
): Where<DataContactsField> = where(SendToVoicemail)

// endregion

inline fun DataContactsFields.PhotoUri(
    where: DataContactsField.() -> Where<DataContactsField>
): Where<DataContactsField> = where(PhotoUri)

inline fun DataContactsFields.PhotoThumbnailUri(
    where: DataContactsField.() -> Where<DataContactsField>
): Where<DataContactsField> = where(PhotoThumbnailUri)

inline fun DataContactsFields.HasPhoneNumber(
    where: DataContactsField.() -> Where<DataContactsField>
): Where<DataContactsField> = where(HasPhoneNumber)

// endregion

inline fun Fields.DataId(
    where: GenericDataField.() -> Where<GenericDataField>
): Where<GenericDataField> = where(DataId)

// region EmailFields

inline fun Fields.Email(where: EmailFields.() -> Where<EmailField>): Where<EmailField> =
    where(Email)

inline fun EmailFields.Type(where: EmailField.() -> Where<EmailField>): Where<EmailField> =
    where(Type)

inline fun EmailFields.Label(where: EmailField.() -> Where<EmailField>): Where<EmailField> =
    where(Label)

inline fun EmailFields.Address(where: EmailField.() -> Where<EmailField>): Where<EmailField> =
    where(Address)

// endregion

// region EventFields

inline fun Fields.Event(where: EventFields.() -> Where<EventField>): Where<EventField> =
    where(Event)

inline fun EventFields.Type(where: EventField.() -> Where<EventField>): Where<EventField> =
    where(Type)

inline fun EventFields.Label(where: EventField.() -> Where<EventField>): Where<EventField> =
    where(Label)

inline fun EventFields.Date(where: EventField.() -> Where<EventField>): Where<EventField> =
    where(Date)

// endregion

// region GroupMembershipFields

inline fun Fields.GroupMembership(
    where: GroupMembershipFields.() -> Where<GroupMembershipField>
): Where<GroupMembershipField> = where(GroupMembership)

inline fun GroupMembershipFields.GroupId(
    where: GroupMembershipField.() -> Where<GroupMembershipField>
): Where<GroupMembershipField> = where(GroupId)

// endregion

// region ImFields

inline fun Fields.Im(where: ImFields.() -> Where<ImField>): Where<ImField> =
    where(Im)

inline fun ImFields.Protocol(where: ImField.() -> Where<ImField>): Where<ImField> =
    where(Protocol)

inline fun ImFields.CustomProtocol(where: ImField.() -> Where<ImField>): Where<ImField> =
    where(CustomProtocol)

inline fun ImFields.Data(where: ImField.() -> Where<ImField>): Where<ImField> =
    where(Data)

// endregion

inline fun Fields.IsPrimary(
    where: GenericDataField.() -> Where<GenericDataField>
): Where<GenericDataField> = where(IsPrimary)

inline fun Fields.IsSuperPrimary(
    where: GenericDataField.() -> Where<GenericDataField>
): Where<GenericDataField> = where(IsSuperPrimary)

internal inline fun Fields.MimeType(
    where: GenericDataField.() -> Where<GenericDataField>
): Where<GenericDataField> = where(MimeType)

// region NameFields

inline fun Fields.Name(where: NameFields.() -> Where<NameField>): Where<NameField> =
    where(Name)

inline fun NameFields.DisplayName(where: NameField.() -> Where<NameField>): Where<NameField> =
    where(DisplayName)

inline fun NameFields.GivenName(where: NameField.() -> Where<NameField>): Where<NameField> =
    where(GivenName)

inline fun NameFields.MiddleName(where: NameField.() -> Where<NameField>): Where<NameField> =
    where(MiddleName)

inline fun NameFields.FamilyName(where: NameField.() -> Where<NameField>): Where<NameField> =
    where(FamilyName)

inline fun NameFields.Prefix(where: NameField.() -> Where<NameField>): Where<NameField> =
    where(Prefix)

inline fun NameFields.Suffix(where: NameField.() -> Where<NameField>): Where<NameField> =
    where(Suffix)

inline fun NameFields.PhoneticGivenName(where: NameField.() -> Where<NameField>): Where<NameField> =
    where(PhoneticGivenName)

inline fun NameFields.PhoneticMiddleName(
    where: NameField.() -> Where<NameField>
): Where<NameField> = where(PhoneticMiddleName)

inline fun NameFields.PhoneticFamilyName(
    where: NameField.() -> Where<NameField>
): Where<NameField> = where(PhoneticFamilyName)

// endregion

// region NicknameFields

inline fun Fields.Nickname(where: NicknameFields.() -> Where<NicknameField>): Where<NicknameField> =
    where(Nickname)

inline fun NicknameFields.Name(where: NicknameField.() -> Where<NicknameField>): Where<NicknameField> =
    where(Name)

// endregion

// region NoteFields

inline fun Fields.Note(where: NoteFields.() -> Where<NoteField>): Where<NoteField> =
    where(Note)

inline fun NoteFields.Note(where: NoteField.() -> Where<NoteField>): Where<NoteField> =
    where(Note)

// endregion

// region OrganizationFields

inline fun Fields.Organization(
    where: OrganizationFields.() -> Where<OrganizationField>
): Where<OrganizationField> = where(Organization)

inline fun OrganizationFields.Company(
    where: OrganizationField.() -> Where<OrganizationField>
): Where<OrganizationField> = where(Company)

inline fun OrganizationFields.Title(
    where: OrganizationField.() -> Where<OrganizationField>
): Where<OrganizationField> = where(Title)

inline fun OrganizationFields.Department(
    where: OrganizationField.() -> Where<OrganizationField>
): Where<OrganizationField> = where(Department)

inline fun OrganizationFields.JobDescription(
    where: OrganizationField.() -> Where<OrganizationField>
): Where<OrganizationField> = where(JobDescription)

inline fun OrganizationFields.OfficeLocation(
    where: OrganizationField.() -> Where<OrganizationField>
): Where<OrganizationField> = where(OfficeLocation)

inline fun OrganizationFields.Symbol(
    where: OrganizationField.() -> Where<OrganizationField>
): Where<OrganizationField> = where(Symbol)

inline fun OrganizationFields.PhoneticName(
    where: OrganizationField.() -> Where<OrganizationField>
): Where<OrganizationField> = where(PhoneticName)

// endregion

// region PhoneFields

inline fun Fields.Phone(where: PhoneFields.() -> Where<PhoneField>): Where<PhoneField> =
    where(Phone)

inline fun PhoneFields.Type(where: PhoneField.() -> Where<PhoneField>): Where<PhoneField> =
    where(Type)

inline fun PhoneFields.Label(where: PhoneField.() -> Where<PhoneField>): Where<PhoneField> =
    where(Label)

inline fun PhoneFields.Number(where: PhoneField.() -> Where<PhoneField>): Where<PhoneField> =
    where(Number)

inline fun PhoneFields.NormalizedNumber(where: PhoneField.() -> Where<PhoneField>): Where<PhoneField> =
    where(NormalizedNumber)

// endregion

// region PhotoFields

inline fun Fields.Photo(where: PhotoFields.() -> Where<PhotoField>): Where<PhotoField> =
    where(Photo)

internal inline fun PhotoFields.PhotoFileId(where: PhotoField.() -> Where<PhotoField>): Where<PhotoField> =
    where(PhotoFileId)

internal inline fun PhotoFields.PhotoThumbnail(where: PhotoField.() -> Where<PhotoField>): Where<PhotoField> =
    where(PhotoThumbnail)

// endregion

// region DataRawContactsFields

inline fun Fields.RawContact(
    where: DataRawContactsFields.() -> Where<DataRawContactsField>
): Where<DataRawContactsField> = where(RawContact)

inline fun DataRawContactsFields.Id(
    where: DataRawContactsField.() -> Where<DataRawContactsField>
): Where<DataRawContactsField> = where(Id)


// endregion

// region RelationFields

inline fun Fields.Relation(where: RelationFields.() -> Where<RelationField>): Where<RelationField> =
    where(Relation)

inline fun RelationFields.Type(where: RelationField.() -> Where<RelationField>): Where<RelationField> =
    where(Type)

inline fun RelationFields.Label(where: RelationField.() -> Where<RelationField>): Where<RelationField> =
    where(Label)

inline fun RelationFields.Name(where: RelationField.() -> Where<RelationField>): Where<RelationField> =
    where(Name)

// endregion

// region SipAddressFields

inline fun Fields.SipAddress(
    where: SipAddressFields.() -> Where<SipAddressField>
): Where<SipAddressField> = where(SipAddress)

inline fun SipAddressFields.SipAddress(
    where: SipAddressField.() -> Where<SipAddressField>
): Where<SipAddressField> = where(SipAddress)

// endregion

// region WebsiteFields

inline fun Fields.Website(
    where: WebsiteFields.() -> Where<WebsiteField>
): Where<WebsiteField> = where(Website)

inline fun WebsiteFields.Url(
    where: WebsiteField.() -> Where<WebsiteField>
): Where<WebsiteField> = where(Url)

// endregion

// endregion

// region AggregationExceptions Table Fields

internal inline fun AggregationExceptionsFields.Type(
    where: AggregationExceptionsField.() -> Where<AggregationExceptionsField>
): Where<AggregationExceptionsField> = where(Type)

internal inline fun AggregationExceptionsFields.RawContactId1(
    where: AggregationExceptionsField.() -> Where<AggregationExceptionsField>
): Where<AggregationExceptionsField> = where(RawContactId1)

internal inline fun AggregationExceptionsFields.RawContactId2(
    where: AggregationExceptionsField.() -> Where<AggregationExceptionsField>
): Where<AggregationExceptionsField> = where(RawContactId2)

// endregion

// region Contacts Table Fields

inline fun ContactsFields.Id(
    where: ContactsField.() -> Where<ContactsField>
): Where<ContactsField> = where(Id)

inline fun ContactsFields.DisplayNamePrimary(
    where: ContactsField.() -> Where<ContactsField>
): Where<ContactsField> = where(DisplayNamePrimary)

inline fun ContactsFields.DisplayNameAlt(
    where: ContactsField.() -> Where<ContactsField>
): Where<ContactsField> = where(DisplayNameAlt)

inline fun ContactsFields.LastUpdatedTimestamp(
    where: ContactsField.() -> Where<ContactsField>
): Where<ContactsField> = where(LastUpdatedTimestamp)

// region ContactsOptionsFields

inline fun ContactsFields.Options(
    where: ContactsOptionsFields.() -> Where<ContactsField>
): Where<ContactsField> = where(Options)

internal inline fun ContactsOptionsFields.Id(
    where: ContactsField.() -> Where<ContactsField>
): Where<ContactsField> = where(Id)

inline fun ContactsOptionsFields.Starred(
    where: ContactsField.() -> Where<ContactsField>
): Where<ContactsField> = where(Starred)

inline fun ContactsOptionsFields.CustomRingtone(
    where: ContactsField.() -> Where<ContactsField>
): Where<ContactsField> = where(CustomRingtone)

inline fun ContactsOptionsFields.SendToVoicemail(
    where: ContactsField.() -> Where<ContactsField>
): Where<ContactsField> = where(SendToVoicemail)

// endregion

internal inline fun ContactsFields.DisplayNameSource(
    where: ContactsField.() -> Where<ContactsField>
): Where<ContactsField> = where(DisplayNameSource)

internal inline fun ContactsFields.NameRawContactId(
    where: ContactsField.() -> Where<ContactsField>
): Where<ContactsField> = where(NameRawContactId)

inline fun ContactsFields.PhotoUri(
    where: ContactsField.() -> Where<ContactsField>
): Where<ContactsField> = where(PhotoUri)

inline fun ContactsFields.PhotoThumbnailUri(
    where: ContactsField.() -> Where<ContactsField>
): Where<ContactsField> = where(PhotoThumbnailUri)

internal inline fun ContactsFields.PhotoFileId(
    where: ContactsField.() -> Where<ContactsField>
): Where<ContactsField> = where(PhotoFileId)

inline fun ContactsFields.HasPhoneNumber(
    where: ContactsField.() -> Where<ContactsField>
): Where<ContactsField> = where(HasPhoneNumber)

// endregion

// region Groups Table Fields

inline fun GroupsFields.Id(where: GroupsField.() -> Where<GroupsField>): Where<GroupsField> =
    where(Id)

inline fun GroupsFields.SystemId(where: GroupsField.() -> Where<GroupsField>): Where<GroupsField> =
    where(SystemId)

inline fun GroupsFields.Title(where: GroupsField.() -> Where<GroupsField>): Where<GroupsField> =
    where(Title)

inline fun GroupsFields.ReadOnly(where: GroupsField.() -> Where<GroupsField>): Where<GroupsField> =
    where(ReadOnly)

inline fun GroupsFields.Favorites(where: GroupsField.() -> Where<GroupsField>): Where<GroupsField> =
    where(Favorites)

inline fun GroupsFields.AutoAdd(where: GroupsField.() -> Where<GroupsField>): Where<GroupsField> =
    where(AutoAdd)

inline fun GroupsFields.AccountName(
    where: GroupsField.() -> Where<GroupsField>
): Where<GroupsField> = where(Id)

inline fun GroupsFields.AccountType(
    where: GroupsField.() -> Where<GroupsField>
): Where<GroupsField> = where(AccountType)

// endregion

// region RawContacts Table Fields

inline fun RawContactsFields.Id(
    where: RawContactsField.() -> Where<RawContactsField>
): Where<RawContactsField> = where(Id)

inline fun RawContactsFields.ContactId(
    where: RawContactsField.() -> Where<RawContactsField>
): Where<RawContactsField> = where(ContactId)

inline fun RawContactsFields.DisplayNamePrimary(
    where: RawContactsField.() -> Where<RawContactsField>
): Where<RawContactsField> = where(DisplayNamePrimary)

inline fun RawContactsFields.DisplayNameAlt(
    where: RawContactsField.() -> Where<RawContactsField>
): Where<RawContactsField> = where(DisplayNameAlt)

inline fun RawContactsFields.AccountName(
    where: RawContactsField.() -> Where<RawContactsField>
): Where<RawContactsField> = where(AccountName)

inline fun RawContactsFields.AccountType(
    where: RawContactsField.() -> Where<RawContactsField>
): Where<RawContactsField> = where(AccountType)

// region RawContactsOptionsFields

inline fun RawContactsFields.Options(
    where: RawContactsOptionsFields.() -> Where<RawContactsField>
): Where<RawContactsField> = where(Options)

internal inline fun RawContactsOptionsFields.Id(
    where: RawContactsField.() -> Where<RawContactsField>
): Where<RawContactsField> = where(Id)

inline fun RawContactsOptionsFields.Starred(
    where: RawContactsField.() -> Where<RawContactsField>
): Where<RawContactsField> = where(Starred)

inline fun RawContactsOptionsFields.CustomRingtone(
    where: RawContactsField.() -> Where<RawContactsField>
): Where<RawContactsField> = where(CustomRingtone)

inline fun RawContactsOptionsFields.SendToVoicemail(
    where: RawContactsField.() -> Where<RawContactsField>
): Where<RawContactsField> = where(SendToVoicemail)

// endregion

// endregion