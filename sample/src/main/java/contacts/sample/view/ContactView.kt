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
import contacts.Contacts
import contacts.Fields
import contacts.async.commitWithContext
import contacts.async.findWithContext
import contacts.async.util.contactWithContext
import contacts.async.util.optionsWithContext
import contacts.async.util.setOptionsWithContext
import contacts.async.util.updateOptionsWithContext
import contacts.entities.MutableContact
import contacts.entities.MutableOptions
import contacts.entities.MutableRawContact
import contacts.equalTo
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
 * A (vertical) [LinearLayout] that displays a [MutableContact] and handles the modifications to the
 * given [contact]. Each of the RawContact is displayed in a [RawContactView].
 *
 * Loading the [contact] will automatically update the views. Any modifications in the views will
 * also be made to the [contact].
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
     * modifications in the views will also be made to the this.
     */
    private var contact: MutableContact? = null
        set(value) {
            field = value

            launch { setStarredView(contact?.options?.starred == true) }
            setSendToVoicemailView(contact?.options?.sendToVoicemail == true)

            setDetailsView()
            setRawContactsView()
        }

    /**
     * A RawContactView with a new (empty) RawContact. Used for creating a new Contact.
     */
    private var newRawContactView: RawContactView? = null

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.Main

    // options
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

        photoView = findViewById(R.id.photo)
        displayNamePrimaryView = findViewById(R.id.displayNamePrimary)
        displayNameAltView = findViewById(R.id.displayNameAlt)
        lastUpdatedView = findViewById(R.id.lastUpdated)

        starredView = findViewById(R.id.starred)
        starredView.setOnClickListener {
            launch { toggleStarred() }
        }

        sendToVoicemailView = findViewById(R.id.sendToVoicemail)
        sendToVoicemailView.setOnCheckedChangeListener { _, isChecked ->
            launch { sendToVoicemail(isChecked) }
        }

        customRingtoneView = findViewById(R.id.customRingtone)
        customRingtoneView.setOnClickListener {
            launch { selectCustomRingtone() }
        }

        rawContactsView = findViewById(R.id.rawContacts)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        onRingtoneSelected(requestCode, resultCode, data) { ringtoneUri ->
            launch { setCustomRingtone(ringtoneUri) }
        }

        photoView.onActivityResult(requestCode, resultCode, data)

        for (index in 0 until rawContactsView.childCount) {
            val rawContactView = rawContactsView.getChildAt(index) as RawContactView
            rawContactView.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        // super.setEnabled(enabled) intentionally not calling this
        // Do not ever disable the contact options and details.
        rawContactsView.setThisAndDescendantsEnabled(enabled)
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
     * Inserts the new (raw) contact to the database.
     *
     * Returns the newly created contact's ID. Returns null if the insert failed.
     */
    @JvmOverloads
    suspend fun createNewContact(contacts: Contacts = Contacts(context)): Long? {
        val rawContact = newRawContactView?.rawContact ?: return null

        // TODO Contact photo!

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

        // Update RawContact photos that have changed first so that the (Raw)Contacts does not get
        // deleted if it only has a photo. Blank (Raw)Contacts are by default deleted in updates.
        for (index in 0 until rawContactsView.childCount) {
            val rawContactView = rawContactsView.getChildAt(index) as RawContactView
            rawContactView.savePhoto()
        }

        // Update the Contact photo iff it has changed.
        photoView.savePhoto()

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

    @SuppressLint("SetTextI18n")
    private fun setDetailsView() {
        photoView.contact = contact
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

    private fun setRawContactsView() {
        rawContactsView.removeAllViews()

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
        rawContactsView.addView(rawContactView)
        return rawContactView
    }

    private suspend fun toggleStarred() {
        // The starred value from DB needs to be retrieved.
        val options = contact?.optionsWithContext(context)?.toMutableOptions() ?: MutableOptions()
        val starred = options.starred == true

        options.starred = !starred

        // Update immediately, separate from the general update/save.
        val success = contact?.setOptionsWithContext(context, options) == true

        if (success) {
            setStarredView(!starred)
        }
    }

    private suspend fun sendToVoicemail(sendToVoicemail: Boolean) {
        // Update immediately, separate from the general update/save.
        contact?.updateOptionsWithContext(context) {
            this.sendToVoicemail = sendToVoicemail
        }
    }

    private suspend fun selectCustomRingtone() {
        // The customRingtone value from DB needs to be retrieved.
        activity?.selectRingtone(contact?.optionsWithContext(context)?.customRingtone)
    }

    private suspend fun setCustomRingtone(customRingtone: Uri?) {
        // Update immediately, separate from the general update/save.
        contact?.updateOptionsWithContext(context) {
            this.customRingtone = customRingtone
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancel()
    }
}