package contacts.core.data

import contacts.core.Contacts

/**
 * Provides new [DataQueryFactory], [DataUpdate], and [DataDelete] for Profile OR non-Profile (depending on
 * instance) operations.
 *
 * Note that there is no DataInsert as data is required to be associated with a RawContact.
 * See [contacts.core.Insert]
 *
 * ## Permissions
 *
 * - Add the "android.permission.READ_CONTACTS" to the AndroidManifest in order to [query].
 * - Add the "android.permission.WRITE_CONTACTS" to the AndroidManifest in order to [update] and
 * [delete].
 *
 * Use [permissions] convenience functions to check for required permissions.
 */
interface Data {

    /**
     * Returns a new [DataQueryFactory] instance for Profile OR non-Profile (depending on instance) data
     * queries.
     */
    fun query(): DataQueryFactory

    /**
     * Returns a new [DataUpdate] instance for Profile OR non-Profile (depending on instance) data
     * updates.
     */
    fun update(): DataUpdate

    /**
     * Returns a new [DataDelete] instance for Profile OR non-Profile (depending on instance) data
     * deletes.
     */
    fun delete(): DataDelete

    /**
     * A reference to the [Contacts] instance that constructed this. This is mostly used internally
     * to shorten internal code.
     *
     * Don't worry, [Contacts] does not keep references to instances of this. There are no circular
     * references that could cause leaks =). [Contacts] is just a factory.
     */
    val contactsApi: Contacts
}

@Suppress("FunctionName")
internal fun Data(contacts: Contacts, isProfile: Boolean): Data = DataImpl(contacts, isProfile)

private class DataImpl(
    override val contactsApi: Contacts,
    private val isProfile: Boolean
) : Data {

    override fun query() = DataQueryFactory(contactsApi, isProfile)

    override fun update() = DataUpdate(contactsApi, isProfile)

    override fun delete() = DataDelete(contactsApi, isProfile)
}