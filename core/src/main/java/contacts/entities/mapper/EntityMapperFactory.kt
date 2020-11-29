package contacts.entities.mapper

import contacts.AbstractDataField
import contacts.ContactsField
import contacts.GroupsField
import contacts.RawContactsField
import contacts.entities.*
import contacts.entities.cursor.*

// region EntityCursor<AbstractDataField>

internal fun EntityCursor<AbstractDataField>.addressMapper(): EntityMapper<Address> =
    AddressMapper(addressCursor())

internal fun EntityCursor<AbstractDataField>.dataContactsMapper():
        EntityMapper<Contact> = ContactMapper(
    dataContactsCursor(), dataContactsOptionsMapper()
)

internal fun EntityCursor<AbstractDataField>.emailMapper(): EntityMapper<Email> =
    EmailMapper(emailCursor())

internal fun EntityCursor<AbstractDataField>.eventMapper(): EntityMapper<Event> =
    EventMapper(eventCursor())

internal fun EntityCursor<AbstractDataField>.groupMembershipMapper(): EntityMapper<GroupMembership> =
    GroupMembershipMapper(groupMembershipCursor())

internal fun EntityCursor<AbstractDataField>.imMapper(): EntityMapper<Im> = ImMapper(imCursor())

internal fun EntityCursor<AbstractDataField>.nameMapper(): EntityMapper<Name> =
    NameMapper(nameCursor())

internal fun EntityCursor<AbstractDataField>.nicknameMapper(): EntityMapper<Nickname> =
    NicknameMapper(nicknameCursor())

internal fun EntityCursor<AbstractDataField>.noteMapper(): EntityMapper<Note> =
    NoteMapper(noteCursor())

internal fun EntityCursor<AbstractDataField>.dataContactsOptionsMapper(): EntityMapper<Options> =
    OptionsMapper(dataContactsOptionsCursor())

internal fun EntityCursor<AbstractDataField>.organizationMapper(): EntityMapper<Organization> =
    OrganizationMapper(organizationCursor())

internal fun EntityCursor<AbstractDataField>.phoneMapper(): EntityMapper<Phone> =
    PhoneMapper(phoneCursor())

// The receiver EntityCursor<AbstractDataField> is unused here. However, we should still have it
// so that the mapper is still tied to or coupled with the receiver.
@Suppress("Unused")
internal fun EntityCursor<AbstractDataField>.photoMapper(): EntityMapper<Photo> = PhotoMapper()

internal fun EntityCursor<AbstractDataField>.relationMapper(): EntityMapper<Relation> =
    RelationMapper(relationCursor())

internal fun EntityCursor<AbstractDataField>.sipAddressMapper(): EntityMapper<SipAddress> =
    SipAddressMapper(sipAddressCursor())

internal fun EntityCursor<AbstractDataField>.websiteMapper(): EntityMapper<Website> =
    WebsiteMapper(websiteCursor())

@Suppress("UNCHECKED_CAST")
internal fun <T : CommonDataEntity> EntityCursor<AbstractDataField>.entityMapperFor(
    mimeType: MimeType
): EntityMapper<T> = when (mimeType) {
    MimeType.ADDRESS -> addressMapper()
    MimeType.EMAIL -> emailMapper()
    MimeType.EVENT -> eventMapper()
    MimeType.GROUP_MEMBERSHIP -> groupMembershipMapper()
    MimeType.IM -> imMapper()
    MimeType.NAME -> nameMapper()
    MimeType.NICKNAME -> nicknameMapper()
    MimeType.NOTE -> noteMapper()
    MimeType.ORGANIZATION -> organizationMapper()
    MimeType.PHONE -> phoneMapper()
    MimeType.PHOTO -> photoMapper()
    MimeType.RELATION -> relationMapper()
    MimeType.SIP_ADDRESS -> sipAddressMapper()
    MimeType.WEBSITE -> websiteMapper()
    MimeType.UNKNOWN -> throw UnsupportedOperationException(
        "No entity mapper for mime type $mimeType"
    )
} as EntityMapper<T>

// endregion


internal fun RawContactIdCursor.tempRawContactMapper(): EntityMapper<TempRawContact> =
    TempRawContactMapper(this)

internal fun EntityCursor<RawContactsField>.blankRawContactMapper(): EntityMapper<BlankRawContact> =
    BlankRawContactMapper(rawContactsCursor())

internal fun EntityCursor<RawContactsField>.rawContactsOptionsMapper(): EntityMapper<Options> =
    OptionsMapper(rawContactsOptionsCursor())

internal fun EntityCursor<ContactsField>.contactsMapper(): EntityMapper<Contact> =
    ContactMapper(contactsCursor(), optionsMapper())

internal fun EntityCursor<ContactsField>.optionsMapper(): EntityMapper<Options> =
    OptionsMapper(optionsCursor())

internal fun EntityCursor<GroupsField>.groupMapper(): EntityMapper<Group> =
    GroupMapper(groupsCursor())