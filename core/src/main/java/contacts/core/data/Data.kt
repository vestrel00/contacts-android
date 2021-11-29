package contacts.core.data

import android.content.Context
import contacts.core.Contacts
import contacts.core.ContactsPermissions

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
     * Returns a [ContactsPermissions] instance, which provides functions for checking required
     * permissions.
     */
    val permissions: ContactsPermissions

    /**
     * Reference to the Application's Context for use in extension functions and external library
     * modules. This is safe to hold on to. Not meant for consumer use.
     */
    val applicationContext: Context
}

@Suppress("FunctionName")
internal fun Data(contacts: Contacts, isProfile: Boolean): Data = DataImpl(contacts, isProfile)

private class DataImpl(
    private val contacts: Contacts,
    private val isProfile: Boolean
) : Data {

    override fun query() = DataQuery(contacts, isProfile)

    override fun update() = DataUpdate(contacts, isProfile)

    override fun delete() = DataDelete(contacts, isProfile)

    override val permissions: ContactsPermissions = contacts.permissions

    override val applicationContext: Context = contacts.applicationContext
}