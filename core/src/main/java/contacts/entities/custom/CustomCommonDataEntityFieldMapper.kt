package contacts.entities.custom

import contacts.AbstractCustomCommonDataField

/**
 * Provides a piece of data as a nullable String from the custom entity [V] corresponding to the
 * field [K].
 */
interface CustomCommonDataEntityFieldMapper<K : AbstractCustomCommonDataField,
        V : MutableCustomCommonDataEntity> {

    /**
     * Return a piece of data as a nullable String from the [customEntity] corresponding to the
     * given [field].
     */
    fun valueOf(field: K, customEntity: V): String?
}