package contacts.core.entities

import android.os.Parcelable

/**
 * Type of all entities provided in this library.
 */
sealed interface Entity : Parcelable {

    /**
     * The ID of this entity (row) in the table it belongs to.
     */
    val id: Long?

    /**
     * Returns true all property values are either null, empty, or blank.
     *
     * The [id] has no influence on the value this returns.
     */
    val isBlank: Boolean
}

/**
 * An immutable [Entity].
 *
 * Implementations of this interface must guarantee immutability. No properties can change once
 * instances are created. This guarantees thread-safety.
 */
sealed interface ImmutableEntity : Entity

/**
 * An [ImmutableEntity] that has a mutable type [T].
 *
 * To get a mutable copy of the corresponding mutable type [T], use the [mutableCopy] function.
 */
sealed interface ImmutableEntityWithMutableType<T : MutableEntity> : ImmutableEntity {

    /**
     * Returns a **mutable copy** of this immutable entity. This copy allows for some properties of
     * instances to be mutated/modified.
     */
    fun mutableCopy(): T
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
sealed interface MutableEntity : Entity

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