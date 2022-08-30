package contacts.sample

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        findViewById<View>(R.id.accounts).setOnClickListener { showAccounts() }
        findViewById<View>(R.id.default_account).setOnClickListener { chooseDefaultAccount() }
        findViewById<Spinner>(R.id.sort_by).setupSortBy()
        findViewById<Spinner>(R.id.name_format).setupNameFormat()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        AccountsActivity.onSelectAccountsResult(requestCode, resultCode, data) {
            preferences.defaultAccountForNewContacts = it.firstOrNull()
            setResult(RESULT_OK)
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

        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                preferences.sortBy = adapter.getItem(position) as SortBy
                setResult(RESULT_OK)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun Spinner.setupNameFormat() {
        adapter = ArrayAdapter(context, R.layout.simple_list_item_1_no_padding, NameFormat.values())
            .also { it.setDropDownViewResource(android.R.layout.simple_list_item_1) }

        setSelection(preferences.nameFormat.ordinal)

        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {
                preferences.nameFormat = adapter.getItem(position) as NameFormat
                setResult(RESULT_OK)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
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