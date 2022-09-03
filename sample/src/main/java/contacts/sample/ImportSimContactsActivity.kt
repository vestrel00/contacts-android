package contacts.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import contacts.async.commitWithContext
import contacts.async.findWithContext
import contacts.async.sim.findWithContext
import contacts.core.Contacts
import contacts.core.entities.SimContact
import contacts.core.equalTo
import contacts.core.util.toNewRawContacts
import contacts.permissions.insertWithPermission
import contacts.permissions.queryWithPermission
import contacts.permissions.sim.queryWithPermission
import contacts.sample.util.trueKeys
import kotlinx.coroutines.launch

/**
 * Shows the list of all SIM contacts.
 *
 * All SIM contacts are initially selected except for those that are already in the Contacts
 * Provider database (which are not selectable).
 *
 * All selected SIM contacts are allowed to be imported/inserted into the Contacts Provider DB.
 */
class ImportSimContactsActivity : BaseActivity() {

    // Not using any view binding libraries or plugins just for this.
    private lateinit var simContactsListView: ListView
    private lateinit var simContactsAdapter: ArrayAdapter<SimContactsListItem>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_import_sim_contacts)

        if (!contacts.sim().state.isReady) {
            showToast(R.string.import_sim_contacts_card_not_ready)
            setResult(RESULT_CANCELED)
            finish()
            return
        }

        setupSimContactsListView()

        launch {
            showProgressDialog()
            addAllSimContacts()
            checkSimContactsNotAlreadyInContactsProviderDb()
            invalidateOptionsMenu()
            dismissProgressDialog()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_import_sim_contacts, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            menu.findItem(R.id.import_sim_contacts).isVisible =
                simContactsListView.checkedItemCount > 0
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.import_sim_contacts -> launch {
                importCheckedSimContacts()
            }
        }

        return super.onOptionsItemSelected(menuItem)
    }

    private fun setupSimContactsListView() {
        simContactsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice)

        // [ANDROID X] Not using RecyclerView to avoid dependency on androidx.recyclerview.
        // Obviously, everyone should use RecyclerView. I'm just being stupid here by trying to
        // avoid adding dependencies to make this repo as lean as possible.
        simContactsListView = findViewById(R.id.simContactsListView)
        simContactsListView.apply {
            adapter = simContactsAdapter
            choiceMode = ListView.CHOICE_MODE_MULTIPLE
            onItemClickListener = OnSimContactsListItemClickListener()
        }
    }

    private suspend fun addAllSimContacts() {
        val simContacts = contacts.sim().queryWithPermission().findWithContext()
        simContactsAdapter.addAll(simContacts.toSimContactsListItem(contacts))
    }

    private fun checkSimContactsNotAlreadyInContactsProviderDb() {
        for (position in 0 until simContactsAdapter.count) {
            simContactsAdapter.getItem(position)?.let { simContactsListItem ->
                simContactsListView.setItemChecked(
                    position,
                    !simContactsListItem.isAlreadyInContactsProviderDb
                )
            }
        }
    }

    private suspend fun importCheckedSimContacts() {
        showProgressDialog()

        val checkedSimContacts = simContactsListView
            .checkedItemPositions
            .trueKeys
            .mapNotNull { simContactsAdapter.getItem(it) }
            .map { it.simContact }

        val importResult = contacts.insertWithPermission()
            .forAccount(preferences.defaultAccountForNewContacts)
            .rawContacts(checkedSimContacts.toNewRawContacts())
            .commitWithContext()

        dismissProgressDialog()

        if (importResult.isSuccessful) {
            showToast(R.string.import_sim_contacts_success)
            setResult(RESULT_OK)
            finish()
        } else {
            showToast(R.string.import_sim_contacts_failed)
        }
    }

    private inner class OnSimContactsListItemClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            simContactsAdapter.getItem(position)?.let {
                if (it.isAlreadyInContactsProviderDb) {
                    simContactsListView.setItemChecked(position, false)
                    showToast(R.string.import_sim_contacts_already_in_contacts_provider_db)
                }
            }
            invalidateOptionsMenu()
        }
    }

    companion object {
        fun importSimContacts(activity: Activity) {
            activity.startActivityForResult(
                Intent(activity, ImportSimContactsActivity::class.java),
                REQUEST_IMPORT_SIM_CONTACTS
            )
        }

        fun onImportSimContactsResult(
            requestCode: Int,
            resultCode: Int,
            onImportSimContactsCompleted: () -> Unit
        ) {
            if (requestCode != REQUEST_IMPORT_SIM_CONTACTS || resultCode != RESULT_OK) {
                return
            }

            onImportSimContactsCompleted()
        }

        private const val REQUEST_IMPORT_SIM_CONTACTS = 97894578
    }
}

/**
 * This logic is based on the Google Contacts app's implementation of import from SIM function.
 *
 * Contacts in the SIM card that meet one of the following criteria are NOT imported into the
 * Contacts Provider database because they already exist/imported;
 *
 * 1. Contacts in the SIM card that matches (case sensitive) a name display name (ignoring all
 * other structured name components such as given, middle, and family name) in the Contacts
 * Provider database.
 *
 * 2. Contacts in the SIM card that have no name (null) but have a number that matches a phone
 * number (ignoring normalized number) in the Contacts Provider database.
 *
 * SIM contacts that have a name that does NOT match any names in the Contacts Provider DB but has
 * a matching phone number will still be imported. In other words, if the SIM contact has a non-null
 * name, then it does not matter the value of its number.
 *
 * This implementation is app-specific and can be different per app. However, this sample app
 * implementation will follow the Google Contacts app behavior!
 */
private suspend fun List<SimContact>.toSimContactsListItem(contactsApi: Contacts): List<SimContactsListItem> =
    map { simContact ->
        val isAlreadyInContactsProviderDb = contactsApi
            .queryWithPermission()
            .where {
                if (simContact.name != null) {
                    // Case sensitive matching, just like the Google Contacts app.
                    Name.DisplayName equalTo "${simContact.name}"
                } else {
                    // Use the number and ignore the normalized number, just like the Google Contacts app.
                    Phone.Number equalTo "${simContact.number}"
                }
            }
            .findWithContext()
            .isNotEmpty()

        SimContactsListItem(simContact, isAlreadyInContactsProviderDb)
    }


private data class SimContactsListItem(
    val simContact: SimContact,
    val isAlreadyInContactsProviderDb: Boolean
) {
    override fun toString() = "Name: ${simContact.name}, Number: ${simContact.number}"
}