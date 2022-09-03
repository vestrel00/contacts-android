package contacts.core.util

import contacts.core.entities.NewName
import contacts.core.entities.NewPhone
import contacts.core.entities.NewRawContact
import contacts.core.entities.SimContactEntity

/**
 * Returns a new [NewRawContact] instance that may be used for insertion.
 */
fun SimContactEntity.toNewRawContact() = NewRawContact().also {
    it.setName(NewName(displayName = name))
    it.addPhone(NewPhone(type = null, number = number))
}

/**
 * Returns [this] collection of [SimContactEntity]s as list of [NewRawContact] that may be used for
 * insertion.
 */
fun Collection<SimContactEntity>.toNewRawContacts(): List<NewRawContact> =
    map { it.toNewRawContact() }


/**
 * Returns [this] sequence of [SimContactEntity]s as list of [NewRawContact] that may be used for
 * insertion.
 */
fun Sequence<SimContactEntity>.toNewRawContacts(): Sequence<NewRawContact> =
    map { it.toNewRawContact() }