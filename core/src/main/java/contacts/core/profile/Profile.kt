package contacts.core.profile

import android.content.Context
import contacts.core.ContactsPermissions
import contacts.core.data.Data
import contacts.core.entities.MimeType
import contacts.core.entities.custom.CustomDataRegistry

/**
 * Provides new [ProfileQuery], [ProfileInsert], [ProfileUpdate], and [ProfileDelete] instances.
 *
 * ## Permissions
 *
 * - Add the "android.permission.READ_CONTACTS" to the AndroidManifest in order to [query].
 *     - For API 22 and below, the permission "android.permission.READ_PROFILE" is also required.
 * - Add the "android.permission.WRITE_CONTACTS" to the AndroidManifest in order to [insert],
 * [update], and [delete].
 *     - For API 22 and below, the permission "android.permission.WRITE_PROFILE" is also required.
 *
 * Use [permissions] convenience functions to check for required permissions.
 *
 */
interface Profile {

    /**
     * Returns a new [ProfileQuery] instance.
     */
    fun query(): ProfileQuery

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
    val customDataRegistry: CustomDataRegistry
}

@Suppress("FunctionName")
internal fun Profile(context: Context, customDataRegistry: CustomDataRegistry): Profile =
    ProfileImpl(
        context.applicationContext,
        ContactsPermissions(context.applicationContext),
        customDataRegistry
    )

private class ProfileImpl(
    override val applicationContext: Context,
    override val permissions: ContactsPermissions,
    override val customDataRegistry: CustomDataRegistry
) : Profile {

    override fun query() = ProfileQuery(applicationContext, customDataRegistry)

    override fun insert() = ProfileInsert(applicationContext, customDataRegistry)

    override fun update() = ProfileUpdate(applicationContext, customDataRegistry)

    override fun delete() = ProfileDelete(applicationContext)

    override fun data() = Data(applicationContext, customDataRegistry, true)
}