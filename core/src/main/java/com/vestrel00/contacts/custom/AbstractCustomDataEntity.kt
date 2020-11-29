package com.vestrel00.contacts.custom

import com.vestrel00.contacts.entities.CommonDataEntity

/**
 * An abstract [CommonDataEntity] that may be used as a base. This is optional. Consumers may
 * implement the [CommonDataEntity] directly.
 *
 * This is useful for Java consumers as it provides getters instead of having to implement it.
 */
abstract class AbstractCustomDataEntity(
    override val id: Long?,
    override val rawContactId: Long?,
    override val contactId: Long?,
    override val isPrimary: Boolean,
    override val isSuperPrimary: Boolean
) : CommonDataEntity