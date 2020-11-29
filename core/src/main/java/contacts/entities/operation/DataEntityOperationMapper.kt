package contacts.entities.operation

import android.content.ContentProviderOperation
import contacts.entities.MimeType
import contacts.entities.MutableCommonDataEntity

/**
 * Returns a new [ContentProviderOperation] for updating [this].
 */
// This can be declared just as MutableDataEntity.updateOperation but this looks more consistent
// with the other functions.
internal fun <T : MutableCommonDataEntity> T.updateOperation():
        ContentProviderOperation? = dataOperation().updateDataRowOrDeleteIfBlank(this)

// Yes, I know we can avoid this whole type casting situation by moving the body of this function
// to the updateOperation and instead do;
// when (this) is MutableAddress -> AddressOperation().updateDataRowOrDeleteIfBlank(this)
// I prefer this way because this function can be reused :D #NOT-ALWAYS-YAGNI
@Suppress("UNCHECKED_CAST")
private fun <T : MutableCommonDataEntity> T.dataOperation():
        AbstractCommonDataOperation<T> = when (mimeType) {
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

    // The GroupMembership and Photo class intentionally does not have a mutable version unlike the
    // other entities. Manage group memberships via the RawContactGroupMemberships extension
    // functions. Manage photos via the (Raw)ContactPhoto extension functions.
    MimeType.GroupMembership, MimeType.Photo, MimeType.Unknown ->
        throw UnsupportedOperationException(
            "No update operation for ${this.javaClass.simpleName}"
        )
} as AbstractCommonDataOperation<T>