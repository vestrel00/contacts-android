package contacts.core.entities.mapper

import contacts.core.entities.Entity

internal interface EntityMapper<out T : Entity> {
    val value: T
}