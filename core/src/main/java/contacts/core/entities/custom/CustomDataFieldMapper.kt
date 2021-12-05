package contacts.core.entities.custom

import contacts.core.AbstractCustomDataField
import contacts.core.entities.CustomDataEntity

/**
 * Provides a piece of data as a nullable String from the custom entity [E] corresponding to the
 * field [F].
 */
interface CustomDataFieldMapper<F : AbstractCustomDataField, E : CustomDataEntity> {

    /**
     * Return a piece of data as a nullable String from the [customDataEntity] corresponding to the
     * given [field].
     */
    fun valueOf(field: F, customDataEntity: E): String?
}