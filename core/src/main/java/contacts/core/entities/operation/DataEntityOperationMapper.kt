package contacts.core.entities.operation

import android.content.ContentProviderOperation
import contacts.core.AbstractDataField
import contacts.core.ContactsException
import contacts.core.Fields
import contacts.core.entities.DataEntity
import contacts.core.entities.ExistingDataEntity
import contacts.core.entities.MimeType
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.intersect

/**
 * Returns a new [ContentProviderOperation] for updating [this].
 *
 * Only the fields specified in [includeFields] will be updated.
 */
internal fun ExistingDataEntity.updateOperation(
    callerIsSyncAdapter: Boolean,
    includeFields: Set<AbstractDataField>,
    customDataRegistry: CustomDataRegistry
): ContentProviderOperation? = dataOperation(
    callerIsSyncAdapter = callerIsSyncAdapter,
    includeFields = includeFields,
    customDataRegistry = customDataRegistry
).updateDataRowOrDeleteIfBlank(this)

@Suppress("UNCHECKED_CAST")
private fun ExistingDataEntity.dataOperation(
    callerIsSyncAdapter: Boolean,
    includeFields: Set<AbstractDataField>,
    customDataRegistry: CustomDataRegistry
): AbstractDataOperation<*, DataEntity> = when (mimeType) {
    // We could instead do when (this) is MutableAddress -> AddressOperation()
    // However, using mimeType instead of the class allows for exhaustive compilation checks.
    // Not requiring an 'else' branch.
    MimeType.Address -> AddressOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        Fields.Address.intersect(includeFields)
    )

    MimeType.Email -> EmailOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        Fields.Email.intersect(includeFields)
    )

    MimeType.Event -> EventOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        Fields.Event.intersect(includeFields)
    )

    MimeType.Im -> ImOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        Fields.Im.intersect(includeFields)
    )

    MimeType.Name -> NameOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        Fields.Name.intersect(includeFields)
    )

    MimeType.Nickname -> NicknameOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        Fields.Nickname.intersect(includeFields)
    )

    MimeType.Note -> NoteOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        Fields.Note.intersect(includeFields)
    )

    MimeType.Organization -> OrganizationOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        Fields.Organization.intersect((includeFields))
    )

    MimeType.Phone -> PhoneOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        Fields.Phone.intersect(includeFields)
    )

    MimeType.Relation -> RelationOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        Fields.Relation.intersect(includeFields)
    )

    MimeType.SipAddress -> SipAddressOperation(

        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        Fields.SipAddress.intersect(includeFields)
    )

    MimeType.Website -> WebsiteOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        Fields.Website.intersect(includeFields)
    )

    is MimeType.Custom -> {
        val customDataEntry = customDataRegistry
            // Smart cast doesn't work here like this because mimeType has a custom getter. We can fix
            // this by declaring a local val mimeType = this.mimeType but this looks okay.
            .entryOf(mimeType as MimeType.Custom)

        customDataEntry.operationFactory.create(
                callerIsSyncAdapter = callerIsSyncAdapter,
                isProfile = isProfile,
                includeFields = customDataEntry.fieldSet.intersect(includeFields)
            )
    }
    // The GroupMembership and Photo class intentionally does not have a mutable version unlike the
    // other entities. Manage group memberships via the RawContactGroupMemberships extension
    // functions. Manage photos via the (Raw)ContactPhoto extension functions.
    MimeType.GroupMembership, MimeType.Photo, MimeType.Unknown -> throw ContactsException("No data operation found for ${mimeType.value}")
} as AbstractDataOperation<*, DataEntity>