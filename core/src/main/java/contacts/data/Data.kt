package contacts.data

import android.content.Context
import contacts.ContactsPermissions
import contacts.entities.MimeType
import contacts.entities.custom.CustomCommonDataRegistry

/**
 * Provides new [DataQuery], [DataUpdate], and [DataDelete] for Profile OR non-Profile (depending on
 * instance) operations.
 *
 * Note that there is no DataInsert as data is required to be associated with a RawContact.
 * See [contacts.Insert]
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
     * Returns a new [DataQuery] instance for Profile OR non-Profile (depending on instance) data
     * queries.
     */
    fun query(): DataQuery

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

    /**
     * Provides functions required to support custom common data, which have [MimeType.Custom].
     */
    val customDataRegistry: CustomCommonDataRegistry
}

@Suppress("FunctionName")
internal fun Data(
    context: Context, customDataRegistry: CustomCommonDataRegistry, isProfile: Boolean
): Data = DataImpl(
    context.applicationContext,
    ContactsPermissions(context.applicationContext),
    customDataRegistry,
    isProfile
)

private class DataImpl(
    override val applicationContext: Context,
    override val permissions: ContactsPermissions,
    override val customDataRegistry: CustomCommonDataRegistry,
    private val isProfile: Boolean
) : Data {

    override fun query() = DataQuery(applicationContext, customDataRegistry, isProfile)

    override fun update() = DataUpdate(applicationContext, customDataRegistry, isProfile)

    override fun delete() = DataDelete(applicationContext, isProfile)
}