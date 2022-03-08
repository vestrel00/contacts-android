package contacts.sample

import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ListView
import contacts.async.findWithContext
import contacts.core.ContactsFields
import contacts.core.Fields
import contacts.core.asc
import contacts.core.entities.Contact
import contacts.core.util.emails
import contacts.core.util.phones
import contacts.permissions.broadQueryWithPermission
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
 * - Accounts: Opens the accounts activity to select which accounts to include in the search.
 * - Refresh: Performs the search query again to refresh the contacts list.
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

    // The null Account is the "Local Account".
    private var selectedAccounts = emptyList<Account?>()
    private var queryJob: Job? = null

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
            R.id.accounts -> {
                AccountsActivity.selectAccounts(this, true, ArrayList(selectedAccounts))
            }
            R.id.blocked_numbers -> {
                BlockedNumbersActivity.showBlockedNumbers(this)
            }
            R.id.refresh -> {
                showContacts()
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
        contactsListView = findViewById(R.id.contactsListView)
        contactsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        contactsListView.adapter = contactsAdapter
        contactsListView.onItemClickListener = OnContactClickListener()
    }

    private fun showContacts() {
        queryJob?.cancel()
        queryJob = launch {
            // Using BroadQuery here so that it matches closely to the native Contacts app search
            // results. Consumers should try out Query too because it gives the most control.
            searchResults = contacts.broadQueryWithPermission()
                .accounts(selectedAccounts)
                .include(
                    Fields.Contact.LookupKey,
                    Fields.Contact.DisplayNamePrimary,
                    Fields.Email.Address,
                    Fields.Phone.Number
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
        val listOfContactNameAndEmails = searchResults.map { contact ->
            val displayNamePrimary = contact.displayNamePrimary

            val emails = contact
                .emails()
                // Order by super primary first and then primary. This is the same behavior as the
                // native Contacts app.
                .sortedByDescending { it.isSuperPrimary }
                .sortedByDescending { it.isPrimary }
                .map { it.address }
                .joinToString(", ")

            val phoneNumbers = contact
                .phones()
                // Order by super primary first and then primary. This is the same behavior as the
                // native Contacts app.
                .sortedByDescending { it.isSuperPrimary }
                .sortedByDescending { it.isPrimary }
                .map { it.number }
                .joinToString(", ")

            """
                |$displayNamePrimary
                |$emails
                |$phoneNumbers
            """.trimMargin()
        }

        contactsAdapter.apply {
            setNotifyOnChange(false)
            clear()
            addAll(listOfContactNameAndEmails)
            notifyDataSetChanged()
        }
    }

    private inner class OnContactClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            searchResults[position].lookupKey?.let {
                ContactDetailsActivity.viewContactDetails(this@ContactsActivity, it)
            }
        }
    }
}