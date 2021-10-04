package contacts.core

import android.content.Context
import contacts.core.data.Data
import contacts.core.entities.MimeType
import contacts.core.entities.custom.CustomDataRegistry
import contacts.core.entities.custom.GlobalCustomDataRegistry
import contacts.core.groups.Groups
import contacts.core.profile.Profile

/**
 * Provides new [Query], [BroadQuery], [Insert], [Update], [Delete], [Data], [Groups], and
 * [Profile] instances.
 *
 * ## Permissions
 *
 * - Add the "android.permission.READ_CONTACTS" to the AndroidManifest in order to [query] and
 *   [broadQuery].
 * - Add the "android.permission.WRITE_CONTACTS" to the AndroidManifest in order to [insert],
 *   [update], and [delete].
 *
 * Use [permissions] convenience functions to check for required permissions. The same permissions
 * apply to [Data], [Groups], and [Profile].
 *
 * ## Data
 *
 * For data-specific operations, use [data].
 *
 * ## Groups
 *
 * For group operations, use [groups].
 *
 * ## Profile
 *
 * For user profile operations, use [profile].
 */
interface Contacts {

    /**
     * Returns a new [Query] instance.
     */
    fun query(): Query

    /**
     * Returns a new [BroadQuery] instance.
     */
    fun broadQuery(): BroadQuery

    /**
     * Returns a new [Insert] instance.
     */
    fun insert(): Insert

    /**
     * Returns a new [Update] instance.
     */
    fun update(): Update

    /**
     * Returns a new [Delete] instance.
     */
    fun delete(): Delete

    /**
     * Returns a new [Data] instance for non-Profile data operations.
     */
    fun data(): Data

    /**
     * Returns a new [Groups] instance.
     */
    fun groups(): Groups

    /**
     * Returns a new [Profile] instance.
     */
    fun profile(): Profile

    /**
     * Returns a [ContactsPermissions] instance, which provides functions for checking required
     * permissions.
     */
    val permissions: ContactsPermissions

    /**
     * Reference to the Application's Context for use in extension functions and external library
     * modules. This is safe to hold on to. Not meant for consumer use.
     *
     * ## Developer notes
     *
     * It's safe to save a hard reference to the Application context as it is alive for as long as
     * the app is alive. No need to make this a weak reference and make our lives more difficult
     * for no reason. Other libraries do the same; e.g. coil.
     *
     * Don't believe me? Then read the official Android documentation about this posted back in
     * 2009; https://android-developers.googleblog.com/2009/01/avoiding-memory-leaks.html
     *
     * Obviously, we should not save a reference to any Activity context.
     *
     * Consumers of this should still use [Context.getApplicationContext] for redundancy, which
     * provides further protection.
     */
    val applicationContext: Context

    /**
     * Provides functions required to support custom common data, which have [MimeType.Custom].
     */
    val customDataRegistry: CustomDataRegistry
}

/**
 * Creates a new [Contacts] instance.
 */
@JvmOverloads
@Suppress("FunctionName")
fun Contacts(
    context: Context, customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
): Contacts = ContactsImpl(
    context.applicationContext,
    ContactsPermissions(context.applicationContext),
    customDataRegistry
)

/**
 * Creates a new [Contacts] instance.
 *
 * This is mainly for Java convenience. Kotlin users should use [Contacts] function instead.
 */
object ContactsFactory {

    @JvmStatic
    @JvmOverloads
    fun create(
        context: Context, customDataRegistry: CustomDataRegistry = GlobalCustomDataRegistry
    ): Contacts = Contacts(context, customDataRegistry)
}

private class ContactsImpl(
    override val applicationContext: Context,
    override val permissions: ContactsPermissions,
    override val customDataRegistry: CustomDataRegistry
) : Contacts {

    override fun query() = Query(applicationContext, customDataRegistry)

    override fun broadQuery() = BroadQuery(applicationContext, customDataRegistry)

    override fun insert() = Insert(applicationContext, customDataRegistry)

    override fun update() = Update(applicationContext, customDataRegistry)

    override fun delete() = Delete(applicationContext)

    override fun data() = Data(applicationContext, customDataRegistry, false)

    override fun groups() = Groups(applicationContext)

    override fun profile() = Profile(applicationContext, customDataRegistry)
}
