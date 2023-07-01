package contacts.sample

import android.accounts.Account
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import contacts.async.accounts.commitWithContext
import contacts.async.aggregationexceptions.commitWithContext
import contacts.async.findWithContext
import contacts.core.Fields
import contacts.core.entities.ExistingContactEntity
import contacts.core.entities.ExistingRawContactEntity
import contacts.core.equalTo
import contacts.permissions.accounts.moveWithPermission
import contacts.permissions.aggregationexceptions.unlinkWithPermission
import contacts.permissions.queryWithPermission
import contacts.sample.util.createPinnedShortcut
import contacts.sample.util.getSerializableCompat
import contacts.sample.view.ContactView
import kotlinx.coroutines.launch

/**
 * Shows all Data of all RawContacts associated with the given Contact with [CONTACT_LOOKUP_KEY].
 * Also provides functions to create new contacts, edit existing contacts, and deleting existing
 * contacts.
 *
 * This is also able to read/write the Profile (device owner) contact when [IS_PROFILE] is true.
 *
 * #### Modes
 *
 * - [Mode.VIEW]: Displays all existing Contact/RawContact(s) Data. Modifications are NOT allowed.
 * - [Mode.EDIT]: Displays all existing Contact/RawContact(s) Data. Modifications are allowed.
 * - [Mode.CREATE]: Displays an empty Contact/RawContact form for creating a new Contact/RawContact.
 *
 * #### Options Menu
 *
 * - Edit:
 *     - In view mode, allows contact details to be edited.
 *     - In edit or create mode, this option is not visible.
 * - Save:
 *     - In edit mode, save changes to the existing contact.
 *     - In create mode, creates a new contact.
 *     - In view mode, this option is not visible.
 * - Delete:
 *     - In view and edit mode, deletes the existing contact.
 *     - In create mode, aborts creation of a new contact.
 * - Refresh:
 *     - In view mode, refreshes the existing contact data to make sure up-to-date data is shown.
 *     - In edit mode, refreshes the existing contact data to make sure up-to-date data is shown.
 *       Unsaved changes will be discarded.
 *     - In create mode, resets the new contact form to a blank state.
 * - Share:
 *     - In view mode, sends an intent to share the **existing** contact
 *     - In create and edit mode, sends an intent to share the new contact or the existing contact
 *       with unsaved changes.
 *       - This will be supported in https://github.com/vestrel00/contacts-android/issues/26
 * - Add to Home screen:
 *     - In view mode, creates a pinned shortcut to the contact's details activity.
 *     - In edit or create mode, this option is not visible.
 * - Unlink/separate:
 *     - In view mode, unlinks/separates the Contact's RawContacts.
 *       - This is visible only if the Contact has more than one RawContact.
 *     - In edit or create mode, this option is not visible.
 *
 * ## Note
 *
 * This is a very rudimentary activity that is not styled or made to look good. It may not follow
 * any good practices and may even implement bad practices. This is for demonstration purposes only!
 *
 * This does not fully support state retention (e.g. device rotation). The OSS community may
 * contribute to this by implementing it.
 */
class ContactDetailsActivity : BaseActivity() {

    private var contactLookupKey: String? = null

    private var mode: Mode = Mode.VIEW
        set(value) {
            field = value

            invalidateOptionsMenu()
            setViewMode()
        }

    // Not using any view binding libraries or plugins just for this.
    private lateinit var contactView: ContactView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_details)
        contactView = findViewById<ContactView?>(R.id.contactView).also {
            it.setOnMoveExistingRawContactToAccountListener(::onMoveExistingRawContactToAccount)
        }

        if (savedInstanceState != null) {
            contactLookupKey = savedInstanceState.getString(CONTACT_LOOKUP_KEY)
            mode = savedInstanceState.getSerializableCompat(MODE) ?: intent.mode
        } else {
            contactLookupKey = intent.contactLookupKey
            mode = intent.mode
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_contact_details, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            val editMenuItem = menu.findItem(R.id.edit)
            val saveMenuItem = menu.findItem(R.id.save)
            val shareMenuItem = menu.findItem(R.id.share)
            val createShortcutMenuItem = menu.findItem(R.id.create_shortcut)
            val unlinkMenuItem = menu.findItem(R.id.unlink)

            when (mode) {
                Mode.VIEW -> {
                    editMenuItem.isVisible = true
                    saveMenuItem.isVisible = false
                    shareMenuItem.isVisible = true
                    createShortcutMenuItem.isVisible = true
                    unlinkMenuItem.isVisible = contactView.contact?.rawContacts?.size?.let {
                        it > 1
                    } == true
                }
                Mode.EDIT, Mode.CREATE -> {
                    editMenuItem.isVisible = false
                    saveMenuItem.isVisible = true
                    shareMenuItem.isVisible = false
                    createShortcutMenuItem.isVisible = false
                    unlinkMenuItem.isVisible = false
                }
            }

            if (intent.isProfile) {
                // This sample app will not support a shortcut to the profile contact.
                // This is the same as AOSP Contacts app and Google Contacts app.
                createShortcutMenuItem.isVisible = false
                // Unlinking Profile is not supported, so we hide it.
                unlinkMenuItem.isVisible = false
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.save -> when (mode) {
                Mode.EDIT -> updateContact()
                Mode.CREATE -> createNewContact()
                else -> throw IllegalStateException("Save option should not be shown in view mode")
            }
            R.id.edit -> mode = Mode.EDIT
            R.id.delete -> when (mode) {
                Mode.VIEW, Mode.EDIT -> deleteContact()
                Mode.CREATE -> finish()
            }
            R.id.refresh -> refresh()
            R.id.share -> contactView.shareContact()
            R.id.create_shortcut -> {
                val contact = contactView.contact
                if (contact != null && contact is ExistingContactEntity) {
                    contact.createPinnedShortcut(this)
                }
            }
            R.id.unlink -> {
                val contact = contactView.contact
                if (contact != null && contact is ExistingContactEntity) {
                    launch {
                        val unlink = contacts
                            .aggregationExceptions()
                            .unlinkWithPermission()
                            .contact(contact)
                            .commitWithContext()

                        if (unlink.isSuccessful) {
                            showToast(R.string.contact_details_unlink_success)
                            finish()
                        } else {
                            showToast(R.string.contact_details_unlink_error)
                        }
                    }
                }
            }
        }

        return super.onOptionsItemSelected(menuItem)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        contactView.onActivityResult(requestCode, resultCode, data)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(CONTACT_LOOKUP_KEY, contactLookupKey)
        outState.putSerializable(MODE, mode)
    }

    private fun setViewMode() {
        when (mode) {
            Mode.VIEW -> launch {
                loadContact()
                contactView.isEnabled = false
            }
            Mode.EDIT -> launch {
                loadContact()
                contactView.isEnabled = true
            }
            Mode.CREATE -> {
                loadNewContact()
                contactView.isEnabled = true
            }
        }
    }

    private suspend fun loadContact() {
        val hidePhoneticNameIfEmptyAndDisabled =
            preferences.phoneticName == PhoneticName.HIDE_IF_EMPTY

        val loadSuccess = if (intent.isProfile) {
            contactView.loadProfile(contacts, hidePhoneticNameIfEmptyAndDisabled)
        } else {
            contactLookupKey?.let {
                contactView.loadContactWithLookupKey(
                    contacts, it, hidePhoneticNameIfEmptyAndDisabled
                )
            } == true
        }

        if (loadSuccess) {
            // Show/hide the Unlink menu item depending on RawContact count.
            invalidateOptionsMenu()
        } else {
            showToast(R.string.contact_details_fetch_error)
            finish()
        }
    }

    private fun loadNewContact() {
        contactView.loadNewContact(
            contacts,
            preferences.defaultAccountForNewContacts,
            preferences.phoneticName == PhoneticName.HIDE_IF_EMPTY
        )
    }

    private fun createNewContact() = launch {
        showProgressDialog()

        contactLookupKey = contactView.createNewContact(contacts, intent.isProfile)
        val createSuccess = contactLookupKey != null

        val resultMessageRes = if (createSuccess) {
            R.string.contact_details_create_success
        } else {
            R.string.contact_details_create_error
        }
        showToast(resultMessageRes)

        dismissProgressDialog()

        if (createSuccess) {
            mode = Mode.VIEW
        }
    }

    private fun updateContact() = launch {
        showProgressDialog()

        val updateSuccess = contactView.updateContact(contacts)

        val resultMessageRes = if (updateSuccess) {
            R.string.contact_details_update_success
        } else {
            R.string.contact_details_update_error
        }
        showToast(resultMessageRes)

        dismissProgressDialog()

        if (updateSuccess) {
            mode = Mode.VIEW
        }
    }

    private fun deleteContact() = launch {
        showProgressDialog()

        val deleteSuccess = contactView.deleteContact(contacts)

        val resultMessageRes = if (deleteSuccess) {
            R.string.contact_details_delete_success
        } else {
            R.string.contact_details_delete_error
        }
        showToast(resultMessageRes)

        if (deleteSuccess) {
            finish()
        } else {
            dismissProgressDialog()
        }
    }

    private fun onMoveExistingRawContactToAccount(
        account: Account?,
        existingRawContact: ExistingRawContactEntity
    ) = launch {
        val moveResult = contacts
            .accounts()
            .moveWithPermission()
            .rawContactsTo(account, existingRawContact)
            .commitWithContext()

        val movedRawContactId = moveResult.rawContactIds.firstOrNull()
        if (movedRawContactId != null) {
            showToast(R.string.contact_details_move_success)

            contactLookupKey = contacts
                .queryWithPermission()
                .include(Fields.Contact.LookupKey)
                .where { RawContact.Id equalTo movedRawContactId }
                .findWithContext()
                .firstOrNull()
                ?.lookupKey
        } else {
            showToast(R.string.contact_details_move_error)
        }

        refresh()
    }

    private fun refresh() {
        mode = mode
    }

    companion object {

        // region VIEW
        fun viewContactDetailsIntent(context: Context, contactLookupKey: String): Intent =
            Intent(context, ContactDetailsActivity::class.java).apply {
                putExtra(REQUEST_CODE, REQUEST_VIEW)
                putExtra(CONTACT_LOOKUP_KEY, contactLookupKey)
            }

        fun viewContactDetails(activity: Activity, contactLookupKey: String) {
            val intent = viewContactDetailsIntent(activity, contactLookupKey)

            activity.startActivityForResult(intent, REQUEST_VIEW)
        }

        fun viewProfileDetails(activity: Activity) {
            val intent = Intent(activity, ContactDetailsActivity::class.java).apply {
                putExtra(REQUEST_CODE, REQUEST_VIEW)
                putExtra(IS_PROFILE, true)
            }

            activity.startActivityForResult(intent, REQUEST_VIEW)
        }

        fun onViewContactDetailsResult(requestCode: Int, contactDetailsViewed: () -> Unit) {
            if (requestCode == REQUEST_VIEW) {
                contactDetailsViewed()
            }
        }
        // endregion

        // region EDIT
        fun editContactDetails(activity: Activity, contactLookupKey: String) {
            val intent = Intent(activity, ContactDetailsActivity::class.java).apply {
                putExtra(REQUEST_CODE, REQUEST_EDIT)
                putExtra(CONTACT_LOOKUP_KEY, contactLookupKey)
            }

            activity.startActivityForResult(intent, REQUEST_EDIT)
        }

        fun onEditContactDetailsResult(requestCode: Int, contactDetailsEdited: () -> Unit) {
            if (requestCode == REQUEST_EDIT) {
                contactDetailsEdited()
            }
        }
        // endregion

        // region CREATE
        fun createContact(activity: Activity) {
            val intent = Intent(activity, ContactDetailsActivity::class.java).apply {
                putExtra(REQUEST_CODE, REQUEST_CREATE)
            }

            activity.startActivityForResult(intent, REQUEST_CREATE)
        }

        fun createProfile(activity: Activity) {
            val intent = Intent(activity, ContactDetailsActivity::class.java).apply {
                putExtra(REQUEST_CODE, REQUEST_CREATE)
                putExtra(IS_PROFILE, true)
            }

            activity.startActivityForResult(intent, REQUEST_CREATE)
        }

        fun onCreateContactResult(requestCode: Int, contactCreated: () -> Unit) {
            if (requestCode == REQUEST_CREATE) {
                contactCreated()
            }
        }
        // endregion
    }
}

private enum class Mode {
    VIEW, EDIT, CREATE
}

/**
 * The type of request. Defaults to -1 if not provided.
 *
 * #### Dev note
 *
 * I'm aware that [Intent.getAction] paired with [Intent.getData] are more appropriate tools
 * to use than the request code. Using these tools with the correct scheme will enable deep
 * linking into this sample app. For example, launching an intent from any app with matching
 * action and scheme will result in the suggestion of the AOSP Contacts app or this sample
 * app to handle the intent. See [android.provider.ContactsContract.Intents].
 *
 * TODO? Implement deep linking?
 * Anyways, this is not something I'll be implementing in this sample app, at least
 * initially, as it requires a bit of work to get right. This is something actual apps may
 * implement but this sample app may implement it too so it can be used as reference. We may
 * even create a library containing deep linking functions?
 */
private val Intent.mode: Mode
    get() = when (val requestCode = getIntExtra(REQUEST_CODE, -1)) {
        REQUEST_VIEW -> Mode.VIEW
        REQUEST_EDIT -> Mode.EDIT
        REQUEST_CREATE -> Mode.CREATE
        else -> {
            throw IllegalArgumentException("There is no mode for request code $requestCode")
        }
    }

private val Intent.isProfile: Boolean
    get() = getBooleanExtra(IS_PROFILE, false)

/**
 * The lookup key of an existing Contact. Defaults to -null if not provided.
 */
private val Intent.contactLookupKey: String?
    get() = getStringExtra(CONTACT_LOOKUP_KEY)

private const val REQUEST_VIEW = 111
private const val REQUEST_EDIT = 222
private const val REQUEST_CREATE = 333

private const val REQUEST_CODE = "requestCode"
private const val CONTACT_LOOKUP_KEY = "contactLookupKey"
private const val MODE = "mode"

private const val IS_PROFILE = "isProfile"