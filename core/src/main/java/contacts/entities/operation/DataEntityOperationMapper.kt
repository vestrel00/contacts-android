package contacts.entities.operation

import android.content.ContentProviderOperation
import contacts.ContactsException
import contacts.entities.MimeType
import contacts.entities.MutableCommonDataEntity
import contacts.entities.custom.CustomDataException
import contacts.entities.custom.CustomDataRegistry

/**
 * Returns a new [ContentProviderOperation] for updating [this].
 */
internal fun MutableCommonDataEntity.updateOperation(
    customDataRegistry: CustomDataRegistry
): ContentProviderOperation? = dataOperation(customDataRegistry).updateDataRowOrDeleteIfBlank(this)

@Suppress("UNCHECKED_CAST")
private fun MutableCommonDataEntity.dataOperation(
    customDataRegistry: CustomDataRegistry
): AbstractCommonDataOperation<*, MutableCommonDataEntity> = when (mimeType) {
    // We could instead do when (this) is MutableAddress -> AddressOperation()
    // However, using mimeType instead of the class allows for exhaustive compilation checks.
    // Not requiring an 'else' branch.
    MimeType.Address -> AddressOperation(isProfile)
    MimeType.Email -> EmailOperation(isProfile)
    MimeType.Event -> EventOperation(isProfile)
    MimeType.Im -> ImOperation(isProfile)
    MimeType.Name -> NameOperation(isProfile)
    MimeType.Nickname -> NicknameOperation(isProfile)
    MimeType.Note -> NoteOperation(isProfile)
    MimeType.Organization -> OrganizationOperation(isProfile)
    MimeType.Phone -> PhoneOperation(isProfile)
    MimeType.Relation -> RelationOperation(isProfile)
    MimeType.SipAddress -> SipAddressOperation(isProfile)
    MimeType.Website -> WebsiteOperation(isProfile)
    is MimeType.Custom -> customDataRegistry
        // Smart cast doesn't work here like this because mimeType has a custom getter. We can fix
        // this by declaring a local val mimeType = this.mimeType but this looks okay.
        .operationFactoryOf(mimeType as MimeType.Custom)
        ?.create(isProfile)
        ?: throw CustomDataException("No custom data operation found for ${mimeType.value}")

    // The GroupMembership and Photo class intentionally does not have a mutable version unlike the
    // other entities. Manage group memberships via the RawContactGroupMemberships extension
    // functions. Manage photos via the (Raw)ContactPhoto extension functions.
    MimeType.GroupMembership, MimeType.Photo, MimeType.Unknown ->
        throw ContactsException("No data operation found for ${mimeType.value}")
} as AbstractCommonDataOperation<*, MutableCommonDataEntity>