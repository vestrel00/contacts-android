package contacts.core.groups

import contacts.core.Contacts

/**
 * Provides new [GroupsQuery], [GroupsInsert], [GroupsUpdate], and [GroupsDelete] instances.
 *
 * ## Permissions
 *
 * - Add the "android.permission.READ_CONTACTS" to the AndroidManifest in order to [query].
 * - Add the "android.permission.WRITE_CONTACTS" to the AndroidManifest in order to [insert],
 *   [update], and [delete].
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
     * Returns a new [GroupsDelete] instance.
     */
    fun delete(): GroupsDelete

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
internal fun Groups(contacts: Contacts): Groups = GroupsImpl(contacts)

private class GroupsImpl(override val contactsApi: Contacts) : Groups {

    override fun query() = GroupsQuery(contactsApi)

    override fun insert() = GroupsInsert(contactsApi)

    override fun update() = GroupsUpdate(contactsApi)

    override fun delete() = GroupsDelete(contactsApi)
}