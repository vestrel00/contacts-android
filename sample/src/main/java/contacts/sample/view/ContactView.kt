package contacts.sample.view

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.widget.LinearLayout
import contacts.Contacts
import contacts.Fields
import contacts.async.commitWithContext
import contacts.async.findWithContext
import contacts.async.util.contactWithContext
import contacts.entities.MutableContact
import contacts.entities.MutableRawContact
import contacts.equalTo
import contacts.permissions.deleteWithPermission
import contacts.permissions.insertWithPermission
import contacts.permissions.queryWithPermission
import contacts.permissions.updateWithPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext

// TODO Add top level account details like display name and contact options. Include the contact photo.

/**
 * A (vertical) [LinearLayout] that displays a [MutableContact] and handles the modifications to the
 * given [contact]. Each of the RawContact is displayed in a [RawContactView].
 *
 * Loading the [contact] will automatically update the views. Any modifications in the views will
 * also be made to the [contact].
 *
 * ## Note
 *
 * This is a very rudimentary view that is not styled or made to look good. It may not follow any
 * good practices and may even implement bad practices. Consumers of the library may choose to use
 * this as is or simply as a reference on how to implement this part of native Contacts app.
 *
 * This does not support state retention (e.g. device rotation). The OSS community may contribute to
 * this by implementing it.
 *
 * The community may contribute by styling and adding more features and customizations with these
 * views if desired.
 *
 * ## Developer Notes
 *
 * I usually am a proponent of passive views and don't add any logic to views. However, I will make
 * an exception for this basic view that I don't really encourage consumers to use.
 *
 * This is in the sample and not in the contacts-ui module because it requires concurrency. We
 * should not add coroutines and contacts-async as dependencies to contacts-ui just for this.
 * Consumers may copy and paste this into their projects or if the community really wants it, we may
 * move this to a separate module (contacts-ui-async).
 */
class ContactView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAttr), CoroutineScope {

    /**
     * The Contact that is shown in this view. Setting this will automatically update the views. Any
     * modifications in the views will also be made to the this.
     */
    private var contact: MutableContact? = null
        set(value) {
            field = value

            setContactView()
        }

    /**
     * A RawContactView with a new (empty) RawContact. Used for creating a new Contact.
     */
    private var newRawContactView: RawContactView? = null

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val job: Job = SupervisorJob()

    init {
        orientation = VERTICAL
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        for (index in 0 until childCount) {
            val rawContactView = getChildAt(index) as RawContactView
            rawContactView.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Loads the contact with the given [contactId].
     *
     * Returns true if the load succeeded.
     */
    @JvmOverloads
    suspend fun loadContactWithId(
        contactId: Long, contacts: Contacts = Contacts(context)
    ): Boolean {
        contact = contacts.queryWithPermission()
            .where(Fields.Contact.Id equalTo contactId)
            .findWithContext()
            .firstOrNull()
            ?.toMutableContact()

        return contact != null
    }

    /**
     * Removes any loaded contact and loads an empty new (raw) contact.
     *
     * To insert the new (raw) contact into the Contacts database, call [createNewContact].
     */
    fun loadNewContact() {
        contact = null
    }

    /**
     * Inserts the new (raw) contact to the Contacts database.
     *
     * Returns the newly created contact's ID. Returns null if the insert failed.
     */
    @JvmOverloads
    suspend fun createNewContact(contacts: Contacts = Contacts(context)): Long? {
        val rawContact = newRawContactView?.rawContact ?: return null

        val newContact = contacts.insertWithPermission()
            .allowBlanks(true)
            // TODO .forAccount() reuse AccountsActivity in single choice mode to choose an account
            .rawContacts(rawContact)
            .commitWithContext()
            .contactWithContext(context, rawContact)

        return newContact?.id
    }

    /**
     * Updates the [contact] and all of the associated RawContacts.
     *
     * Returns true if the update succeeded regardless of whether the RawContact photos update
     * succeeded of not.
     */
    @JvmOverloads
    suspend fun updateContact(contacts: Contacts = Contacts(context)): Boolean {
        val contact = contact ?: return false

        // Update photos first so that the (Raw)Contacts does not get deleted if it only has a photo.
        // Blank (Raw)Contacts are by default deleted in updates.
        for (index in 0 until childCount) {
            val rawContactView = getChildAt(index) as RawContactView
            rawContactView.savePhoto()
        }

        // Perform the update. Ignore if photos update succeeded or not :D
        return contacts.updateWithPermission()
            // This is implicitly true by default. We are just being explicitly verbose here.
            .deleteBlanks(true)
            .contacts(contact)
            .commitWithContext()
            .isSuccessful
    }

    /**
     * Deletes the [contact] and all of the associated RawContacts.
     *
     * Returns true if the delete succeeded.
     */
    @JvmOverloads
    suspend fun deleteContact(contacts: Contacts = Contacts(context)): Boolean {
        val contact = contact ?: return false

        return contacts.deleteWithPermission()
            .contacts(contact)
            .commitWithContext()
            .isSuccessful
    }

    private fun setContactView() {
        removeAllViews()

        val contact = contact
        if (contact != null) {
            newRawContactView = null

            contact.rawContacts.forEach { rawContact ->
                addRawContactView(rawContact)
            }
        } else {
            newRawContactView = addRawContactView(MutableRawContact())
        }
    }

    private fun addRawContactView(rawContact: MutableRawContact): RawContactView {
        val rawContactView = RawContactView(context).also {
            it.rawContact = rawContact
        }
        addView(rawContactView)
        return rawContactView
    }
}