package com.vestrel00.contacts.entities.operation

import android.content.ContentProviderOperation
import com.vestrel00.contacts.entities.MimeType
import com.vestrel00.contacts.entities.MutableCommonDataEntity

/**
 * Returns a new [ContentProviderOperation] for updating [this].
 */
// This can be declared just as MutableDataEntity.updateOperation but this looks more consistent
// with the other functions.
internal fun <T : MutableCommonDataEntity> T.updateOperation(): ContentProviderOperation? =
    dataOperation().updateDataRowOrDeleteIfBlank(this)

// Yes, I know we can avoid this whole type casting situation by moving the body of this function
// to the updateOperation and instead do;
// when (this) is MutableAddress -> AddressOperation().updateDataRowOrDeleteIfBlank(this)
// I prefer this way because this function can be reused :D #NOT-ALWAYS-YAGNI
@Suppress("UNCHECKED_CAST")
private fun <T : MutableCommonDataEntity> T.dataOperation(): AbstractCommonDataOperation<T> =
    when (mimeType) {
        // We could instead do when (this) is MutableAddress -> AddressOperation()
        // However, using mimeType instead of the class allows for exhaustive compilation checks.
        // Not requiring an 'else' branch.
        MimeType.ADDRESS -> AddressOperation
        MimeType.EMAIL -> EmailOperation
        MimeType.EVENT -> EventOperation
        MimeType.IM -> ImOperation
        MimeType.NAME -> NameOperation
        MimeType.NICKNAME -> NicknameOperation
        MimeType.NOTE -> NoteOperation
        MimeType.ORGANIZATION -> OrganizationOperation
        MimeType.PHONE -> PhoneOperation
        MimeType.RELATION -> RelationOperation
        MimeType.SIP_ADDRESS -> SipAddressOperation
        MimeType.WEBSITE -> WebsiteOperation

        // The GroupMembership and Photo class intentionally does not have a mutable version unlike the
        // other entities. Manage group memberships via the RawContactGroupMemberships extension
        // functions. Manage photos via the (Raw)ContactPhoto extension functions.
        MimeType.GROUP_MEMBERSHIP, MimeType.PHOTO, MimeType.UNKNOWN ->
            throw UnsupportedOperationException(
                "No update operation for ${this.javaClass.simpleName}"
            )
    } as AbstractCommonDataOperation<T>