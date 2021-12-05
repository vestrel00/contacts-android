package contacts.core.util

import contacts.core.*
import contacts.core.data.resolveDataEntity
import contacts.core.entities.*

/**
 * Returns the [ImmutableDataEntity] [T] with all of the latest data.
 *
 * This is useful for getting the latest data after performing an update. This may return null if
 * the data entity no longer exists or if permission is not granted.
 *
 * Returns itself if the [ImmutableDataEntity.id] is null, indicating that this DataEntity instance
 * has not yet been inserted to the DB.
 *
 * Supports profile/non-profile native/custom data.
 *
 * ## For existing (inserted) entities only
 *
 * This function will only work for entities that have already been inserted into the Contacts
 * Provider database. This means that this is only for entities that have been retrieved using
 * query or result APIs.
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
fun <T : ImmutableDataEntity> T.refresh(contacts: Contacts, cancel: () -> Boolean = { false }): T? {
    val dataId = id
    return if (dataId == null) {
        this
    } else if (!contacts.permissions.canQuery()) {
        null
    } else {
        fetchDataEntity<T>(contacts, dataId, cancel)
    }
}

@JvmOverloads
fun <T : MutableDataEntity> T.refresh(contacts: Contacts, cancel: () -> Boolean = { false }): T? {
    val dataId = id
    return if (dataId == null) {
        this
    } else if (!contacts.permissions.canQuery()) {
        null
    } else {
        val immutableDataEntity = fetchDataEntity<ImmutableDataEntity>(contacts, dataId, cancel)

        @Suppress("UNCHECKED_CAST")
        when (immutableDataEntity) {
            null -> null
            is ImmutableDataEntityWithMutableType<*> -> immutableDataEntity.mutableCopy() as T
            is ImmutableDataEntityWithNullableMutableType<*> -> immutableDataEntity.mutableCopy() as T?
            else -> null
        }
    }
}

private fun <T : ImmutableDataEntity> DataEntity.fetchDataEntity(
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