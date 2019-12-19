package com.vestrel00.contacts.entities

/**
 * Type of all entities provided in this library.
 */
interface Entity {

    /**
     * The ID of this entity (row) in the table it belongs to.
     */
    val id: Long

    /**
     * Returns true if this entity has a valid id, which indicates that it is (or was at the time of
     * retrieval) an existing entity in the database.
     */
    fun hasValidId(): Boolean = id > INVALID_ID

    interface Type {
        val value: Int
    }
}

internal const val INVALID_ID: Long = -1L