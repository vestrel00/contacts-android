package contacts.sample.view

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import contacts.async.commitWithContext
import contacts.async.findWithContext
import contacts.async.util.contactWithContext
import contacts.async.util.optionsWithContext
import contacts.async.util.setOptionsWithContext
import contacts.async.util.updateOptionsWithContext
import contacts.core.Contacts
import contacts.core.Fields
import contacts.core.entities.*
import contacts.core.equalTo
import contacts.core.util.lookupKeyIn
import contacts.permissions.deleteWithPermission
import contacts.permissions.insertWithPermission
import contacts.permissions.queryWithPermission
import contacts.permissions.updateWithPermission
import contacts.sample.R
import contacts.ui.util.onRingtoneSelected
import contacts.ui.util.selectRingtone
import contacts.ui.view.activity
import contacts.ui.view.setThisAndDescendantsEnabled
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * A (vertical) [LinearLayout] that displays a [ContactEntity] and handles the modifications to the
 * given [contact]. Each of the RawContact is displayed in a [RawContactView].
 *
 * Loading the [contact] will automatically update the views. Any modifications in the views will
 * also be made to the [contact] (only if it is mutable).
 *
 * ## View layout
 *
 * From top to bottom;
 *
 * 1. Contact's options; starred, sendToVoicemail, and customRingtone.
 *     - These options may be changed in edit or view mode. The changes are immediate and does not
 *     require pressing the save button.
 * 2. Contact photo.
 * 3. Contact details; display name primary and alternate, and last updated timestamp.
 * 4. List of RawContacts associated with this Contact.
 *
 * ## Contact and RawContact Photos
 *
 * A Contact shows the photo of one of its RawContacts. If the photo of a Contact is changed, then
 * that change is propagated to the corresponding RawContact. Likewise, if the photo of a RawContact
 * (whose photo represents the Contact's photo), then the Contact's photo receives the same change.
 *
 * With this in mind, the native Android Contacts app does not allow changing the Contact and
 * RawContact photo in the same screen (activity/fragment) in order to reduce the code complexity
 * of the UI. As such, this API will not be providing any functions to help with supporting this
 * use case in order to keep code complexity low.
 *
 * This means that when the Contact photo is updated in the view, the RawContact photo displayed
 * will not change. The same applies in the other direction.
 *
 * When the [updateContact] function is invoked, the photos of the RawContact(s) will be saved
 * first. Then, the photo of the Contact will be saved afterwards **only if** there has been a
 * change in the Contact photo view. This mitigates the issue. However, the issue still exists if
 * the Contact photo and RawContact photo have both been modified. In that case, the Contact photo
 * will override the RawContact photo.
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
     * modifications in the views will also be made to the this (only if it is mutable).
     */
    private var contact: ContactEntity? = null

    /**
     * Set the Contact shown and managed by this view to the given [contact] and uses the given
     * [contacts] API to perform operations on it.
     */
    private fun setContact(contact: ContactEntity?, contacts: Contacts) {
        this.contact = contact

        setOptionsView(contacts)
        setDetailsView(contacts)
        setRawContactsView(contacts)
    }

    /**
     * A RawContactView with a new (empty) RawContact. Used for creating a new Contact.
     */
    private var newRawContactView: RawContactView? = null

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.Main

    // options
    private val optionsView: ViewGroup
    private val starredView: ImageView
    private val sendToVoicemailView: CheckBox
    private val customRingtoneView: ImageView

    // details
    private val photoView: ContactPhotoView
    private val displayNamePrimaryView: TextView
    private val displayNameAltView: TextView
    private val lastUpdatedView: TextView

    // RawContacts
    // [ANDROID X] Not using RecyclerView to avoid dependency on androidx.recyclerview.
    // Also, I'm too lazy to use a ListView for this. So, I'm just using a LinearLayout. No view
    // recycling. There shouldn't typically be more than a few (if not just one) RawContact per
    // Contact. We aren't displaying tens, hundreds, or thousands of RawContacts here. Anyways,
    // obviously the most correct way is to use a RecyclerView... But I'm prioritizing simplicity
    // over performance and correctness for this SAMPLE app!
    private val rawContactsView: ViewGroup

    init {
        orientation = VERTICAL
        inflate(context, R.layout.view_contact, this)

        optionsView = findViewById(R.id.options)
        starredView = findViewById(R.id.starred)
        sendToVoicemailView = findViewById(R.id.sendToVoicemail)
        customRingtoneView = findViewById(R.id.customRingtone)

        photoView = findViewById(R.id.photo)

        displayNamePrimaryView = findViewById(R.id.displayNamePrimary)
        displayNameAltView = findViewById(R.id.displayNameAlt)
        lastUpdatedView = findViewById(R.id.lastUpdated)

        rawContactsView = findViewById(R.id.rawContacts)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?, contacts: Contacts) {
        onRingtoneSelected(requestCode, resultCode, data) { ringtoneUri ->
            launch { setCustomRingtone(ringtoneUri, contacts) }
        }

        photoView.onActivityResult(requestCode, resultCode, data)

        for (index in 0 until rawContactsView.childCount) {
            val rawContactView = rawContactsView.getChildAt(index) as RawContactView
            rawContactView.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        // super.setEnabled(enabled) intentionally not calling this
        // Do not disable the contact options and details.
        photoView.isEnabled = enabled
        rawContactsView.setThisAndDescendantsEnabled(enabled)
    }

    /**
     * Loads the contact with the given [lookupKey] using the given [contacts] API.
     *
     * Returns true if the load succeeded.
     */
    suspend fun loadContactWithLookupKey(lookupKey: String, contacts: Contacts): Boolean {
        val contact = contacts.queryWithPermission()
            .where { Contact.lookupKeyIn(lookupKey) }
            .findWithContext()
            .firstOrNull()
            ?.mutableCopy()

        setContact(contact, contacts)

        return contact != null
    }

    /**
     * Removes any loaded contact and loads an empty new (raw) contact.
     *
     * To insert the new (raw) contact into the Contacts database, call [createNewContact].
     */
    fun loadNewContact(contacts: Contacts) {
        setContact(null, contacts)
    }

    /**
     * Inserts the new (raw) contact to the database using the given [contacts] API.
     *
     * Returns the newly created contact's lookup key. Returns null if the insert failed.
     */
    suspend fun createNewContact(contacts: Contacts): String? {
        val rawContact = newRawContactView?.rawContact
        if (rawContact == null || rawContact !is NewRawContact) {
            // Only new RawContacts can be inserted.
            return null
        }

        val newContact = contacts.insertWithPermission()
            .allowBlanks(true)
            // TODO .forAccount() reuse AccountsActivity in single choice mode to choose an account
            .rawContacts(rawContact)
            .commitWithContext()
            .contactWithContext(contacts, rawContact)

        // TODO Make sure that if a contact only has a photo, that it still gets inserted!

        return newContact?.lookupKey
    }

    /**
     * Updates the [contact] and all of the associated RawContacts using the given [contacts] API.
     *
     * Returns true if the update succeeded regardless of whether the RawContact photos update
     * succeeded of not.
     */
    suspend fun updateContact(contacts: Contacts): Boolean {
        val contact = contact

        if (contact == null || contact !is ExistingContactEntity) {
            // Only existing Contacts can be updated.
            return false
        }

        // Update RawContact photos that have changed first so that the (Raw)Contacts does not get
        // deleted if it only has a photo. Blank (Raw)Contacts are by default deleted in updates.
        for (index in 0 until rawContactsView.childCount) {
            val rawContactView = rawContactsView.getChildAt(index) as RawContactView
            rawContactView.savePhoto(contacts)
        }

        // Update the Contact photo iff it has changed.
        // Saving the contact photo will override the designated RawContact photo changes.
        // This behavior is "buggy" maybe we should not allow this? I'll leave it for now.
        photoView.savePhoto(contacts)

        // TODO Make sure that if a contact only has a photo, that it does not get deleted!

        // Perform the update. Ignore if photos update succeeded or not :D
        return contacts.updateWithPermission()
            // This is implicitly true by default. We are just being explicitly verbose here.
            .deleteBlanks(true)
            .contacts(contact)
            .commitWithContext()
            .isSuccessful
    }

    /**
     * Deletes the [contact] and all of the associated RawContacts using the given [contacts] API.
     *
     * Returns true if the delete succeeded.
     */
    suspend fun deleteContact(contacts: Contacts): Boolean {
        val contact = contact

        if (contact == null || contact !is ExistingContactEntity) {
            // Only existing Contacts can be deleted. Just return to to proceed with finishing.
            return true
        }

        return contacts.deleteWithPermission()
            .contacts(contact)
            .commitWithContext()
            .isSuccessful
    }

    private fun setOptionsView(contacts: Contacts) {
        val contact = contact

        optionsView.visibility = if (contact != null) VISIBLE else GONE

        // A contact must exist in order to update options. This means that options are not
        // available in create mode. This is the same behavior as the native Contacts app.
        if (contact == null) {
            return
        }

        launch { setStarredView(contact.options?.starred == true) }
        setSendToVoicemailView(contact.options?.sendToVoicemail == true)

        starredView.setOnClickListener {
            launch { toggleStarred(contacts) }
        }
        sendToVoicemailView.setOnCheckedChangeListener { _, isChecked ->
            launch { sendToVoicemail(isChecked, contacts) }
        }
        customRingtoneView.setOnClickListener {
            launch { selectCustomRingtone(contacts) }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDetailsView(contacts: Contacts) {
        photoView.setContact(contact, contacts)
        displayNamePrimaryView.text = "Display name primary: ${contact?.displayNamePrimary}"
        displayNameAltView.text = "Display name alt: ${contact?.displayNameAlt}"
        lastUpdatedView.text = "Last updated: ${contact?.lastUpdatedTimestamp}"
    }

    private suspend fun setStarredView(starred: Boolean) {
        val starBitmap = withContext(Dispatchers.IO) {
            BitmapFactory.decodeResource(
                resources, if (starred) {
                    android.R.drawable.star_big_on
                } else {
                    android.R.drawable.star_big_off
                }
            )
        }

        starredView.setImageBitmap(starBitmap)
    }

    private fun setSendToVoicemailView(sendToVoicemail: Boolean) {
        sendToVoicemailView.isChecked = sendToVoicemail
    }

    private fun setRawContactsView(contacts: Contacts) {
        rawContactsView.removeAllViews()

        val contact = contact
        if (contact != null) {
            newRawContactView = null

            contact.rawContacts.forEach { rawContact ->
                addRawContactView(rawContact, contacts)
            }
        } else {
            newRawContactView = addRawContactView(NewRawContact(), contacts)
        }
    }

    private fun addRawContactView(
        rawContact: RawContactEntity,
        contacts: Contacts
    ): RawContactView {
        val rawContactView = RawContactView(context)
        rawContactView.setRawContact(rawContact, contacts)
        rawContactsView.addView(rawContactView)
        return rawContactView
    }

    private suspend fun toggleStarred(contacts: Contacts) {
        val contact = contact

        if (contact == null || contact !is ExistingContactEntity) {
            // Only existing contacts can perform this operation.
            return
        }

        // The starred value from DB needs to be retrieved.
        val options = contact.optionsWithContext(contacts)?.mutableCopy() ?: NewOptions()
        val starred = options.starred == true

        options.starred = !starred

        // Update immediately, separate from the general update/save.
        val success = contact.setOptionsWithContext(contacts, options)

        if (success) {
            setStarredView(!starred)

            // As documented in the setOptions extensions, we need to refresh the set of group
            // memberships for all RawContacts belonging to the Contact because a group membership
            // to the favorites group may have been added or removed automatically by the
            // Contacts Provider. We can just refresh the entire contact but that will undo any
            // pending changes the user has not yet saved.
            refreshRawContactsGroupMemberships(contacts)
        }
    }

    private suspend fun sendToVoicemail(sendToVoicemail: Boolean, contacts: Contacts) {
        val contact = contact
        if (contact == null || contact !is ExistingContactEntity) {
            // Only existing contacts can perform this operation.
            return
        }

        // Update immediately, separate from the general update/save.
        contact.updateOptionsWithContext(contacts) {
            this.sendToVoicemail = sendToVoicemail
        }
    }

    private suspend fun selectCustomRingtone(contacts: Contacts) {
        val contact = contact
        if (contact == null || contact !is ExistingContactEntity) {
            // Only existing contacts can perform this operation.
            return
        }

        // The customRingtone value from DB needs to be retrieved.
        activity?.selectRingtone(contact.optionsWithContext(contacts)?.customRingtone)
    }

    private suspend fun setCustomRingtone(customRingtone: Uri?, contacts: Contacts) {
        val contact = contact
        if (contact == null || contact !is ExistingContactEntity) {
            // Only existing contacts can perform this operation.
            return
        }

        // Update immediately, separate from the general update/save.
        contact.updateOptionsWithContext(contacts) {
            this.customRingtone = customRingtone
        }
    }

    private suspend fun refreshRawContactsGroupMemberships(contacts: Contacts) {
        val contact = contact
        if (contact == null || contact !is MutableContact) {
            // Only existing mutable contacts can perform this operation.
            return
        }

        val contactId = contact.id

        // We could fetch the the set of groups for all linked RawContacts and then fetch group
        // memberships to the favorites group. Or, we can just fetch the entire contact again but
        // only to copy over the group memberships to the RawContacts =)
        val refreshedContact = contacts.queryWithPermission()
            .include(Fields.GroupMembership.all)
            .where { Contact.Id equalTo contactId }
            .findWithContext()
            .firstOrNull()
            ?: return

        // Copy over the refreshed group memberships to the RawContacts.
        for (rawContact in contact.rawContacts) {
            refreshedContact.rawContacts.find { it.id == rawContact.id }
                ?.let { refreshedRawContact ->
                    rawContact.groupMemberships =
                        refreshedRawContact.groupMemberships.asMutableList()
                }
        }

        // Invalidate group membership views.
        // Unsaved changes to the set of group memberships will be discarded. We could fix this but
        // it's too much code and this is just a sample app so we'll leave it!
        for (index in 0 until rawContactsView.childCount) {
            val rawContactView = rawContactsView.getChildAt(index) as RawContactView
            rawContactView.setAccountRequiredViews(contacts)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancel()
    }
}