package contacts.core.entities.custom

import contacts.core.AbstractCustomDataField

/**
 * Provides a piece of data as a nullable String from the custom entity [E] corresponding to the
 * field [F].
 */
interface CustomDataFieldMapper<F : AbstractCustomDataField, E : MutableCustomData> {

    /**
     * Return a piece of data as a nullable String from the [customEntity] corresponding to the
     * given [field].
     */
    fun valueOf(field: F, customEntity: E): String?
}