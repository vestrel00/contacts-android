package com.vestrel00.contacts.entities.mapper

import android.database.Cursor
import com.vestrel00.contacts.entities.*
import com.vestrel00.contacts.entities.cursor.CursorFactory

internal class EntityMapper(private val cursorFactory: CursorFactory = CursorFactory()) {

    fun init(cursor: Cursor) {
        cursorFactory.init(cursor)
    }

    val address: MutableAddress
        get() = AddressMapper(cursorFactory.addressCursor).address

    val company: MutableCompany
        get() = CompanyMapper(cursorFactory.companyCursor).company

    val contact: Contact
        get() = ContactMapper(
            cursorFactory.contactCursor,
            OptionsMapper(cursorFactory.optionsCursor)
        ).contact

    val contactId: Long
        get() = cursorFactory.contactCursor.id

    val email: MutableEmail
        get() = EmailMapper(cursorFactory.emailCursor).email

    val event: MutableEvent
        get() = EventMapper(cursorFactory.eventCursor).event

    val groupMembership: GroupMembership
        get() = GroupMembershipMapper(cursorFactory.groupMembershipCursor).groupMembership

    val im: MutableIm
        get() = ImMapper(cursorFactory.imCursor).im

    val mimeType: MimeType
        get() = MimeTypeMapper(cursorFactory.mimeTypeCursor).mimeType

    val name: MutableName
        get() = NameMapper(cursorFactory.nameCursor).name

    val nickname: MutableNickname
        get() = NicknameMapper(cursorFactory.nicknameCursor).nickname

    val note: MutableNote
        get() = NoteMapper(cursorFactory.noteCursor).note

    val phone: MutablePhone
        get() = PhoneMapper(cursorFactory.phoneCursor).phone

    val rawContact: MutableRawContact
        get() = RawContactMapper(cursorFactory.rawContactCursor).rawContact

    val rawContactId: Long
        get() = cursorFactory.rawContactCursor.id

    val relation: MutableRelation
        get() = RelationMapper(cursorFactory.relationCursor).relation

    val sipAddress: MutableSipAddress
        get() = SipAddressMapper(cursorFactory.sipAddressCursor).sipAddress

    val website: MutableWebsite
        get() = WebsiteMapper(cursorFactory.websiteCursor).website
}
