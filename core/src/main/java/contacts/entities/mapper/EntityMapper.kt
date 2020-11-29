package contacts.entities.mapper

import contacts.entities.Entity

internal interface EntityMapper<out T : Entity> {
    val value: T
}