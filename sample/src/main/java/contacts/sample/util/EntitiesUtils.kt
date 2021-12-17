package contacts.sample.util

import contacts.core.entities.ContactEntity
import contacts.core.entities.ExistingContactEntity
import contacts.core.entities.ExistingRawContactEntity
import contacts.core.entities.RawContactEntity

// TODO Think extracting these extensions for consumer use.

suspend fun <R> ContactEntity?.runIfExist(block: suspend (ExistingContactEntity) -> R?): R? =
    if (this != null && this is ExistingContactEntity) {
        block(this)
    } else {
        null
    }

suspend fun <R> RawContactEntity?.runIfExist(block: suspend (ExistingRawContactEntity) -> R?): R? =
    if (this != null && this is ExistingRawContactEntity) {
        block(this)
    } else {
        null
    }