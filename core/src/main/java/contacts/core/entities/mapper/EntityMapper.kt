package contacts.core.entities.mapper

import contacts.core.entities.ExistingEntity
import contacts.core.entities.ImmutableCustomDataEntity
import contacts.core.entities.ImmutableDataEntity

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
 * Creates immutable data entities from database values.
 *
 * We'll make sure that all **data** entities that are extracted from the database are immutable.
 */
internal sealed interface DataEntityMapper<out T : ImmutableDataEntity> : EntityMapper<T>

/**
 * Creates immutable custom data entities from database values.
 *
 * We'll make sure that all **data** entities that are extracted from the database are immutable.
 */
// Intentionally not sealed.
internal interface CustomDataEntityMapper<out T : ImmutableCustomDataEntity> : DataEntityMapper<T>