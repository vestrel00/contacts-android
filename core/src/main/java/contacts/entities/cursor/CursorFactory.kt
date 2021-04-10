package contacts.entities.cursor

import contacts.AbstractDataField
import contacts.ContactsField
import contacts.GroupsField
import contacts.RawContactsField
import contacts.entities.custom.CustomDataRegistry

internal fun CursorHolder<AbstractDataField>.addressCursor() = AddressCursor(cursor)
internal fun CursorHolder<AbstractDataField>.dataContactsCursor() = DataContactsCursor(cursor)
internal fun CursorHolder<AbstractDataField>.dataCursor() = DataCursor(cursor)
internal fun CursorHolder<AbstractDataField>.emailCursor() = EmailCursor(cursor)
internal fun CursorHolder<AbstractDataField>.eventCursor() = EventCursor(cursor)
internal fun CursorHolder<AbstractDataField>.groupMembershipCursor() = GroupMembershipCursor(cursor)
internal fun CursorHolder<AbstractDataField>.imCursor() = ImCursor(cursor)
internal fun CursorHolder<AbstractDataField>.mimeTypeCursor(
    customDataRegistry: CustomDataRegistry
) = MimeTypeCursor(cursor, customDataRegistry)

internal fun CursorHolder<AbstractDataField>.nameCursor() = NameCursor(cursor)
internal fun CursorHolder<AbstractDataField>.nicknameCursor() = NicknameCursor(cursor)
internal fun CursorHolder<AbstractDataField>.noteCursor() = NoteCursor(cursor)
internal fun CursorHolder<AbstractDataField>.dataContactsOptionsCursor() = OptionsCursor(cursor)
internal fun CursorHolder<AbstractDataField>.organizationCursor() = OrganizationCursor(cursor)
internal fun CursorHolder<AbstractDataField>.phoneCursor() = PhoneCursor(cursor)
internal fun CursorHolder<AbstractDataField>.photoCursor() = PhotoCursor(cursor)
internal fun CursorHolder<AbstractDataField>.relationCursor() = RelationCursor(cursor)
internal fun CursorHolder<AbstractDataField>.sipAddressCursor() = SipAddressCursor(cursor)
internal fun CursorHolder<AbstractDataField>.websiteCursor() = WebsiteCursor(cursor)

internal fun CursorHolder<RawContactsField>.rawContactsCursor() = RawContactsCursor(cursor)
internal fun CursorHolder<RawContactsField>.rawContactsOptionsCursor() = OptionsCursor(cursor)

internal fun CursorHolder<ContactsField>.contactsCursor() = ContactsCursor(cursor)
internal fun CursorHolder<ContactsField>.optionsCursor() = OptionsCursor(cursor)

internal fun CursorHolder<GroupsField>.groupsCursor() = GroupsCursor(cursor)