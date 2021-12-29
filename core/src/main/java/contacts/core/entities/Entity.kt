package contacts.core.entities

import android.os.Parcelable
import contacts.core.Redactable

/**
 * Base of all entities provided in this library.
 */
sealed interface Entity : Redactable, Parcelable {

    /**
     * Returns true all property values are either null, empty, or blank.
     */
    val isBlank: Boolean

    /**
     * Only existing data entities have an id. For all others, this will return null.
     */
    // This is declared here instead of an extension function for easier use for Java users.
    val idOrNull: Long?
        get() = null

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): Entity

    companion object {
        internal const val INVALID_ID: Long = -1L
    }
}

/**
 * An [Entity] that has NOT yet been inserted into the database.
 *
 * These entities are only used for insert operations.
 */
sealed interface NewEntity : Entity {

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): NewEntity
}

/**
 * An [Entity] that has already been inserted into the database.
 *
 * These entities are returned in query operations and used in update and delete operations.
 *
 * Note that there is no guarantee that a reference to these entities in memory still exist in the
 * database as they can be deleted.
 */
sealed interface ExistingEntity : Entity {

    /**
     * The ID of this entity (row) in the table it belongs to.
     */
    val id: Long

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): ExistingEntity

    override val idOrNull: Long?
        get() = id
}

/**
 * An immutable [Entity].
 *
 * Implementations of this interface must guarantee immutability. No properties can change once
 * instances are created. This guarantees thread-safety.
 */
sealed interface ImmutableEntity : Entity {

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): ImmutableEntity
}

/**
 * An [ImmutableEntity] that has a mutable type [T].
 *
 * To get a mutable copy of the corresponding mutable type [T], use the [mutableCopy] function.
 */
sealed interface ImmutableEntityWithMutableType<T : MutableEntity> : ImmutableEntity {

    /**
     * Returns a **mutable copy** of this immutable entity. This copy allows for some properties of
     * instances to be mutated/modified.
     *
     * This is typically used for update operations.
     */
    fun mutableCopy(): T

    /**
     * Same as [mutableCopy] except this takes in a function with [T] as the receiver.
     */
    fun mutableCopy(newCopy: T.() -> Unit): T = mutableCopy().apply(newCopy)

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): ImmutableEntityWithMutableType<T>
}

/**
 * An [ImmutableEntity] that has a mutable type [T] that may or may not be null.
 *
 * To get a mutable copy of the corresponding mutable type [T], use the [mutableCopy] function.
 */
sealed interface ImmutableEntityWithNullableMutableType<T : MutableEntity> : ImmutableEntity {

    /**
     * Returns a **mutable copy** of this immutable entity. This copy allows for some properties of
     * instances to be mutated/modified.
     *
     * The return value of this may be null. This is useful in cases where a mutable copy should
     * only be produced under certain conditions.
     */
    fun mutableCopy(): T?

    /**
     * Same as [mutableCopy] except this takes in a function with [T] as the receiver.
     */
    fun mutableCopy(newCopy: T.() -> Unit): T? = mutableCopy()?.apply(newCopy)

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): ImmutableEntityWithNullableMutableType<T>
}

/**
 * A mutable [Entity].
 *
 * Implementations of this interface must have at least one property that is mutable. Otherwise, it
 * should instead be an [ImmutableEntity].
 *
 * This is **NOT thread-safe**. You must perform synchronizations yourself if you are trying to use
 * shared instances of this in multi-threaded environments.
 */
sealed interface MutableEntity : Entity {

    // We have to cast the return type because we are not using recursive generic types.
    override fun redactedCopy(): MutableEntity
}

/**
 * Returns a reference to this list if it is an instance of [MutableList]. Otherwise, it returns a
 * new [MutableList] instance with the same contents as this list.
 *
 * This is useful for saving a reference to the same mutable list (if it is an instance of it) so
 * that modifications to the same mutable list can be made in multiple places.
 */
fun <T : Entity> List<T>.asMutableList(): MutableList<T> = if (this is MutableList) {
    this
} else {
    toMutableList()
}

/**
 * Returns an immutable list containing mutable copies of type [T] for each instance of type [R] in
 * the list.
 */
fun <T : MutableEntity, R : ImmutableEntityWithMutableType<T>> Collection<R>.mutableCopies():
        List<T> = map { it.mutableCopy() }

/**
 * Returns a sequence containing mutable copies of type [T] for each instance of type [R] in the
 * sequence.
 */
fun <T : MutableEntity, R : ImmutableEntityWithMutableType<T>> Sequence<R>.mutableCopies():
        Sequence<T> = map { it.mutableCopy() }

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

/**
 * TODO documentation
 */
internal fun Any?.isNotNullOrBlank(): Boolean = when (this) {
    null -> false
    is Entity -> !this.isBlank
    is String -> this.isNotBlank()
    is Collection<*> -> this.isNotNullOrBlank()
    else -> true
}

// TODO migrate all of the below functions to use all, any, none and add documentation
// https://play.kotlinlang.org/byExample/05_Collections/05_existential
// Make some temporary tests to ensure the new function versions match the output of the old.
// Make sure to test the case where the list is empty!!!

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