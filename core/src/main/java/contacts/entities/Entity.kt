package contacts.entities

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
     * Returns true if this entity has no non-null (and non-blank for strings) properties or other
     * entities containing non-null (and non-blank for strings) properties.
     */
    val isBlank: Boolean

    interface Type {
        val value: Int
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

internal fun propertiesAreAllNullOrBlank(vararg properties: Any?): Boolean {
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