package com.vestrel00.contacts.entities.mapper

import android.database.Cursor
import com.vestrel00.contacts.entities.*
import com.vestrel00.contacts.entities.cursor.CursorFactory

internal class EntityMapperFactory(private val cursorFactory: CursorFactory = CursorFactory()) {

    fun init(cursor: Cursor) {
        cursorFactory.init(cursor)
    }

    val addressMapper: EntityMapper<Address>
        get() = AddressMapper(cursorFactory.addressCursor)

    val companyMapper: EntityMapper<Company>
        get() = CompanyMapper(cursorFactory.companyCursor)

    val contactMapper: EntityMapper<Contact>
        get() = ContactMapper(
            cursorFactory.contactCursor,
            OptionsMapper(cursorFactory.optionsCursor)
        )

    val contactId: Long
        get() = cursorFactory.contactCursor.id

    val emailMapper: EntityMapper<Email>
        get() = EmailMapper(cursorFactory.emailCursor)

    val eventMapper: EntityMapper<Event>
        get() = EventMapper(cursorFactory.eventCursor)

    val groupMembershipMapper: EntityMapper<GroupMembership>
        get() = GroupMembershipMapper(cursorFactory.groupMembershipCursor)

    val imMapper: EntityMapper<Im>
        get() = ImMapper(cursorFactory.imCursor)

    val mimeType: MimeType
        get() = cursorFactory.mimeTypeCursor.mimeType

    val nameMapper: EntityMapper<Name>
        get() = NameMapper(cursorFactory.nameCursor)

    val nicknameMapper: EntityMapper<Nickname>
        get() = NicknameMapper(cursorFactory.nicknameCursor)

    val noteMapper: EntityMapper<Note>
        get() = NoteMapper(cursorFactory.noteCursor)

    val phoneMapper: EntityMapper<Phone>
        get() = PhoneMapper(cursorFactory.phoneCursor)

    val rawContactId: Long
        get() = cursorFactory.rawContactCursor.id

    val relationMapper: EntityMapper<Relation>
        get() = RelationMapper(cursorFactory.relationCursor)

    val sipAddressMapper: EntityMapper<SipAddress>
        get() = SipAddressMapper(cursorFactory.sipAddressCursor)

    val tempRawContactMapper: EntityMapper<TempRawContact>
        get() = TempRawContactMapper(cursorFactory.rawContactCursor)

    val websiteMapper: EntityMapper<Website>
        get() = WebsiteMapper(cursorFactory.websiteCursor)
}
