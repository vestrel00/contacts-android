package contacts.core.entities.cursor

import contacts.core.*
import contacts.core.entities.custom.CustomDataRegistry

// region AbstractDataField

internal fun CursorHolder<AbstractDataField>.addressCursor() =
    AddressCursor(cursor, Fields.Address.intersect(includeFields))

internal fun CursorHolder<AbstractDataField>.dataContactsCursor() =
    DataContactsCursor(cursor, Fields.Contact.intersect(includeFields))

internal fun <T : AbstractDataField> CursorHolder<T>.dataCursor() =
    DataCursor(cursor, includeFields)

internal fun CursorHolder<AbstractDataField>.emailCursor() =
    EmailCursor(cursor, Fields.Email.intersect(includeFields))

internal fun CursorHolder<AbstractDataField>.eventCursor() =
    EventCursor(cursor, Fields.Event.intersect(includeFields))

internal fun CursorHolder<AbstractDataField>.groupMembershipCursor() =
    GroupMembershipCursor(cursor, Fields.GroupMembership.intersect(includeFields))

internal fun CursorHolder<AbstractDataField>.imCursor() =
    ImCursor(cursor, Fields.Im.intersect(includeFields))

internal fun CursorHolder<AbstractDataField>.mimeTypeCursor(
    customDataRegistry: CustomDataRegistry
) = MimeTypeCursor(cursor, customDataRegistry)

internal fun CursorHolder<AbstractDataField>.nameCursor() =
    NameCursor(cursor, Fields.Name.intersect(includeFields))

internal fun CursorHolder<AbstractDataField>.nicknameCursor() =
    NicknameCursor(cursor, Fields.Nickname.intersect(includeFields))

internal fun CursorHolder<AbstractDataField>.noteCursor() =
    NoteCursor(cursor, Fields.Note.intersect(includeFields))

internal fun CursorHolder<AbstractDataField>.dataContactsOptionsCursor() =
    DataContactsOptionsCursor(cursor, includeFields)

internal fun CursorHolder<AbstractDataField>.organizationCursor() =
    OrganizationCursor(cursor, Fields.Organization.intersect(includeFields))

internal fun CursorHolder<AbstractDataField>.phoneCursor() =
    PhoneCursor(cursor, Fields.Phone.intersect(includeFields))

internal fun CursorHolder<AbstractDataField>.photoCursor() =
    PhotoCursor(cursor, Fields.Photo.intersect(includeFields))

internal fun CursorHolder<AbstractDataField>.relationCursor() =
    RelationCursor(cursor, Fields.Relation.intersect(includeFields))

internal fun CursorHolder<AbstractDataField>.sipAddressCursor() =
    SipAddressCursor(cursor, Fields.SipAddress.intersect(includeFields))

internal fun CursorHolder<AbstractDataField>.websiteCursor() =
    WebsiteCursor(cursor, Fields.Website.intersect(includeFields))

// endregion

internal fun CursorHolder<RawContactsField>.rawContactsCursor() =
    RawContactsCursor(cursor, includeFields)

internal fun CursorHolder<RawContactsField>.rawContactsOptionsCursor() =
    RawContactsOptionsCursor(cursor, includeFields)

internal fun CursorHolder<ContactsField>.contactsCursor() = ContactsCursor(cursor, includeFields)

internal fun CursorHolder<ContactsField>.optionsCursor() =
    ContactsOptionsCursor(cursor, includeFields)

internal fun CursorHolder<GroupsField>.groupsCursor() = GroupsCursor(cursor, includeFields)

internal fun CursorHolder<BlockedNumbersField>.blockedNumbersCursor() =
    BlockedNumbersCursor(cursor, includeFields)

internal fun CursorHolder<SimContactField>.simContactCursor() =
    SimContactCursor(cursor, includeFields)