package com.vestrel00.contacts.entities.cursor

import android.database.Cursor

// Not using AutoFactory because there is no dependency injection framework used in this lib.
internal class CursorFactory {

    private lateinit var cursor: Cursor

    fun init(cursor: Cursor) {
        this.cursor = cursor
    }

    val addressCursor: AddressCursor
        get() = AddressCursor(cursor)

    val companyCursor: CompanyCursor
        get() = CompanyCursor(cursor)

    val contactCursor: ContactCursor
        get() = ContactCursor(cursor)

    val emailCursor: EmailCursor
        get() = EmailCursor(cursor)

    val eventCursor: EventCursor
        get() = EventCursor(cursor)

    val groupMembershipCursor: GroupMembershipCursor
        get() = GroupMembershipCursor(cursor)

    val imCursor: ImCursor
        get() = ImCursor(cursor)

    val mimeTypeCursor: MimeTypeCursor
        get() = MimeTypeCursor(cursor)

    val nameCursor: NameCursor
        get() = NameCursor(cursor)

    val nicknameCursor: NicknameCursor
        get() = NicknameCursor(cursor)

    val noteCursor: NoteCursor
        get() = NoteCursor(cursor)

    val optionsCursor: OptionsCursor
        get() = OptionsCursor(cursor)

    val phoneCursor: PhoneCursor
        get() = PhoneCursor(cursor)

    val rawContactCursor: RawContactCursor
        get() = RawContactCursor(cursor)

    val relationCursor: RelationCursor
        get() = RelationCursor(cursor)

    val sipAddressCursor: SipAddressCursor
        get() = SipAddressCursor(cursor)

    val websiteCursor: WebsiteCursor
        get() = WebsiteCursor(cursor)
}
