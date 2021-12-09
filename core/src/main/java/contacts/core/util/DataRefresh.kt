package contacts.core.util

import contacts.core.*
import contacts.core.data.resolveDataEntity
import contacts.core.entities.*

/**
 * Returns the [ExistingDataEntity] [T] with all of the latest data.
 *
 * This is useful for getting the latest data after performing an update. This may return null if
 * the data entity no longer exists or if permission is not granted.
 *
 * Supports profile/non-profile native/custom data.
 *
 * ## Permissions
 *
 * The [contacts.core.ContactsPermissions.READ_PERMISSION] is required. Otherwise, null will be
 * returned if the permission is not granted.
 *
 * ## Thread Safety
 *
 * This should be called in a background thread to avoid blocking the UI thread.
 */
// [ANDROID X] @WorkerThread (not using annotation to avoid dependency on androidx.annotation)
@JvmOverloads
fun <T : ExistingDataEntity> T.refresh(contacts: Contacts, cancel: () -> Boolean = { false }): T? {
    return if (!contacts.permissions.canQuery()) {
        null
    } else {
        val existingDataEntity = fetchDataEntity(contacts, id, cancel)

        val existingDataEntityIsImmutable = existingDataEntity is ImmutableDataEntity
        val resultShouldBeMutable = this is MutableDataEntity

        @Suppress("UNCHECKED_CAST")
        return if (existingDataEntityIsImmutable && resultShouldBeMutable) {
            // Need to cast to mutable entity
            when (existingDataEntity) {
                null -> null
                is ImmutableDataEntityWithMutableType<*> -> existingDataEntity.mutableCopy() as T
                is ImmutableDataEntityWithNullableMutableType<*> -> existingDataEntity.mutableCopy() as T?
                else -> existingDataEntity
            }
        } else {
            existingDataEntity
        }
    }
}

private fun <T : ExistingDataEntity> T.fetchDataEntity(
    contacts: Contacts,
    dataId: Long,
    cancel: () -> Boolean = { false }
): T? = contacts.resolveDataEntity<T>(
    isProfile,
    mimeType,
    null,
    Include(fields(contacts.customDataRegistry) + Fields.Required.all),
    Fields.DataId equalTo dataId,
    CompoundOrderBy(setOf(Fields.DataId.asc())),
    1,
    0,
    cancel
).firstOrNull()

/* DEV NOTE
We could declare and implement a single function instead of two by using the generic type...
fun <T: ContactEntity> T.refresh(contacts: Contacts, cancel: () -> Boolean = { false }): T? =
However, unsafe type casting is required, which I'd rather avoid =)

As of Kotlin 1.6.0...

sealed interface Human
class Male : Human
class Female : Human

// This is clean but does not return concrete type.
fun Human.refresh(): Human = when (this) {
    is Male -> Male()
    is Female -> Female()
}

// This returns concrete type but requires unchecked cast. Also the when statement asks for an else
// branch even though the interface is sealed...
@Suppress("UNCHECKED_CAST")
fun <T : Human> T.refresh(): T = when (this) {
    is Male -> Male()
    is Female -> Female()
    else -> throw UnknownHumanException()
} as T

// Inlining to use reified does not help. Plus, this is not Java-friendly =(
@Suppress("UNCHECKED_CAST")
inline fun <reified T : Human> T.refresh(): T = when (T::class) {
    Male::class -> Male()
    Female::class -> Female()
    else -> throw UnknownHumanException()
} as T

So... we will keep things this way until Kotlin supports the following code (if ever),
fun <T : Human> T.refresh(): T = when (this) {
    is Male -> Male()
    is Female -> Female()
}
*/