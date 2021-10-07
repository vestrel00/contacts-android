package contacts.core.groups

import android.content.Context
import android.os.Build
import contacts.core.ContactsPermissions

/**
 * Provides new [GroupsQuery], [GroupsInsert], [GroupsUpdate], and [GroupsDelete] instances.
 *
 * ## Permissions
 *
 * - Add the "android.permission.READ_CONTACTS" to the AndroidManifest in order to [query].
 * - Add the "android.permission.WRITE_CONTACTS" to the AndroidManifest in order to [insert],
 *   [update], and [delete].
 *
 * Use [permissions] convenience functions to check for required permissions.
 *
 * ## Deleting Groups
 *
 * Deleting groups, even user-created groups, is currently not supported due to some limitations
 * imposed by the Contacts Provider. For more details, see the DEV_NOTES "Groups; Deletion" section.
 */
interface Groups {

    /**
     * Returns a new [GroupsQuery] instance.
     */
    fun query(): GroupsQuery

    /**
     * Returns a new [GroupsInsert] instance.
     */
    fun insert(): GroupsInsert

    /**
     * Returns a new [GroupsUpdate] instance.
     */
    fun update(): GroupsUpdate

    /**
     * Returns a new [GroupsDelete] instance if API level is 26 or above. Returns null otherwise.
     */
    fun delete(): GroupsDelete?

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
internal fun Groups(context: Context): Groups = GroupsImpl(
    context.applicationContext,
    ContactsPermissions(context.applicationContext)
)

private class GroupsImpl(
    override val applicationContext: Context,
    override val permissions: ContactsPermissions
) : Groups {

    override fun query() = GroupsQuery(applicationContext)

    override fun insert() = GroupsInsert(applicationContext)

    override fun update() = GroupsUpdate(applicationContext)

    override fun delete() = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        GroupsDelete(applicationContext)
    } else {
        null
    }
}