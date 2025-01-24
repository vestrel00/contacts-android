package contacts.core.entities.mapper

import contacts.core.AbstractDataField
import contacts.core.BlockedNumbersField
import contacts.core.ContactsException
import contacts.core.ContactsField
import contacts.core.GroupsField
import contacts.core.RawContactsField
import contacts.core.SimContactsField
import contacts.core.entities.*
import contacts.core.entities.cursor.CursorHolder
import contacts.core.entities.cursor.addressCursor
import contacts.core.entities.cursor.blockedNumbersCursor
import contacts.core.entities.cursor.contactsCursor
import contacts.core.entities.cursor.dataContactsCursor
import contacts.core.entities.cursor.dataContactsOptionsCursor
import contacts.core.entities.cursor.emailCursor
import contacts.core.entities.cursor.eventCursor
import contacts.core.entities.cursor.groupMembershipCursor
import contacts.core.entities.cursor.groupsCursor
import contacts.core.entities.cursor.imCursor
import contacts.core.entities.cursor.nameCursor
import contacts.core.entities.cursor.nicknameCursor
import contacts.core.entities.cursor.noteCursor
import contacts.core.entities.cursor.optionsCursor
import contacts.core.entities.cursor.organizationCursor
import contacts.core.entities.cursor.phoneCursor
import contacts.core.entities.cursor.photoCursor
import contacts.core.entities.cursor.rawContactsCursor
import contacts.core.entities.cursor.rawContactsOptionsCursor
import contacts.core.entities.cursor.relationCursor
import contacts.core.entities.cursor.simContactCursor
import contacts.core.entities.cursor.sipAddressCursor
import contacts.core.entities.cursor.websiteCursor
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.intersect

// region EntityCursor<AbstractDataField>

internal fun CursorHolder<AbstractDataField>.addressMapper(): DataEntityMapper<Address> =
    AddressMapper(addressCursor())

internal fun CursorHolder<AbstractDataField>.dataContactsMapper():
        EntityMapper<Contact> = ContactMapper(
    dataContactsCursor(), dataContactsOptionsMapper()
)

internal fun CursorHolder<AbstractDataField>.emailMapper(): DataEntityMapper<Email> =
    EmailMapper(emailCursor())

internal fun CursorHolder<AbstractDataField>.eventMapper(): DataEntityMapper<Event> =
    EventMapper(eventCursor())

internal fun CursorHolder<AbstractDataField>.groupMembershipMapper(): DataEntityMapper<GroupMembership> =
    GroupMembershipMapper(groupMembershipCursor())

internal fun CursorHolder<AbstractDataField>.imMapper(): DataEntityMapper<@Suppress("Deprecation") Im> =
    ImMapper(imCursor())

internal fun CursorHolder<AbstractDataField>.nameMapper(): DataEntityMapper<Name> =
    NameMapper(nameCursor())

internal fun CursorHolder<AbstractDataField>.nicknameMapper(): DataEntityMapper<Nickname> =
    NicknameMapper(nicknameCursor())

internal fun CursorHolder<AbstractDataField>.noteMapper(): DataEntityMapper<Note> =
    NoteMapper(noteCursor())

internal fun CursorHolder<AbstractDataField>.dataContactsOptionsMapper(): EntityMapper<Options> =
    OptionsMapper(dataContactsOptionsCursor())

internal fun CursorHolder<AbstractDataField>.organizationMapper(): DataEntityMapper<Organization> =
    OrganizationMapper(organizationCursor())

internal fun CursorHolder<AbstractDataField>.phoneMapper(): DataEntityMapper<Phone> =
    PhoneMapper(phoneCursor())

internal fun CursorHolder<AbstractDataField>.photoMapper(): DataEntityMapper<Photo> =
    PhotoMapper(photoCursor())

internal fun CursorHolder<AbstractDataField>.relationMapper(): DataEntityMapper<Relation> =
    RelationMapper(relationCursor())

internal fun CursorHolder<AbstractDataField>.sipAddressMapper(): DataEntityMapper<@Suppress("Deprecation") SipAddress> =
    SipAddressMapper(sipAddressCursor())

internal fun CursorHolder<AbstractDataField>.websiteMapper(): DataEntityMapper<Website> =
    WebsiteMapper(websiteCursor())

@Suppress("UNCHECKED_CAST")
internal fun <T : ExistingDataEntity> CursorHolder<AbstractDataField>.dataEntityMapperFor(
    mimeType: MimeType,
    customDataRegistry: CustomDataRegistry
): DataEntityMapper<T> = when (mimeType) {
    // Check custom mimetype first to allow for overriding built-in mimetypes.
    is MimeType.Custom -> {
        val customDataEntry = customDataRegistry.entryOf(mimeType)
        customDataEntry.mapperFactory
            .create(
                cursor,
                // Only include custom data fields assigned by this entry.
                includeFields?.let(customDataEntry.fieldSet::intersect)
            )
    }
    MimeType.Address -> addressMapper()
    MimeType.Email -> emailMapper()
    MimeType.Event -> eventMapper()
    MimeType.GroupMembership -> groupMembershipMapper()
    MimeType.Im -> imMapper()
    MimeType.Name -> nameMapper()
    MimeType.Nickname -> nicknameMapper()
    MimeType.Note -> noteMapper()
    MimeType.Organization -> organizationMapper()
    MimeType.Phone -> phoneMapper()
    MimeType.Photo -> photoMapper()
    MimeType.Relation -> relationMapper()
    MimeType.SipAddress -> sipAddressMapper()
    MimeType.Website -> websiteMapper()
    MimeType.Unknown -> throw ContactsException(
        "No entity mapper for mime type ${mimeType.value}"
    )
} as DataEntityMapper<T>

// endregion

internal fun CursorHolder<RawContactsField>.tempRawContactMapper(): EntityMapper<TempRawContact> =
    TempRawContactMapper(rawContactsCursor(), rawContactsOptionsMapper())

internal fun CursorHolder<RawContactsField>.rawContactsOptionsMapper(): EntityMapper<Options> =
    OptionsMapper(rawContactsOptionsCursor())

internal fun CursorHolder<ContactsField>.contactsMapper(): EntityMapper<Contact> =
    ContactMapper(contactsCursor(), optionsMapper())

internal fun CursorHolder<ContactsField>.optionsMapper(): EntityMapper<Options> =
    OptionsMapper(optionsCursor())

internal fun CursorHolder<GroupsField>.groupMapper(): EntityMapper<Group> =
    GroupMapper(groupsCursor())

internal fun CursorHolder<BlockedNumbersField>.blockedNumberMapper(): EntityMapper<BlockedNumber> =
    BlockedNumberMapper(blockedNumbersCursor())

internal fun CursorHolder<SimContactsField>.simContactMapper(): EntityMapper<SimContact> =
    SimContactMapper(simContactCursor())