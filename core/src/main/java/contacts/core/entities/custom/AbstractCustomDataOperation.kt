package contacts.core.entities.custom

import contacts.core.AbstractCustomDataField
import contacts.core.entities.MimeType
import contacts.core.entities.operation.AbstractCommonDataOperation

/**
 * An abstract class that is used as a base of all custom [AbstractCommonDataOperation]s.
 */
abstract class AbstractCustomDataOperation<K : AbstractCustomDataField, V : MutableCustomDataEntity>(
    isProfile: Boolean
) : AbstractCommonDataOperation<K, V>(isProfile) {

    // Override this to cast type from MimeType to MimeType.Custom
    abstract override val mimeType: MimeType.Custom

    /**
     * Sets the custom [data] values into the operation via the provided [setValue] function.
     */
    protected abstract fun setCustomData(
        data: V, setValue: (field: K, value: Any?) -> Unit
    )

    /*
     * Invokes the abstract setCustomData function, which uses the type of
     * AbstractCustomDataField in the setValue function instead of CommonDataField. This
     * enforces consumers to use their custom data field instead of API fields.
     */
    final override fun setData(data: V, setValue: (field: K, value: Any?) -> Unit) {
        setCustomData(data, setValue)
    }

    /**
     * Creates instances of [AbstractCustomDataOperation].
     */
    interface Factory<K : AbstractCustomDataField, V : MutableCustomDataEntity> {

        /**
         * Creates instances of [AbstractCustomDataOperation] with the given [isProfile].
         */
        fun create(isProfile: Boolean): AbstractCustomDataOperation<K, V>
    }
}