package contacts.core.entities

import android.os.Parcelable

/**
 * Type of all entities provided in this library.
 */
interface Entity : Parcelable {

    /**
     * The ID of this entity (row) in the table it belongs to.
     */
    val id: Long?

    /**
     * Returns true if the underlying data contains at least one non-null and non-empty piece of
     * information.
     *
     * The following has no influence on the value this returns;
     * - Contact ID
     * - RawContact ID
     * - Data ID
     * - Data IS_PRIMARY
     * - Data IS_SUPER_PRIMARY
     */
    val isBlank: Boolean
}

/**
 * Removes all instances of the given [instance] from [this] collection.
 *
 * By default, all **structurally equal (same content but maybe different objects)** instances will
 * be removed. Set [byReference] to true to remove all instances that are **equal by reference
 * (same object)**.
 */
@JvmOverloads
fun <T : Entity> MutableCollection<T>.removeAll(instance: T, byReference: Boolean = false) {
    if (byReference) {
        removeAll { it === instance }
    } else {
        removeAll { it == instance }
    }
}

internal fun Any?.isNotNullOrBlank(): Boolean = when (this) {
    null -> false
    is Entity -> !this.isBlank
    is String -> this.isNotBlank()
    is Collection<*> -> this.isNotNullOrBlank()
    else -> true
}

private fun Collection<*>.isNotNullOrBlank(): Boolean {
    for (it in this) {
        if (it.isNotNullOrBlank()) {
            return true
        }
    }
    return false
}

fun propertiesAreAllNullOrBlank(vararg properties: Any?): Boolean {
    for (property in properties) {
        if (property.isNotNullOrBlank()) {
            return false
        }
    }
    return true
}

internal fun entitiesAreAllBlank(vararg collectionOfEntities: Collection<Entity>): Boolean {
    for (entities in collectionOfEntities) {
        if (entities.isNotNullOrBlank()) {
            return false
        }
    }
    return true
}