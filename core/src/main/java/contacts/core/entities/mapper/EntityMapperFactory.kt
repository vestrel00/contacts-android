package contacts.core.entities.mapper

import contacts.core.*
import contacts.core.entities.*
import contacts.core.entities.cursor.*
import contacts.core.entities.custom.CustomDataRegistry

// region EntityCursor<AbstractDataField>

internal fun CursorHolder<AbstractDataField>.addressMapper(): EntityMapper<Address> =
    AddressMapper(addressCursor())

internal fun CursorHolder<AbstractDataField>.dataContactsMapper():
        EntityMapper<Contact> = ContactMapper(
    dataContactsCursor(), dataContactsOptionsMapper()
)

internal fun CursorHolder<AbstractDataField>.emailMapper(): EntityMapper<Email> =
    EmailMapper(emailCursor())

internal fun CursorHolder<AbstractDataField>.eventMapper(): EntityMapper<Event> =
    EventMapper(eventCursor())

internal fun CursorHolder<AbstractDataField>.groupMembershipMapper(): EntityMapper<GroupMembership> =
    GroupMembershipMapper(groupMembershipCursor())

internal fun CursorHolder<AbstractDataField>.imMapper(): EntityMapper<Im> = ImMapper(imCursor())

internal fun CursorHolder<AbstractDataField>.nameMapper(): EntityMapper<Name> =
    NameMapper(nameCursor())

internal fun CursorHolder<AbstractDataField>.nicknameMapper(): EntityMapper<Nickname> =
    NicknameMapper(nicknameCursor())

internal fun CursorHolder<AbstractDataField>.noteMapper(): EntityMapper<Note> =
    NoteMapper(noteCursor())

internal fun CursorHolder<AbstractDataField>.dataContactsOptionsMapper(): EntityMapper<Options> =
    OptionsMapper(dataContactsOptionsCursor())

internal fun CursorHolder<AbstractDataField>.organizationMapper(): EntityMapper<Organization> =
    OrganizationMapper(organizationCursor())

internal fun CursorHolder<AbstractDataField>.phoneMapper(): EntityMapper<Phone> =
    PhoneMapper(phoneCursor())

// The receiver EntityCursor<AbstractDataField> is unused here. However, we should still have it
// so that the mapper is still tied to or coupled with the receiver.
@Suppress("Unused")
internal fun CursorHolder<AbstractDataField>.photoMapper(): EntityMapper<Photo> = PhotoMapper()

internal fun CursorHolder<AbstractDataField>.relationMapper(): EntityMapper<Relation> =
    RelationMapper(relationCursor())

internal fun CursorHolder<AbstractDataField>.sipAddressMapper(): EntityMapper<SipAddress> =
    SipAddressMapper(sipAddressCursor())

internal fun CursorHolder<AbstractDataField>.websiteMapper(): EntityMapper<Website> =
    WebsiteMapper(websiteCursor())

@Suppress("UNCHECKED_CAST")
internal fun <T : ImmutableDataEntity> CursorHolder<AbstractDataField>.entityMapperFor(
    mimeType: MimeType,
    customDataRegistry: CustomDataRegistry
): EntityMapper<T> = when (mimeType) {
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
    is MimeType.Custom -> {
        val customDataEntry = customDataRegistry.entryOf(mimeType)
        customDataEntry.mapperFactory
            .create(
                cursor,
                // Only include custom data fields assigned by this entry.
                customDataEntry.fieldSet.intersect(includeFields)
            )
    }
    MimeType.Unknown -> throw ContactsException(
        "No entity mapper for mime type ${mimeType.value}"
    )
} as EntityMapper<T>

// endregion

internal fun RawContactIdCursor.tempRawContactMapper(): EntityMapper<TempRawContact> =
    TempRawContactMapper(this)

internal fun CursorHolder<RawContactsField>.blankRawContactMapper(): EntityMapper<BlankRawContact> =
    BlankRawContactMapper(rawContactsCursor())

internal fun CursorHolder<RawContactsField>.rawContactsOptionsMapper(): EntityMapper<Options> =
    OptionsMapper(rawContactsOptionsCursor())

internal fun CursorHolder<ContactsField>.contactsMapper(): EntityMapper<Contact> =
    ContactMapper(contactsCursor(), optionsMapper())

internal fun CursorHolder<ContactsField>.optionsMapper(): EntityMapper<Options> =
    OptionsMapper(optionsCursor())

internal fun CursorHolder<GroupsField>.groupMapper(): EntityMapper<Group> =
    GroupMapper(groupsCursor())