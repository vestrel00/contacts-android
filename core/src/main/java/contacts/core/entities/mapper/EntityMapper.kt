package contacts.core.entities.mapper

import contacts.core.entities.ExistingCustomDataEntity
import contacts.core.entities.ExistingDataEntity
import contacts.core.entities.ExistingEntity

/**
 * Creates entities from database values.
 */
internal sealed interface EntityMapper<out T : ExistingEntity> {
    val value: T

    /**
     * The [value] if it is not blank. Else, returns null.
     */
    val nonBlankValueOrNull: T?
        get() = if (value.isBlank) null else value
}

/**
 * Creates models for existing data entities from database values.
 */
internal sealed interface DataEntityMapper<out T : ExistingDataEntity> : EntityMapper<T>

/**
 * Creates models for existing custom data entities from database values.
 */
// Intentionally not sealed.
internal interface CustomDataEntityMapper<out T : ExistingCustomDataEntity> : DataEntityMapper<T>