package contacts.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import contacts.async.profile.findWithContext
import contacts.core.Fields
import contacts.permissions.profile.queryWithPermission
import kotlinx.coroutines.launch

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<View>(R.id.profile).setOnClickListener { launch { showProfile() } }
        findViewById<View>(R.id.accounts).setOnClickListener { showAccounts() }
        findViewById<View>(R.id.default_account).setOnClickListener { chooseDefaultAccount() }
        findViewById<Spinner>(R.id.sort_by).setupSortBy()
        findViewById<Spinner>(R.id.name_format).setupNameFormat()
        findViewById<Spinner>(R.id.phonetic_name).setupPhoneticName()
        findViewById<View>(R.id.import_sim_contacts).setOnClickListener { importSimContacts() }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        AccountsActivity.onSelectAccountsResult(requestCode, resultCode, data) { accounts, _ ->
            preferences.defaultAccountForNewContacts = accounts.firstOrNull()
            setResult(RESULT_OK)
        }
        ImportSimContactsActivity.onImportSimContactsResult(requestCode, resultCode) {
            setResult(RESULT_OK)
        }
    }

    private suspend fun showProfile() {
        val profile = contacts
            .profile()
            .queryWithPermission()
            .include(Fields.Contact.LookupKey)
            .findWithContext()
            .contact

        if (profile != null) {
            ContactDetailsActivity.viewProfileDetails(this)
        } else {
            ContactDetailsActivity.createProfile(this)
        }
    }

    private fun showAccounts() {
        startActivity(Intent(Settings.ACTION_SYNC_SETTINGS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        })
    }

    private fun chooseDefaultAccount() {
        AccountsActivity.selectAccounts(
            this,
            false,
            arrayListOf(preferences.defaultAccountForNewContacts)
        )
    }

    private fun Spinner.setupSortBy() {
        adapter = ArrayAdapter(context, R.layout.simple_list_item_1_no_padding, SortBy.values())
            .also { it.setDropDownViewResource(android.R.layout.simple_list_item_1) }

        setSelection(preferences.sortBy.ordinal)

        onItemPositionSelected { position ->
            preferences.sortBy = adapter.getItem(position) as SortBy
        }
    }

    private fun Spinner.setupNameFormat() {
        adapter = ArrayAdapter(context, R.layout.simple_list_item_1_no_padding, NameFormat.values())
            .also { it.setDropDownViewResource(android.R.layout.simple_list_item_1) }

        setSelection(preferences.nameFormat.ordinal)

        onItemPositionSelected { position ->
            preferences.nameFormat = adapter.getItem(position) as NameFormat
        }
    }

    private fun Spinner.setupPhoneticName() {
        adapter =
            ArrayAdapter(context, R.layout.simple_list_item_1_no_padding, PhoneticName.values())
                .also { it.setDropDownViewResource(android.R.layout.simple_list_item_1) }

        setSelection(preferences.phoneticName.ordinal)

        onItemPositionSelected { position ->
            preferences.phoneticName = adapter.getItem(position) as PhoneticName
        }
    }

    private inline fun Spinner.onItemPositionSelected(crossinline block: (position: Int) -> Unit) {
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                block(position)
                setResult(RESULT_OK)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun importSimContacts() {
        ImportSimContactsActivity.importSimContacts(this)
    }

    companion object {
        fun showSettings(activity: Activity) {
            activity.startActivityForResult(
                Intent(activity, SettingsActivity::class.java),
                REQUEST_SHOW_SETTINGS
            )
        }

        fun onShowSettingsResult(requestCode: Int, resultCode: Int, onSettingsChanged: () -> Unit) {
            if (requestCode != REQUEST_SHOW_SETTINGS || resultCode != RESULT_OK) {
                return
            }

            onSettingsChanged()
        }

        private const val REQUEST_SHOW_SETTINGS = 6735246
    }
}