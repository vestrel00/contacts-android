package com.vestrel00.contacts.entities.mapper

import android.database.Cursor
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.cursor.CursorFactory

internal class EntityMapperFactory(private val cursorFactory: CursorFactory = CursorFactory()) {

    fun init(cursor: Cursor) {
        cursorFactory.init(cursor)
    }

    val addressMapper: AddressMapper
        get() = AddressMapper(cursorFactory.addressCursor)

    val companyMapper: CompanyMapper
        get() = CompanyMapper(cursorFactory.companyCursor)

    val contactMapper: ContactMapper
        get() = ContactMapper(
            cursorFactory.contactCursor,
            OptionsMapper(cursorFactory.optionsCursor)
        )

    val contactId: Long
        get() = cursorFactory.contactCursor.id

    val emailMapper: EmailMapper
        get() = EmailMapper(cursorFactory.emailCursor)

    val eventMapper: EventMapper
        get() = EventMapper(cursorFactory.eventCursor)

    val groupMembershipMapper: GroupMembershipMapper
        get() = GroupMembershipMapper(cursorFactory.groupMembershipCursor)

    val imMapper: ImMapper
        get() = ImMapper(cursorFactory.imCursor)

    val mimeType: MimeType
        get() = cursorFactory.mimeTypeCursor.mimeType

    val nameMapper: NameMapper
        get() = NameMapper(cursorFactory.nameCursor)

    val nicknameMapper: NicknameMapper
        get() = NicknameMapper(cursorFactory.nicknameCursor)

    val noteMapper: NoteMapper
        get() = NoteMapper(cursorFactory.noteCursor)

    val phoneMapper: PhoneMapper
        get() = PhoneMapper(cursorFactory.phoneCursor)

    val rawContactMapper: RawContactMapper
        get() = RawContactMapper(cursorFactory.rawContactCursor)

    val rawContactId: Long
        get() = cursorFactory.rawContactCursor.id

    val relationMapper: RelationMapper
        get() = RelationMapper(cursorFactory.relationCursor)

    val sipAddressMapper: SipAddressMapper
        get() = SipAddressMapper(cursorFactory.sipAddressCursor)

    val websiteMapper: WebsiteMapper
        get() = WebsiteMapper(cursorFactory.websiteCursor)
}
