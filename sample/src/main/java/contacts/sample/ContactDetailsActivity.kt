package contacts.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import contacts.sample.view.ContactView
import kotlinx.coroutines.launch

/**
 * Shows all Data of all RawContacts associated with the given Contact with [CONTACT_ID]. Also
 * provides functions to create new contacts, edit existing contacts, and deleting existing
 * contacts.
 *
 * #### Modes
 *
 * - [Mode.VIEW]: Displays all existing Contact/RawContact(s) Data. Modifications are NOT allowed.
 * - [Mode.EDIT]: Displays all existing Contact/RawContact(s) Data. Modifications are allowed.
 * - [Mode.CREATE]: Displays an empty Contact/RawContact form for creating a new Contact/RawContact.
 *
 * #### Options Menu
 *
 * - Edit: Allow contact details to be edited.
 * - Save:
 *     - In edit mode, save changes to the existing contact.
 *     - In create mode, creates a new contact.
 * - Delete:
 *     - In view and edit mode, deletes the existing contact.
 *     - In create mode, aborts creation of a new contact.
 * - Refresh:
 *     - In view mode, refreshes the existing contact data to make sure up-to-date data is shown.
 *     - In edit mode, refreshes the existing contact data to make sure up-to-date data is shown.
 *       Unsaved changes will be discarded.
 *     - In create mode, resets the new contact form to a blank state.
 *
 * ## Note
 *
 * This is a very rudimentary activity that is not styled or made to look good. It may not follow
 * any good practices and may even implement bad practices. This is for demonstration purposes only!
 *
 * This does not support state retention (e.g. device rotation). The OSS community may contribute to
 * this by implementing it.
 */
class ContactDetailsActivity : BaseActivity() {

    private var contactId: Long? = null

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
        contactView = findViewById(R.id.contactView)

        if (savedInstanceState != null) {
            contactId = savedInstanceState.getLong(CONTACT_ID)
            mode = savedInstanceState.getSerializable(MODE) as Mode
        } else {
            contactId = intent.contactId
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

            when (mode) {
                Mode.VIEW -> {
                    editMenuItem.isVisible = true
                    saveMenuItem.isVisible = false
                }
                Mode.EDIT, Mode.CREATE -> {
                    editMenuItem.isVisible = false
                    saveMenuItem.isVisible = true
                }
            }
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.save -> when (mode) {
                Mode.VIEW, Mode.EDIT -> updateContact()
                Mode.CREATE -> createNewContact()
            }
            R.id.edit -> mode = Mode.EDIT
            R.id.delete -> when (mode) {
                Mode.VIEW, Mode.EDIT -> deleteContact()
                Mode.CREATE -> finish()
            }
            R.id.refresh -> mode = mode
        }

        return super.onOptionsItemSelected(menuItem)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        contactView.onActivityResult(requestCode, resultCode, data, contacts)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putSerializable(CONTACT_ID, contactId)
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
                contactView.loadNewContact(contacts)
                contactView.isEnabled = true
            }
        }
    }

    private suspend fun loadContact() {
        val loadSuccess = contactId?.let {
            contactView.loadContactWithId(it, contacts)
        } == true

        if (!loadSuccess) {
            Toast
                .makeText(
                    this@ContactDetailsActivity, R.string.contact_details_fetch_error, LENGTH_SHORT
                )
                .show()

            finish()
        }
    }

    private fun createNewContact() = launch {
        showProgressDialog()

        contactId = contactView.createNewContact(contacts)
        val createSuccess = contactId != null

        val resultMessageRes = if (createSuccess) {
            R.string.contact_details_create_success
        } else {
            R.string.contact_details_create_error
        }
        Toast.makeText(this@ContactDetailsActivity, resultMessageRes, LENGTH_SHORT).show()

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
        Toast.makeText(this@ContactDetailsActivity, resultMessageRes, LENGTH_SHORT).show()

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
        Toast.makeText(this@ContactDetailsActivity, resultMessageRes, LENGTH_SHORT).show()

        if (deleteSuccess) {
            finish()
        } else {
            dismissProgressDialog()
        }
    }

    companion object {

        // region VIEW
        fun viewContactDetails(activity: Activity, contactId: Long) {
            val intent = Intent(activity, ContactDetailsActivity::class.java).apply {
                putExtra(REQUEST_CODE, REQUEST_VIEW)
                putExtra(CONTACT_ID, contactId)
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
        fun editContactDetails(activity: Activity, contactId: Long) {
            val intent = Intent(activity, ContactDetailsActivity::class.java).apply {
                putExtra(REQUEST_CODE, REQUEST_EDIT)
                putExtra(CONTACT_ID, contactId)
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

            intent.type

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
 * action and scheme will result in the suggestion of the native Contacts app or this sample
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

/**
 * The id of an existing Contact. Defaults to -1 if not provided.
 */
private val Intent.contactId: Long
    get() = getLongExtra(CONTACT_ID, -1L)

private const val REQUEST_VIEW = 111
private const val REQUEST_EDIT = 222
private const val REQUEST_CREATE = 333

private const val REQUEST_CODE = "requestCode"
private const val CONTACT_ID = "contactId"
private const val MODE = "mode"