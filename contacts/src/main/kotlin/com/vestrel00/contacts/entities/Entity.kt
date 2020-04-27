package com.vestrel00.contacts.entities

/**
 * Type of all entities provided in this library.
 */
interface Entity {

    /**
     * The ID of this entity (row) in the table it belongs to.
     */
    val id: Long

    /**
     * Returns true if this entity has no non-null (and non-blank for strings) properties or other
     * entities containing non-null (and non-blank for strings) properties.
     */
    fun isBlank(): Boolean

    /**
     * Returns true if this entity has a valid id, which indicates that it is (or was at the time of
     * retrieval) an existing entity in the database.
     */
    fun hasValidId(): Boolean = id > INVALID_ID

    interface Type {
        val value: Int
    }
}

internal fun Any?.isNotNullOrBlank(): Boolean = when (this) {
    null -> false
    is Entity -> !this.isBlank()
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

internal fun <T : Entity> Collection<T>.toValidEntitiesMap(): Map<Long, T> = asSequence()
    .filter { it.hasValidId() }
    .map { it.id to it }
    .toMap()

// There is no invalid entities map because all invalid entities have the same id!!!
internal fun <T : Entity> Collection<T>.toInvalidEntities(): Collection<T> =
    filter { !it.hasValidId() }

// TODO Delete this ?
internal const val INVALID_ID: Long = -1L