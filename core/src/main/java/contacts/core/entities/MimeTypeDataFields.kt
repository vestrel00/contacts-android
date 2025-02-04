package contacts.core.entities

import contacts.core.AbstractDataFieldSet
import contacts.core.DataField
import contacts.core.EmptyDataFields
import contacts.core.Fields
import contacts.core.entities.custom.CustomDataRegistry

internal fun DataEntity.fields(customDataRegistry: CustomDataRegistry):
        AbstractDataFieldSet<DataField> = mimeType.fields(customDataRegistry)

internal fun MimeType.fields(
    customDataRegistry: CustomDataRegistry
): AbstractDataFieldSet<DataField> = when (this) {
    // Check custom mimetype first to allow for overriding built-in mimetypes.
    // Note that this can also be placed at the end instead of here at the beginning because
    // 'this' can only be custom or one of the built-in mimetypes. However, this follows the pattern
    // used throughout the codebase of checking custom data first, which makes more logical sense
    // even if technically unnecessary.
    is MimeType.Custom -> customDataRegistry.entryOf(this).fieldSet
    MimeType.Address -> Fields.Address
    MimeType.Email -> Fields.Email
    MimeType.Event -> Fields.Event
    MimeType.GroupMembership -> Fields.GroupMembership
    MimeType.Im -> @Suppress("Deprecation") Fields.Im
    MimeType.Name -> Fields.Name
    MimeType.Nickname -> Fields.Nickname
    MimeType.Note -> Fields.Note
    MimeType.Organization -> Fields.Organization
    MimeType.Phone -> Fields.Phone
    MimeType.Photo -> Fields.Photo
    MimeType.Relation -> Fields.Relation
    MimeType.SipAddress -> @Suppress("Deprecation") Fields.SipAddress
    MimeType.Website -> Fields.Website
    MimeType.Unknown -> EmptyDataFields
}