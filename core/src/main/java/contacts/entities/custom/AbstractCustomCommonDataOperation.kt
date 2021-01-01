package contacts.entities.custom

import contacts.AbstractCustomCommonDataField
import contacts.CommonDataField
import contacts.entities.CommonDataEntity
import contacts.entities.operation.AbstractCommonDataOperation

/**
 * An abstract class that is used as a base of all custom [AbstractCommonDataOperation]s.
 *
 * ## Developer notes
 *
 * Technically, this can be optional. We could have implemented this part of the API to be able to
 * handle [CommonDataEntity] directly instead of this [AbstractMutableCustomCommonDataEntity].
 * However, we are able to streamline all custom entities this way, which makes our internal code
 * easier to follow / trace. It also gives us more control and flexibility.
 */
abstract class AbstractCustomCommonDataOperation<T : AbstractMutableCustomCommonDataEntity>(
    isProfile: Boolean
) : AbstractCommonDataOperation<T>(isProfile) {

    /**
     * Sets the custom [data] values into the operation via the provided [setValue] function.
     */
    protected abstract fun setCustomData(
        data: T, setValue: (field: AbstractCustomCommonDataField, value: Any?) -> Unit
    )

    /*
     * Invokes the abstract setCustomData function, which uses the type of
     * AbstractCustomCommonDataField in the setValue function instead of CommonDataField. This
     * enforces consumers to use their custom data field instead of API fields.
     */
    final override fun setData(data: T, setValue: (field: CommonDataField, value: Any?) -> Unit) {
        setCustomData(data, setValue)
    }

    /**
     * Creates instances of [AbstractCustomCommonDataOperation].
     */
    abstract class Factory<out T : AbstractCustomCommonDataOperation<*>> {

        /**
         * Creates instances of [AbstractCustomCommonDataOperation] with the given [isProfile].
         */
        abstract fun create(isProfile: Boolean): T
    }
}