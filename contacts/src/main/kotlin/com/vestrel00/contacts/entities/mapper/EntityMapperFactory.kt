package com.vestrel00.contacts.entities.mapper

import android.database.Cursor
import com.vestrel00.contacts.entities.*
import com.vestrel00.contacts.entities.cursor.*

internal fun Cursor.addressMapper(): EntityMapper<Address> = AddressMapper(addressCursor())

internal fun Cursor.companyMapper(): EntityMapper<Company> = CompanyMapper(companyCursor())

internal fun Cursor.contactMapper(
    contactIdCursor: ContactIdCursor, isProfile: Boolean
): EntityMapper<Contact> =
    ContactMapper(contactCursor(), contactIdCursor, optionsMapper(), isProfile)

internal fun Cursor.emailMapper(): EntityMapper<Email> = EmailMapper(emailCursor())

internal fun Cursor.eventMapper(): EntityMapper<Event> = EventMapper(eventCursor())

internal fun Cursor.groupMapper(): EntityMapper<Group> = GroupMapper(groupsCursor())

internal fun Cursor.groupMembershipMapper(): EntityMapper<GroupMembership> =
    GroupMembershipMapper(groupMembershipCursor())

internal fun Cursor.imMapper(): EntityMapper<Im> = ImMapper(imCursor())

internal fun Cursor.nameMapper(): EntityMapper<Name> = NameMapper(nameCursor())

internal fun Cursor.nicknameMapper(): EntityMapper<Nickname> = NicknameMapper(nicknameCursor())

internal fun Cursor.noteMapper(): EntityMapper<Note> = NoteMapper(noteCursor())

internal fun Cursor.optionsMapper(): EntityMapper<Options> = OptionsMapper(optionsCursor())

internal fun Cursor.phoneMapper(): EntityMapper<Phone> = PhoneMapper(phoneCursor())

internal fun Cursor.relationMapper(): EntityMapper<Relation> = RelationMapper(relationCursor())

internal fun Cursor.sipAddressMapper(): EntityMapper<SipAddress> =
    SipAddressMapper(sipAddressCursor())

internal fun Cursor.tempRawContactMapper(
    rawContactIdCursor: RawContactIdCursor, isProfile: Boolean
): EntityMapper<TempRawContact> = TempRawContactMapper(rawContactIdCursor, isProfile)

internal fun Cursor.websiteMapper(): EntityMapper<Website> = WebsiteMapper(websiteCursor())