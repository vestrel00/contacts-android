package contacts.sample

import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.ActionMode
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import contacts.async.commitInOneTransactionWithContext
import contacts.async.findWithContext
import contacts.async.util.linkWithContext
import contacts.core.ContactsFields
import contacts.core.Fields
import contacts.core.asc
import contacts.core.entities.Contact
import contacts.core.entities.Group
import contacts.permissions.broadQueryWithPermission
import contacts.permissions.deleteWithPermission
import contacts.sample.util.AbstractMultiChoiceModeListener
import contacts.sample.util.trueKeys
import contacts.ui.text.AbstractTextWatcher
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch


/**
 * Displays a search field that allows users to broadly search for contacts using the Contacts
 * Provider general matching algorithm. All matching contacts are displayed in a list view. Tapping
 * a contact in the list view will open the details activity, where more details are shown.
 *
 * #### Options Menu
 *
 * - Create: Opens an activity to create a new contact.
 * - Refresh: Performs the search query again to refresh the contacts list.
 * - Accounts: Opens an activity to select which accounts to include in the search.
 * - Groups: Opens an activity to select which groups of selected accounts to include in the search.
 * - Blocked numbers: Opens an activity that allows users to add/remove blocked numbers.
 *
 * #### Contextual options menu
 *
 * When multiple contacts are selected...
 *
 * - Delete: Deletes all selected contacts.
 *
 * ## Note
 *
 * This is a very rudimentary activity that is not styled or made to look good. It may not follow
 * any good practices and may even implement bad practices. This is for demonstration purposes only!
 *
 * This does not support state retention (e.g. device rotation). The OSS community may contribute to
 * this by implementing it.
 */
class ContactsActivity : BaseActivity() {

    private var queryJob: Job? = null

    private var selectedAccounts = emptyList<Account?>()
    private var selectedGroups = emptyList<Group>()

    private val searchText: String
        get() = searchField.text.toString()

    private var searchResults = emptyList<Contact>()

    private lateinit var contactsAdapter: ArrayAdapter<String>

    // Not using any view binding libraries or plugins just for this.
    private lateinit var searchField: EditText
    private lateinit var contactsListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contacts)
        setupSearchField()
        setupContactsListView()
        showContacts()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_contacts, menu)
        return true
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.create -> {
                ContactDetailsActivity.createContact(this)
            }
            R.id.refresh -> {
                showContacts()
            }
            R.id.accounts -> {
                AccountsActivity.selectAccounts(this, true, ArrayList(selectedAccounts))
            }
            R.id.groups -> {
                AccountsActivity.selectGroups(this, ArrayList(selectedGroups))
            }
            R.id.blocked_numbers -> {
                BlockedNumbersActivity.showBlockedNumbers(this)
            }
        }

        return super.onOptionsItemSelected(menuItem)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        ContactDetailsActivity.onViewContactDetailsResult(requestCode) {
            showContacts()
        }

        ContactDetailsActivity.onCreateContactResult(requestCode) {
            showContacts()
        }

        AccountsActivity.onSelectAccountsResult(requestCode, resultCode, data) { selectedAccounts ->
            this.selectedAccounts = selectedAccounts
            showContacts()
        }

        AccountsActivity.onSelectGroupsResult(requestCode, resultCode, data) { selectedGroups ->
            this.selectedGroups = selectedGroups
            showContacts()
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setupSearchField() {
        searchField = findViewById(R.id.searchField)
        searchField.addTextChangedListener(object : AbstractTextWatcher {
            override fun afterTextChanged(s: Editable?) {
                showContacts()
            }
        })
    }

    private fun setupContactsListView() {
        // [ANDROID X] Not using RecyclerView to avoid dependency on androidx.recyclerview.
        // Ahh, my good ol' friend ListView. You serve me once again =)
        contactsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice)

        contactsListView = findViewById(R.id.contactsListView)
        contactsListView.adapter = contactsAdapter
        contactsListView.onItemClickListener = OnContactClickListener()
        // CHOICE_MODE_MULTIPLE_(MODAL) means that initial selection will only occur on long press.
        contactsListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE_MODAL
        contactsListView.setMultiChoiceModeListener(OnMultipleContactsChosenListener())
    }

    private fun showContacts() {
        queryJob?.cancel()
        queryJob = launch {
            // Using BroadQuery here so that it matches closely to the native Contacts app search
            // results. Consumers should try out Query too because it gives the most control.
            searchResults = contacts.broadQueryWithPermission()
                .accounts(selectedAccounts)
                .groups(selectedGroups)
                .include(
                    Fields.Contact.LookupKey,
                    Fields.Contact.DisplayNamePrimary,
                )
                .wherePartiallyMatches(searchText)
                .orderBy(ContactsFields.DisplayNamePrimary.asc())
                // Not showing how to low 4xad x number of contacts at a time and then loading more when
                // scrolled to the very bottom of the list for brevity. Consumers can figure it out.
                // .offset(...)
                // .limit(...)
                .findWithContext()

            setContactsAdapterItems()
        }
    }

    private fun setContactsAdapterItems() {
        contactsAdapter.apply {
            setNotifyOnChange(false)
            clear()
            addAll(searchResults.map { it.displayNamePrimary })
            notifyDataSetChanged()
        }
    }

    private fun deleteSelectedContacts(mode: ActionMode) = launch {
        val contactIdsToDelete = contactsListView
            .checkedItemPositions
            .trueKeys
            .map { searchResults[it].id }

        val delete = contacts.deleteWithPermission()
            .contactsWithId(contactIdsToDelete)
            .commitInOneTransactionWithContext()

        if (delete.isSuccessful) {
            showToast(R.string.contacts_delete_success)

            contactsListView.clearChoices()
            mode.finish()
            showContacts()
        } else {
            showToast(R.string.contacts_delete_error)
        }
    }

    private fun linkSelectedContacts(mode: ActionMode) = launch {
        val contactsToLink = contactsListView
            .checkedItemPositions
            .trueKeys
            .map { searchResults[it] }

        val link = contactsToLink.linkWithContext(contacts)

        if (link.isSuccessful) {
            showToast(R.string.contacts_link_success)

            contactsListView.clearChoices()
            mode.finish()
            showContacts()
        } else {
            showToast(R.string.contacts_link_error)
        }
    }

    private inner class OnContactClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            searchResults[position].lookupKey?.let {
                ContactDetailsActivity.viewContactDetails(this@ContactsActivity, it)
            }
        }
    }

    private inner class OnMultipleContactsChosenListener : AbstractMultiChoiceModeListener {
        override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
            menuInflater.inflate(R.menu.menu_contacts_multiple_choice, menu)
            return true
        }

        override fun onItemCheckedStateChanged(
            mode: ActionMode,
            position: Int,
            id: Long,
            checked: Boolean
        ) {
            val linkMenuItem = mode.menu.findItem(R.id.link)
            linkMenuItem.isVisible = contactsListView.checkedItemCount > 1
        }

        override fun onActionItemClicked(mode: ActionMode, item: MenuItem): Boolean =
            when (item.itemId) {
                R.id.delete -> {
                    deleteSelectedContacts(mode)
                    true
                }
                R.id.link -> {
                    linkSelectedContacts(mode)
                    true
                }
                else -> false
            }
    }
}