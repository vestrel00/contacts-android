package contacts.core.entities.custom

import contacts.core.AbstractCustomDataField

/**
 * Provides a piece of data as a nullable String from the custom entity [V] corresponding to the
 * field [K].
 */
interface CustomDataFieldMapper<K : AbstractCustomDataField, V : MutableCustomDataEntity> {

    /**
     * Return a piece of data as a nullable String from the [customEntity] corresponding to the
     * given [field].
     */
    fun valueOf(field: K, customEntity: V): String?
}