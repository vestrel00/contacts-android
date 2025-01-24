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
    includeFields: Set<AbstractDataField>?,
    customDataRegistry: CustomDataRegistry
): ContentProviderOperation? = dataOperation(
    callerIsSyncAdapter = callerIsSyncAdapter,
    includeFields = includeFields,
    customDataRegistry = customDataRegistry
).updateDataRowOrDeleteIfBlank(this)

@Suppress("UNCHECKED_CAST")
private fun ExistingDataEntity.dataOperation(
    callerIsSyncAdapter: Boolean,
    includeFields: Set<AbstractDataField>?,
    customDataRegistry: CustomDataRegistry
): AbstractDataOperation<*, DataEntity> =
    // We could instead do when (this) is MutableAddress -> AddressOperation()
    // However, using mimeType instead of the class allows for exhaustive compilation checks.
    // Not requiring an 'else' branch.
    when (mimeType) {
        // Check custom mimetype first to allow for overriding built-in mimetypes.
        is MimeType.Custom -> {
            val customDataEntry = customDataRegistry
                // Smart cast doesn't work here like this because mimeType has a custom getter. We can fix
                // this by declaring a local val mimeType = this.mimeType but this looks okay.
                .entryOf(mimeType as MimeType.Custom)

            customDataEntry.operationFactory.create(
                callerIsSyncAdapter = callerIsSyncAdapter,
                isProfile = isProfile,
                includeFields = includeFields?.let(customDataEntry.fieldSet::intersect)
            )
        }

        MimeType.Address -> AddressOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            includeFields?.let(Fields.Address::intersect)
        )

        MimeType.Email -> EmailOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            includeFields?.let(Fields.Email::intersect)
        )

        MimeType.Event -> EventOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            includeFields?.let(Fields.Event::intersect)
        )

        MimeType.Im -> ImOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            @Suppress("Deprecation") includeFields?.let(Fields.Im::intersect)
        )

        MimeType.Name -> NameOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            includeFields?.let(Fields.Name::intersect)
        )

        MimeType.Nickname -> NicknameOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            includeFields?.let(Fields.Nickname::intersect)
        )

        MimeType.Note -> NoteOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            includeFields?.let(Fields.Note::intersect)
        )

        MimeType.Organization -> OrganizationOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            includeFields?.let(Fields.Organization::intersect)
        )

        MimeType.Phone -> PhoneOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            includeFields?.let(Fields.Phone::intersect)
        )

        MimeType.Relation -> RelationOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            includeFields?.let(Fields.Relation::intersect)
        )

        MimeType.SipAddress -> SipAddressOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            @Suppress("Deprecation") includeFields?.let(Fields.SipAddress::intersect)
        )

        MimeType.Website -> WebsiteOperation(
            callerIsSyncAdapter = callerIsSyncAdapter,
            isProfile = isProfile,
            includeFields?.let(Fields.Website::intersect)
        )

        // The GroupMembership and Photo class intentionally does not have a mutable version unlike the
        // other entities. Manage group memberships via the RawContactGroupMemberships extension
        // functions. Manage photos via the (Raw)ContactPhoto extension functions.
        MimeType.GroupMembership, MimeType.Photo, MimeType.Unknown -> throw ContactsException("No data operation found for ${mimeType.value}")
    } as AbstractDataOperation<*, DataEntity>