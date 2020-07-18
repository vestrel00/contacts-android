package com.vestrel00.contacts.sample

import android.accounts.Account
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.vestrel00.contacts.*
import com.vestrel00.contacts.async.findWithContext
import com.vestrel00.contacts.debug.logContactsProviderTables
import com.vestrel00.contacts.entities.Contact
import com.vestrel00.contacts.permissions.queryWithPermission
import com.vestrel00.contacts.ui.text.AbstractTextWatcher
import com.vestrel00.contacts.util.emails
import com.vestrel00.contacts.util.phones
import kotlinx.android.synthetic.main.activity_contacts.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ContactsActivity : BaseActivity() {

    // The null Account is the "Local Account".
    private var selectedAccounts = emptyList<Account?>()
    private var queryJob: Job? = null

    private val searchText: String
        get() = searchField.text.toString()

    private var searchResults = emptyList<Contact>()

    private lateinit var contactsAdapter: ArrayAdapter<String>

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
            R.id.accounts -> {
                AccountsActivity.selectAccounts(this, true, ArrayList(selectedAccounts))
                return true
            }
            R.id.refresh -> {
                showContacts()
                return true
            }
        }

        return super.onOptionsItemSelected(menuItem)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        EditContactDetailsActivity.onEditContactDetailsResult(requestCode) {
            showContacts()
        }

        AccountsActivity.onSelectAccountsResult(requestCode, resultCode, data) { selectedAccounts ->
            this.selectedAccounts = selectedAccounts
            showContacts()
        }

        super.onActivityResult(requestCode, resultCode, data)
    }

    private fun setupSearchField() {
        searchField.addTextChangedListener(object : AbstractTextWatcher {
            override fun afterTextChanged(s: Editable?) {
                showContacts()
            }
        })
    }

    private fun setupContactsListView() {
        // [ANDROID X] Not using RecyclerView to avoid dependency on androidx.recyclerview.
        // Ahh, my good ol' friend ListView. You serve me once again =)
        contactsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_1)
        contactsListView.adapter = contactsAdapter
        contactsListView.onItemClickListener = OnContactClickListener()
    }

    private fun showContacts() {
        queryJob?.cancel()
        queryJob = launch {
            val searchText = searchText
            val where = if (searchText.isEmpty()) {
                null
            } else {
                SEARCH_FIELDS whereOr { it contains searchText }
            }

            val context = this@ContactsActivity

            searchResults = Contacts().queryWithPermission(context)
                // Use accounts to limit the query to contacts belong to the specified accounts.
                // Not passing any accounts or not calling accounts will default to searching all
                // accounts.
                .accounts(selectedAccounts)
                // Not passing any fields or not calling include will default to including all
                // fields.
                .include(SEARCH_FIELDS)
                // Passing null or not calling where will default to returning all contacts.
                .where(where)
                // Not passing any orderBys or not calling orderBy will default to ordering by ID.
                .orderBy(ContactsFields.DisplayNamePrimary.asc())
                // Not showing how to load x number of contacts at a time and then loading more when
                // scrolled to the very bottom of the list for brevity. Consumers can figure it out.
                .offset(0)  // offset is 0 if offset function is not called
                .limit(Int.MAX_VALUE) // limit is Int.MAX_VALUE if limit function is not called
                .findWithContext()

            setContactsAdapterItems()

            // Uncommenting this may make the UI thread choppy because it may result in logging
            // thousands of table rows. Only use this for debugging purposes.
            // TODO Make sure to comment out the below and remove all logging before going public!
            context.logContactsProviderTables()
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
            val contactId = searchResults[position].id ?: return
            // TODO Show contact details instead of edit
            EditContactDetailsActivity.editContactDetails(this@ContactsActivity, contactId)
        }
    }
}

private val SEARCH_FIELDS = setOf(
    Fields.Contact.DisplayNamePrimary,
    Fields.Email.Address,
    Fields.Phone.Number,
    Fields.Phone.NormalizedNumber
)