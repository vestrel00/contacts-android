package contacts.core.profile

import contacts.core.Contacts
import contacts.core.RawContactsQuery
import contacts.core.data.Data

/**
 * Provides new [ProfileQuery], [ProfileInsert], [ProfileUpdate], and [ProfileDelete] instances.
 *
 * Also [RawContactsQuery] and [Data].
 *
 * ## Permissions
 *
 * - Add the "android.permission.READ_CONTACTS" to the AndroidManifest in order to [query]
 * and [rawContactsQuery].
 *     - For API 22 and below, the permission "android.permission.READ_PROFILE" is also required.
 * - Add the "android.permission.WRITE_CONTACTS" to the AndroidManifest in order to [insert],
 * [update], and [delete].
 *     - For API 22 and below, the permission "android.permission.WRITE_PROFILE" is also required.
 */
interface Profile {

    /**
     * Returns a new [ProfileQuery] instance.
     */
    fun query(): ProfileQuery

    /**
     * Returns a new [RawContactsQuery] instance for profile operations.
     */
    fun rawContactsQuery(): RawContactsQuery

    /**
     * Returns a new [ProfileInsert] instance.
     */
    fun insert(): ProfileInsert

    /**
     * Returns a new [ProfileUpdate] instance.
     */
    fun update(): ProfileUpdate

    /**
     * Returns a new [ProfileDelete] instance.
     */
    fun delete(): ProfileDelete

    /**
     * Returns a new [Data] instance for Profile data operations.
     */
    fun data(): Data

    /**
     * A reference to the [Contacts] instance that constructed this. This is mostly used internally
     * to shorten internal code.
     *
     * Don't worry, [Contacts] does not keep references to instances of this. There are no circular
     * references that could cause leaks =). [Contacts] is just a factory.
     */
    val contactsApi: Contacts
}

internal fun Profile(contacts: Contacts): Profile = ProfileImpl(contacts)

private class ProfileImpl(override val contactsApi: Contacts) : Profile {

    override fun query() = ProfileQuery(contactsApi)

    override fun rawContactsQuery() = RawContactsQuery(contactsApi, true)

    override fun insert() = ProfileInsert(contactsApi)

    override fun update() = ProfileUpdate(contactsApi)

    override fun delete() = ProfileDelete(contactsApi)

    override fun data() = Data(contactsApi, true)
}