package com.vestrel00.contacts.entities

/**
 * [Entity] in the data table that belong to a [RawContact].
 */
interface DataEntity : Entity {

    /**
     * The id of the [RawContact] that this data entity is associated with.
     */
    val rawContactId: Long

    /**
     * The id of the [Contact] that this data entity is associated with.
     */
    val contactId: Long
}