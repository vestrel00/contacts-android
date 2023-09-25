package contacts.core.entities.custom

import contacts.core.AbstractCustomDataField
import contacts.core.entities.CustomDataEntity
import contacts.core.entities.MimeType
import contacts.core.entities.operation.AbstractDataOperation

/**
 * Base type of all custom [AbstractDataOperation]s.
 */
abstract class AbstractCustomDataOperation
<F : AbstractCustomDataField, E : CustomDataEntity>(
    callerIsSyncAdapter: Boolean,
    isProfile: Boolean,
    includeFields: Set<F>?
) : AbstractDataOperation<F, E>(
    callerIsSyncAdapter = callerIsSyncAdapter,
    isProfile = isProfile,
    includeFields = includeFields
) {

    // Override this to cast type from MimeType to MimeType.Custom
    abstract override val mimeType: MimeType.Custom

    /**
     * Sets the custom [data] values into the operation via the provided [setValue] function.
     */
    protected abstract fun setCustomData(
        data: E, setValue: (field: F, value: Any?) -> Unit
    )

    /*
     * Invokes the abstract setCustomData function, which uses the type of
     * AbstractCustomDataField in the setValue function instead of DataField. This
     * enforces consumers to use their custom data field instead of API fields.
     */
    final override fun setValuesFromData(data: E, setValue: (field: F, value: Any?) -> Unit) {
        setCustomData(data, setValue)
    }

    /**
     * Creates instances of [AbstractCustomDataOperation].
     */
    interface Factory<F : AbstractCustomDataField, E : CustomDataEntity> {

        /**
         * Creates instances of [AbstractCustomDataOperation].
         *
         * ## Include fields
         *
         * Insert and update operations will do nothing (no-op) for data whose corresponding field
         * is not specified in [includeFields]. If [includeFields] is...
         *
         * - null, then the included field checks are disabled. This means that any non-blank data
         *   will be processed. This is a more optimal, recommended way of including all fields.
         * - not null but empty, then data will be skipped (no-op).
         */
        fun create(
            callerIsSyncAdapter: Boolean,
            isProfile: Boolean,
            includeFields: Set<F>?
        ): AbstractCustomDataOperation<F, E>
    }
}