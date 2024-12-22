package contacts.core.util

import contacts.core.entities.ExistingDataEntity
import contacts.core.entities.ExistingRawContactEntity

/**
 * Sequence of all data kinds (e.g. addresses, emails, events, etc) of this
 * [ExistingRawContactEntity].
 */
fun ExistingRawContactEntity.data(): Sequence<ExistingDataEntity> = sequence {
    yieldAll(addresses.filterIsInstance<ExistingDataEntity>())
    yieldAll(emails.filterIsInstance<ExistingDataEntity>())
    yieldAll(events.filterIsInstance<ExistingDataEntity>())
    // Group memberships are implicitly read-only.
    yieldAll(@Suppress("Deprecation") ims.filterIsInstance<ExistingDataEntity>())
    (name as? ExistingDataEntity)?.also { yield(it) }
    (nickname as? ExistingDataEntity)?.also { yield(it) }
    (note as? ExistingDataEntity)?.also { yield(it) }
    (organization as? ExistingDataEntity)?.also { yield(it) }
    yieldAll(phones.filterIsInstance<ExistingDataEntity>())
    // Photo is implicitly read-only.
    yieldAll(relations.filterIsInstance<ExistingDataEntity>())
    (@Suppress("Deprecation") sipAddress as? ExistingDataEntity)?.also { yield(it) }
    yieldAll(websites.filterIsInstance<ExistingDataEntity>())

    yieldAll(
        customDataEntities.values
            .flatMap { it.entities }
            .filterIsInstance<ExistingDataEntity>()
    )
}

/**
 * Same as [ExistingRawContactEntity.data] but as a [List].
 */
fun ExistingRawContactEntity.dataList(): List<ExistingDataEntity> = data().toList()