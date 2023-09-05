package contacts.core.util

import contacts.core.entities.ExistingDataEntity
import contacts.core.entities.ExistingRawContactEntity

/**
 * Sequence of all data kinds (e.g. addresses, emails, events, etc) of this
 * [ExistingRawContactEntity].
 */
fun ExistingRawContactEntity.data(): Sequence<ExistingDataEntity> = sequence {
    yieldAll(addresses.filterIsInstance(ExistingDataEntity::class.java))
    yieldAll(emails.filterIsInstance(ExistingDataEntity::class.java))
    yieldAll(events.filterIsInstance(ExistingDataEntity::class.java))
    // Group memberships are implicitly read-only.
    yieldAll(ims.filterIsInstance(ExistingDataEntity::class.java))
    (name as? ExistingDataEntity)?.also { yield(it) }
    (nickname as? ExistingDataEntity)?.also { yield(it) }
    (note as? ExistingDataEntity)?.also { yield(it) }
    (organization as? ExistingDataEntity)?.also { yield(it) }
    yieldAll(phones.filterIsInstance(ExistingDataEntity::class.java))
    // Photo is implicitly read-only.
    yieldAll(relations.filterIsInstance(ExistingDataEntity::class.java))
    (sipAddress as? ExistingDataEntity)?.also { yield(it) }
    yieldAll(websites.filterIsInstance(ExistingDataEntity::class.java))

    yieldAll(
        customDataEntities.values
            .flatMap { it.entities }
            .filterIsInstance(ExistingDataEntity::class.java)
    )
}

/**
 * Same as [ExistingRawContactEntity.data] but as a [List].
 */
fun ExistingRawContactEntity.dataList(): List<ExistingDataEntity> = data().toList()