package contacts.core.util

import contacts.core.entities.Entity
import java.util.*

/**
 * Entities can either be new or existing entities. Only existing entities have IDs. Therefore, when
 * comparing by ID, we have to be able to compare null and non-null ids.
 */
// Expose this to consumers. Maybe they'll find a use for it?
class EntityIdComparator : Comparator<Entity> {

    override fun compare(o1: Entity, o2: Entity): Int = o1.idOrNull.compareTo(o2.idOrNull)

    /**
     * Compares [this] nullable long to the [other] nullable long.
     *
     * If both [this] and [other] are not null, then a comparison is done on both. Otherwise, this
     * returns a positive integer if [this] is null and [other] is not null. Returns a negative integer
     * if [this] is not null and [other] is null. Returns 0 if both [this] and [other] are null.
     */
    private fun Long?.compareTo(other: Long?): Int {
        return if (this != null && other != null) {
            compareTo(other)
        } else if (this == null && other != null) {
            1
        } else if (this != null && other == null) {
            -1
        } else {
            0
        }
    }
}

fun <T : Entity> Sequence<T>.sortedById(): Sequence<T> = sortedWith(EntityIdComparator())

fun <T : Entity> Collection<T>.sortedById(): Collection<T> = sortedWith(EntityIdComparator())