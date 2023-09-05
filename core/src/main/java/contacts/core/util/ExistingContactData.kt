package contacts.core.util

import contacts.core.entities.ExistingContactEntity
import contacts.core.entities.ExistingDataEntity

/**
 * Sequence of all data kinds (e.g. addresses, emails, events, etc) of all of this
 * [ExistingContactEntity.rawContacts].
 */
fun ExistingContactEntity.data(): Sequence<ExistingDataEntity> = sequence {
    yieldAll(rawContacts.flatMap { it.data() })
}

/**
 * Same as [ExistingContactEntity.data] but as a [List].
 */
fun ExistingContactEntity.dataList(): List<ExistingDataEntity> = data().toList()