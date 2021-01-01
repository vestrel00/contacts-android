package contacts.entities.custom

/**
 * Holds a single, one-per-RawContact entity or multiple, one-or-more-per-RawContact entities.
 */
internal sealed class MutableCustomCommonDataEntityHolder<T : AbstractMutableCustomCommonDataEntity>

/**
 * Holds zero or one-per-RawContact entity.
 */
internal class SingleMutableCustomCommonDataEntityHolder<T : AbstractMutableCustomCommonDataEntity>(
    val entity: T
) : MutableCustomCommonDataEntityHolder<T>()

/**
 * Holds zero or one-or-more-per-RawContact entities.
 */
internal class MultipleMutableCustomCommonDataEntityHolder<T : AbstractMutableCustomCommonDataEntity>(
    val entities: List<T>
) : MutableCustomCommonDataEntityHolder<T>()

internal val <T : AbstractMutableCustomCommonDataEntity>
        MutableCustomCommonDataEntityHolder<T>.entityList: List<T>
    get() = when (this) {
        is SingleMutableCustomCommonDataEntityHolder<T> -> listOf(entity)
        is MultipleMutableCustomCommonDataEntityHolder<T> -> entities
    }