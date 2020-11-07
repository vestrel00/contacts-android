package com.vestrel00.contacts.entities.cursor

import com.vestrel00.contacts.AbstractDataField
import com.vestrel00.contacts.ContactsField
import com.vestrel00.contacts.GroupsField
import com.vestrel00.contacts.RawContactsField

internal fun EntityCursor<AbstractDataField>.addressCursor() = AddressCursor(cursor)
internal fun EntityCursor<AbstractDataField>.dataContactsCursor() = DataContactsCursor(cursor)
internal fun EntityCursor<AbstractDataField>.dataCursor() = DataCursor(cursor)
internal fun EntityCursor<AbstractDataField>.emailCursor() = EmailCursor(cursor)
internal fun EntityCursor<AbstractDataField>.eventCursor() = EventCursor(cursor)
internal fun EntityCursor<AbstractDataField>.groupMembershipCursor() = GroupMembershipCursor(cursor)
internal fun EntityCursor<AbstractDataField>.imCursor() = ImCursor(cursor)
internal fun EntityCursor<AbstractDataField>.mimeTypeCursor() = MimeTypeCursor(cursor)
internal fun EntityCursor<AbstractDataField>.nameCursor() = NameCursor(cursor)
internal fun EntityCursor<AbstractDataField>.nicknameCursor() = NicknameCursor(cursor)
internal fun EntityCursor<AbstractDataField>.noteCursor() = NoteCursor(cursor)
internal fun EntityCursor<AbstractDataField>.dataContactsOptionsCursor() = OptionsCursor(cursor)
internal fun EntityCursor<AbstractDataField>.organizationCursor() = OrganizationCursor(cursor)
internal fun EntityCursor<AbstractDataField>.phoneCursor() = PhoneCursor(cursor)
internal fun EntityCursor<AbstractDataField>.photoCursor() = PhotoCursor(cursor)
internal fun EntityCursor<AbstractDataField>.relationCursor() = RelationCursor(cursor)
internal fun EntityCursor<AbstractDataField>.sipAddressCursor() = SipAddressCursor(cursor)
internal fun EntityCursor<AbstractDataField>.websiteCursor() = WebsiteCursor(cursor)

internal fun EntityCursor<RawContactsField>.rawContactsCursor() = RawContactsCursor(cursor)
internal fun EntityCursor<RawContactsField>.rawContactsOptionsCursor() = OptionsCursor(cursor)

internal fun EntityCursor<ContactsField>.contactsCursor() = ContactsCursor(cursor)
internal fun EntityCursor<ContactsField>.optionsCursor() = OptionsCursor(cursor)

internal fun EntityCursor<GroupsField>.groupsCursor() = GroupsCursor(cursor)