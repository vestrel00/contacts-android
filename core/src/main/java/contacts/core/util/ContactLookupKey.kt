package contacts.core.util

import contacts.core.*
import contacts.core.entities.ExistingContactEntity

/**
 * Returns a [Where] clause for finding Contacts with one of the given [lookupKeys]. Returns null if
 * the [lookupKeys] is empty.
 *
 * For example;
 *
 * ```kotlin
 * val contacts = query.where { Contact.lookupKeyIn(lookupKey) }.find()
 * ```
 *
 * For more info about why you should use this function instead of directly using
 * [ExistingContactEntity.lookupKey] in queries, read documentation in [decomposedLookupKeys].
 *
 * ## Use Contact ID as fallback!
 *
 * For example;
 *
 * ```kotlin
 * val contacts = query.where { Contact.lookupKeyIn(lookupKey)?.or(Contact.Id equalTo id) }.find()
 * ```
 *
 * The reason for doing this is because the ContactsProvider may assign a different value to the
 * Contact lookup key if it's constituent RawContacts that are not associated with an Account
 * (local, unsynced, source id is null) gets its primary display name source updated. Display name
 * sources are specified in `ContactsContract.DisplayNameSources`. In order of increasing priority;
 * email, phone, organization, nickname, and name.
 */
fun DataContactsFields.lookupKeyIn(vararg lookupKeys: String): Where<DataContactsField>? =
    lookupKeyIn(lookupKeys.asSequence())

/**
 * See [DataContactsFields.lookupKeyIn].
 */
fun DataContactsFields.lookupKeyIn(lookupKeys: Collection<String>): Where<DataContactsField>? =
    lookupKeyIn(lookupKeys.asSequence())

/**
 * See [DataContactsFields.lookupKeyIn].
 */
fun DataContactsFields.lookupKeyIn(lookupKeys: Sequence<String>): Where<DataContactsField>? =
    decomposedLookupKeys(lookupKeys) whereOr { LookupKey contains it }

/**
 * Returns a [Where] clause for finding Contacts with one of the given [lookupKeys]. Returns null if
 * the [lookupKeys] is empty.
 *
 * For example;
 *
 * ```kotlin
 * val contacts = query.where { Contact.lookupKeyIn(lookupKey) }.find()
 * ```
 *
 * For more info about why you should use this function instead of directly using
 * [ExistingContactEntity.lookupKey] in queries, read documentation in [decomposedLookupKeys].
 *
 * ## Use Contact ID as fallback!
 *
 * For example;
 *
 * ```kotlin
 * val contacts = query.where { Contact.lookupKeyIn(lookupKey)?.or(Contact.Id equalTo id) }.find()
 * ```
 *
 * The reason for doing this is because the ContactsProvider may assign a different value to the
 * Contact lookup key if it's constituent RawContacts that are not associated with an Account
 * (local, unsynced, source id is null) gets its primary display name source updated. Display name
 * sources are specified in `ContactsContract.DisplayNameSources`. In order of increasing priority;
 * email, phone, organization, nickname, and name.
 */
fun ContactsFields.lookupKeyIn(vararg lookupKeys: String): Where<ContactsField>? =
    lookupKeyIn(lookupKeys.asSequence())

/**
 * See [ContactsFields.lookupKeyIn].
 */
fun ContactsFields.lookupKeyIn(lookupKeys: Collection<String>): Where<ContactsField>? =
    lookupKeyIn(lookupKeys.asSequence())

/**
 * See [ContactsFields.lookupKeyIn].
 */
fun ContactsFields.lookupKeyIn(lookupKeys: Sequence<String>): Where<ContactsField>? =
    decomposedLookupKeys(lookupKeys) whereOr { LookupKey contains it }

/**
 * The [ExistingContactEntity.lookupKey] is a String that contains one or more lookup keys.
 * Therefore, it needs to be "decomposed" or broken up into individual lookup keys so that it
 * can be used for queries. Use this function to decompose all of the given [lookupKeys].
 *
 * ## What's going on here?
 *
 * Let's say that there are two Contacts with one RawContact each,
 *
 * - Contact A, lookupKey: 2059i6f5de8460f7f227e
 * - Contact B, lookupKey: 0r62-2A2C2E
 *
 * Getting the Contact by lookup key is straightforward in this case,
 *
 * ```kotlin
 * val contactA = query.where { Contact.LookupKey equalTo "2059i6f5de8460f7f227e" }.find().first()
 * val contactB = query.where { Contact.LookupKey equalTo "0r62-2A2C2E" }.find().first()
 * ```
 *
 * However, if Contact A and Contact B are linked, their lookupKeys will be combined separated by
 * a '.',
 *
 * - Contact A&B, lookupKey: "2059i6f5de8460f7f227e.0r62-2A2C2E"
 *
 * Therefore, getting the linked Contact by lookup key should use `contains` instead of `equalTo`,
 *
 * ```kotlin
 * val contactAB = query.where { Contact.LookupKey contains "2059i6f5de8460f7f227e" }.find().first()
 * val contactAB = query.where { Contact.LookupKey contains "0r62-2A2C2E" }.find().first()
 * ```
 *
 * BUT what happens if we are trying to get a Contact using a combined lookupKey after the linked
 * Contact has been unlinked?
 *
 * - Contact A, lookupKey: 2059i6f5de8460f7f227e
 * - Contact B, lookupKey: 0r62-2A2C2E
 *
 * ```kotlin
 * val contact = query.where { Contact.LookupKey contains "2059i6f5de8460f7f227e.0r62-2A2C2E" }.find()
 * ```
 *
 * The above query will not match anything!
 *
 * Thus, we must ensure that the lookup key is decomposed and use `whereOr` and `contains` in the
 * query,
 *
 * ```kotlin
 * val contacts = query.where { listOf("2059i6f5de8460f7f227e", "0r62-2A2C2E") whereOr { Contact.LookupKey contains it } }.find()
 * ```
 *
 * Note that in this case, two contacts will be returned using a single lookup key because the
 * contact has been unlink into two separate contacts.
 *
 * This function ensures that the lookup key is decomposed into a list of "separate lookup keys".
 *
 * ```kotlin
 * val contacts = query.where { decomposedLookupKeys(lookupKeys) whereOr { Contact.LookupKey contains it } }.find()
 * ```
 */
fun decomposedLookupKeys(vararg lookupKeys: String): List<String> =
    lookupKeys.flatMap { it.decomposedLookupKey() }

/**
 * See [decomposedLookupKeys].
 */
fun decomposedLookupKeys(lookupKeys: Collection<String>): List<String> =
    lookupKeys.flatMap { it.decomposedLookupKey() }

/**
 * See [decomposedLookupKeys].
 */
fun decomposedLookupKeys(lookupKeys: Sequence<String>): Sequence<String> =
    lookupKeys.flatMap { it.decomposedLookupKey() }

/**
 * See [decomposedLookupKeys].
 */
fun ExistingContactEntity.decomposedLookupKeys(): List<String> =
    lookupKey?.decomposedLookupKey() ?: emptyList()

private fun String.decomposedLookupKey(): List<String> = split('.')