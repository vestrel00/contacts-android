package contacts.entities.custom

import contacts.AbstractCustomDataField
import contacts.CommonDataField
import contacts.entities.operation.AbstractCommonDataOperation

/**
 * An abstract class that is used as a base of all custom [AbstractCommonDataOperation]s.
 */
abstract class AbstractCustomDataOperation<T : MutableCustomDataEntity>(
    isProfile: Boolean
) : AbstractCommonDataOperation<T>(isProfile) {

    /**
     * Sets the custom [data] values into the operation via the provided [setValue] function.
     */
    protected abstract fun setCustomData(
        data: T, setValue: (field: AbstractCustomDataField, value: Any?) -> Unit
    )

    /*
     * Invokes the abstract setCustomData function, which uses the type of
     * AbstractCustomDataField in the setValue function instead of CommonDataField. This
     * enforces consumers to use their custom data field instead of API fields.
     */
    final override fun setData(data: T, setValue: (field: CommonDataField, value: Any?) -> Unit) {
        setCustomData(data, setValue)
    }

    /**
     * Creates instances of [AbstractCustomDataOperation].
     */
    abstract class Factory<T : MutableCustomDataEntity> {

        /**
         * Creates instances of [AbstractCustomDataOperation] with the given [isProfile].
         */
        abstract fun create(isProfile: Boolean): AbstractCustomDataOperation<T>
    }
}