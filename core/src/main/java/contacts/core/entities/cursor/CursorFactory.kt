package contacts.core.entities.cursor

import contacts.core.AbstractDataField
import contacts.core.BlockedNumbersField
import contacts.core.ContactsField
import contacts.core.Fields
import contacts.core.GroupsField
import contacts.core.PhoneLookupField
import contacts.core.RawContactsField
import contacts.core.SimContactsField
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.intersect

// region AbstractDataField

internal fun CursorHolder<AbstractDataField>.addressCursor() =
    AddressCursor(cursor, includeFields?.let(Fields.Address::intersect))

internal fun CursorHolder<AbstractDataField>.dataContactsCursor() =
    DataContactsCursor(cursor, includeFields?.let(Fields.Contact::intersect))

internal fun <T : AbstractDataField> CursorHolder<T>.dataCursor() =
    DataCursor(cursor, includeFields)

internal fun CursorHolder<AbstractDataField>.emailCursor() =
    EmailCursor(cursor, includeFields?.let(Fields.Email::intersect))

internal fun CursorHolder<AbstractDataField>.eventCursor() =
    EventCursor(cursor, includeFields?.let(Fields.Event::intersect))

internal fun CursorHolder<AbstractDataField>.groupMembershipCursor() =
    GroupMembershipCursor(cursor, includeFields?.let(Fields.GroupMembership::intersect))

@Suppress("Deprecation")
internal fun CursorHolder<AbstractDataField>.imCursor() =
    ImCursor(cursor, includeFields?.let(Fields.Im::intersect))

internal fun CursorHolder<AbstractDataField>.mimeTypeCursor(
    customDataRegistry: CustomDataRegistry
) = MimeTypeCursor(cursor, customDataRegistry)

internal fun CursorHolder<AbstractDataField>.nameCursor() =
    NameCursor(cursor, includeFields?.let(Fields.Name::intersect))

internal fun CursorHolder<AbstractDataField>.nicknameCursor() =
    NicknameCursor(cursor, includeFields?.let(Fields.Nickname::intersect))

internal fun CursorHolder<AbstractDataField>.noteCursor() =
    NoteCursor(cursor, includeFields?.let(Fields.Note::intersect))

internal fun CursorHolder<AbstractDataField>.organizationCursor() =
    OrganizationCursor(cursor, includeFields?.let(Fields.Organization::intersect))

internal fun CursorHolder<AbstractDataField>.phoneCursor() =
    PhoneCursor(cursor, includeFields?.let(Fields.Phone::intersect))

internal fun CursorHolder<AbstractDataField>.photoCursor() =
    PhotoCursor(cursor, includeFields?.let(Fields.Photo::intersect))

internal fun CursorHolder<AbstractDataField>.relationCursor() =
    RelationCursor(cursor, includeFields?.let(Fields.Relation::intersect))

@Suppress("Deprecation")
internal fun CursorHolder<AbstractDataField>.sipAddressCursor() =
    SipAddressCursor(cursor, includeFields?.let(Fields.SipAddress::intersect))

internal fun CursorHolder<AbstractDataField>.websiteCursor() =
    WebsiteCursor(cursor, includeFields?.let(Fields.Website::intersect))

// endregion

internal fun CursorHolder<RawContactsField>.rawContactsCursor() =
    RawContactsCursor(cursor, includeFields)

internal fun CursorHolder<RawContactsField>.rawContactsOptionsCursor() =
    RawContactsOptionsCursor(cursor, includeFields)

internal fun CursorHolder<ContactsField>.contactsCursor() = ContactsCursor(cursor, includeFields)

internal fun CursorHolder<ContactsField>.optionsCursor() =
    ContactsOptionsCursor(cursor, includeFields)

internal fun CursorHolder<PhoneLookupField>.phoneLookupCursor() =
    PhoneLookupCursor(cursor, includeFields)

internal fun CursorHolder<GroupsField>.groupsCursor() = GroupsCursor(cursor, includeFields)

internal fun CursorHolder<BlockedNumbersField>.blockedNumbersCursor() =
    BlockedNumbersCursor(cursor, includeFields)

internal fun CursorHolder<SimContactsField>.simContactCursor() =
    SimContactCursor(cursor, includeFields)