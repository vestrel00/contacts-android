package contacts.entities

import contacts.CommonDataField
import contacts.EmptyCommonDataFields
import contacts.Fields
import contacts.custom.CustomCommonDataRegistry

internal fun CommonDataEntity.fields(customDataRegistry: CustomCommonDataRegistry):
        Set<CommonDataField> = mimeType.fields(customDataRegistry)

internal fun MimeType.fields(customDataRegistry: CustomCommonDataRegistry): Set<CommonDataField> =
    when (this) {
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
        is MimeType.Custom -> customDataRegistry.customFieldSetOf(this) ?: EmptyCommonDataFields
        MimeType.Unknown -> EmptyCommonDataFields
    }.all