package contacts.sample.view

import android.accounts.Account
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import contacts.async.commitWithContext
import contacts.async.findWithContext
import contacts.async.profile.commitWithContext
import contacts.async.profile.findWithContext
import contacts.async.util.contactWithContext
import contacts.core.Contacts
import contacts.core.entities.*
import contacts.core.util.lookupKeyIn
import contacts.core.util.shareVCardIntent
import contacts.permissions.deleteWithPermission
import contacts.permissions.insertWithPermission
import contacts.permissions.profile.deleteWithPermission
import contacts.permissions.profile.insertWithPermission
import contacts.permissions.profile.queryWithPermission
import contacts.permissions.profile.updateWithPermission
import contacts.permissions.queryWithPermission
import contacts.permissions.updateWithPermission
import contacts.sample.R
import contacts.ui.view.OptionsView
import contacts.ui.view.activity
import contacts.ui.view.setThisAndDescendantsEnabled
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
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
    var contact: ContactEntity? = null
        private set

    /**
     * Set the Contact shown and managed by this view to the given [contact] and uses the given
     * [contacts] API to perform operations on it. The [defaultAccount] is used as the account if
     * the [contact] is not yet associated with one.
     */
    private fun setContact(
        contacts: Contacts,
        contact: ContactEntity?,
        defaultAccount: Account?,
        hidePhoneticNameIfEmptyAndDisabled: Boolean
    ) {
        this.contact = contact

        setRawContactsView(contacts, defaultAccount, hidePhoneticNameIfEmptyAndDisabled)
        // Call setDetailsView after newRawContactView has been set in setRawContactsView
        setDetailsView(contacts)
    }

    /**
     * A RawContactView with a new (empty) RawContact. Used for creating a new Contact.
     */
    private var newRawContactView: RawContactView? = null

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.Main

    // options
    private val optionsView: OptionsView

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

        photoView = findViewById(R.id.photo)

        displayNamePrimaryView = findViewById(R.id.displayNamePrimary)
        displayNameAltView = findViewById(R.id.displayNameAlt)
        lastUpdatedView = findViewById(R.id.lastUpdated)

        rawContactsView = findViewById(R.id.rawContacts)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        optionsView.onActivityResult(requestCode, resultCode, data)

        photoView.onActivityResult(requestCode, resultCode, data)

        for (index in 0 until rawContactsView.childCount) {
            val rawContactView = rawContactsView.getChildAt(index) as RawContactView
            rawContactView.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun setEnabled(enabled: Boolean) {
        // super.setEnabled(enabled) intentionally not calling this
        optionsView.setThisAndDescendantsEnabled(enabled)
        photoView.isEnabled = enabled
        rawContactsView.setThisAndDescendantsEnabled(enabled)
    }

    /**
     * Loads the contact with the given [lookupKey] using the given [contacts] API.
     *
     * Returns true if the load succeeded.
     */
    suspend fun loadContactWithLookupKey(
        contacts: Contacts,
        lookupKey: String,
        hidePhoneticNameIfEmptyAndDisabled: Boolean
    ): Boolean {
        val contact = contacts
            .queryWithPermission()
            .where { Contact.lookupKeyIn(lookupKey) }
            .findWithContext()
            .firstOrNull()
            ?.mutableCopy()

        setContact(contacts, contact, null, hidePhoneticNameIfEmptyAndDisabled)

        return contact != null
    }

    /**
     * Loads the Profile (device owner) contact.
     */
    suspend fun loadProfile(
        contacts: Contacts,
        hidePhoneticNameIfEmptyAndDisabled: Boolean
    ): Boolean {
        val contact = contacts
            .profile()
            .queryWithPermission()
            .findWithContext()
            .contact
            ?.mutableCopy()

        setContact(contacts, contact, null, hidePhoneticNameIfEmptyAndDisabled)

        return contact != null
    }

    /**
     * Removes any loaded contact and loads an empty new (raw) contact. The [defaultAccount] is
     * used as the initially selected account.
     *
     * To insert the new (raw) contact into the Contacts database, call [createNewContact].
     */
    fun loadNewContact(
        contacts: Contacts,
        defaultAccount: Account?,
        hidePhoneticNameIfEmptyAndDisabled: Boolean
    ) {
        setContact(contacts, null, defaultAccount, hidePhoneticNameIfEmptyAndDisabled)
    }

    /**
     * Inserts the new (raw) contact to the database using the given [contacts] API.
     *
     * Returns the newly created contact's lookup key. Returns null if the insert failed.
     */
    suspend fun createNewContact(contacts: Contacts, isProfile: Boolean): String? {
        val rawContact = newRawContactView?.rawContact
        if (rawContact == null || rawContact !is NewRawContact) {
            // Only new RawContacts can be inserted.
            return null
        }

        val newContact = if (isProfile) {
            contacts
                .profile()
                .insertWithPermission()
                .forAccount(newRawContactView?.account)
                .rawContact(rawContact)
                .commitWithContext()
                .contactWithContext(contacts)
        } else {
            contacts
                .insertWithPermission()
                .forAccount(newRawContactView?.account)
                .rawContacts(rawContact)
                .commitWithContext()
                .contactWithContext(contacts, rawContact)
        }

        photoView.setContact(newContact, contacts)

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

        // Perform the update. Ignore if photos update succeeded or not :D
        return if (contact.isProfile) {
            contacts
                .profile()
                .updateWithPermission()
                .contact(contact)
                .commitWithContext()
                .isSuccessful
        } else {
            contacts
                .updateWithPermission()
                .contacts(contact)
                .commitWithContext()
                .isSuccessful
        }
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

        return if (contact.isProfile) {
            contacts
                .profile()
                .deleteWithPermission()
                .contact()
                .commitWithContext()
                .isSuccessful
        } else {
            contacts
                .deleteWithPermission()
                .contacts(contact)
                .commitWithContext()
                .isSuccessful
        }
    }

    fun shareContact() {
        val contact = contact
        if (contact is ExistingContactEntity) {
            val shareIntent = contact.shareVCardIntent()
            if (shareIntent != null) {
                activity?.startActivity(Intent.createChooser(shareIntent, null))
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setDetailsView(contacts: Contacts) {
        val contact = contact

        val rawContact = newRawContactView?.rawContact
        val newOptions = NewOptions()
        if (rawContact != null && rawContact is NewRawContact) {
            // We are inserting a new raw contact.
            optionsView.data = newOptions
            rawContact.options = newOptions
        } else {
            // We are editing an existing contact.
            optionsView.data = contact?.options ?: newOptions
        }
        optionsView.visibility = if (contact?.isProfile == true || rawContact?.isProfile == true) {
            GONE
        } else {
            VISIBLE
        }

        photoView.setContact(contact, contacts)
        displayNamePrimaryView.text = "Display name primary: ${contact?.displayNamePrimary}"
        displayNameAltView.text = "Display name alt: ${contact?.displayNameAlt}"
        lastUpdatedView.text = "Last updated: ${contact?.lastUpdatedTimestamp}"
    }

    private fun setRawContactsView(
        contacts: Contacts,
        defaultAccount: Account?,
        hidePhoneticNameIfEmptyAndDisabled: Boolean
    ) {
        rawContactsView.removeAllViews()

        val contact = contact
        if (contact != null) { // Edit existing contact
            newRawContactView = null

            contact.rawContacts.forEach { rawContact ->
                val rawContactView = addRawContactView(
                    contacts,
                    rawContact,
                    null,
                    hidePhoneticNameIfEmptyAndDisabled
                )

                if (contact is ExistingContactEntity
                    && rawContact is ExistingRawContactEntity
                ) {
                    // Make sure that this Contact view and the primary photo holder view is set to
                    // the same photo whenever the user picks one.
                    val photoHolderId = contact.primaryPhotoHolder?.id
                    if (photoHolderId != null && photoHolderId == rawContact.id) {
                        rawContactView.setPhotoDrawableOnPhotoPickedWith(photoView)
                    }
                }
            }
        } else { // Create new raw contact
            newRawContactView = addRawContactView(
                contacts,
                NewRawContact(),
                defaultAccount,
                hidePhoneticNameIfEmptyAndDisabled
            ).also {
                // Make sure that this Contact view and the primary photo holder view is set to the
                // same photo whenever the user picks one.
                it.setPhotoDrawableOnPhotoPickedWith(photoView)
            }
        }
    }

    private fun addRawContactView(
        contacts: Contacts,
        rawContact: RawContactEntity,
        defaultAccount: Account?,
        hidePhoneticNameIfEmptyAndDisabled: Boolean
    ): RawContactView {
        val rawContactView = RawContactView(context)
        rawContactView.setRawContact(
            contacts,
            rawContact,
            defaultAccount,
            hidePhoneticNameIfEmptyAndDisabled
        )
        rawContactsView.addView(rawContactView)
        return rawContactView
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancel()
    }
}