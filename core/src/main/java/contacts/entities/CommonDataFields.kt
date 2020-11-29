package contacts.entities

import contacts.CommonDataField
import contacts.EmptyCommonDataFields
import contacts.Fields

internal val CommonDataEntity.fields: Set<CommonDataField>
    get() = mimeType.fields

internal val MimeType.fields: Set<CommonDataField>
    get() = when (this) {
        MimeType.Address -> Fields.Address
        MimeType.Email -> Fields.Email
        MimeType.Event -> Fields.Event
        MimeType.GroupMembership -> Fields.GroupMembership
        MimeType.Im -> Fields.Im
        MimeType.Name -> Fields.Name
        MimeType.Nickname -> Fields.Nickname
        MimeType.Note -> Fields.Note
        MimeType.Organization -> Fields.Organization
        MimeType.Phone -> Fields.Phone
        MimeType.Photo -> Fields.Photo
        MimeType.Relation -> Fields.Relation
        MimeType.SipAddress -> Fields.SipAddress
        MimeType.Website -> Fields.Website
        MimeType.Unknown -> EmptyCommonDataFields
    }.all