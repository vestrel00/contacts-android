package contacts.entities.custom.googlecontacts.userdefined

import contacts.core.entities.MimeType
import contacts.core.entities.custom.AbstractCustomDataOperation
import contacts.entities.custom.googlecontacts.GoogleContactsFields
import contacts.entities.custom.googlecontacts.GoogleContactsMimeType
import contacts.entities.custom.googlecontacts.UserDefinedField

internal class UserDefinedOperationFactory :
    AbstractCustomDataOperation.Factory<UserDefinedField, UserDefinedEntity> {

    override fun create(
        callerIsSyncAdapter: Boolean, isProfile: Boolean, includeFields: Set<UserDefinedField>?
    ): AbstractCustomDataOperation<UserDefinedField, UserDefinedEntity> = UserDefinedOperation(
        callerIsSyncAdapter = callerIsSyncAdapter,
        isProfile = isProfile,
        includeFields = includeFields
    )
}

private class UserDefinedOperation(
    callerIsSyncAdapter: Boolean,
    isProfile: Boolean,
    includeFields: Set<UserDefinedField>?
) : AbstractCustomDataOperation<UserDefinedField, UserDefinedEntity>(
    callerIsSyncAdapter = callerIsSyncAdapter,
    isProfile = isProfile,
    includeFields = includeFields
) {

    override val mimeType: MimeType.Custom = GoogleContactsMimeType.UserDefined

    override fun setCustomData(
        data: UserDefinedEntity, setValue: (field: UserDefinedField, value: Any?) -> Unit
    ) {
        /*
         * When inserting or updating this data kind, the Google Contacts app enforces field and
         * label to both be non-null and non-blank. Otherwise, the insert or update operation fails.
         * To protect the data integrity that the Google Contacts app imposes, this library is
         * silently not performing insert or update operations for these instances. Consumers are
         * informed via documentation. We might change the way we handle this in the future. Maybe
         * throw an exception instead or fail the entire insert/update and bubble up the reason.
         * For now, to avoid complicating the API in these early stages, we'll go with silent but
         * documented =) We'll see what the community thinks!
         */
        if (
            !data.field.isNullOrBlank() && !data.label.isNullOrBlank() &&
            (includeFields == null || (includeFields as Set<UserDefinedField>).containsAll(
                GoogleContactsFields.UserDefined.all
            ))
        ) {
            // Note that if `setValue` is not invoked at least once, then no insert or update
            // operation will be performed for this instance.Ï
            setValue(GoogleContactsFields.UserDefined.Field, data.field)
            setValue(GoogleContactsFields.UserDefined.Label, data.label)
        }
    }
}