package contacts.sample

import android.accounts.Account
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ListView.*
import contacts.async.accounts.findWithContext
import contacts.core.entities.Group
import contacts.permissions.accounts.queryWithPermission
import contacts.sample.AccountsActivity.Companion.CHOICE_MODE
import contacts.sample.AccountsActivity.Companion.REQUEST_SELECT_ACCOUNTS
import contacts.sample.AccountsActivity.Companion.REQUEST_SELECT_GROUPS
import contacts.sample.util.trueKeys
import kotlinx.coroutines.launch

/**
 * Shows the list of all available accounts.
 *
 * If [REQUEST_SELECT_ACCOUNTS], allows the user to select one or more accounts depending on the
 * [CHOICE_MODE]. The selected account(s) will be included in the result of this activity.
 *
 * If [REQUEST_SELECT_GROUPS], allows the user to select groups from each account. The selected
 * groups(s) will be included in the result of this activity.
 *
 * ## Note
 *
 * This is a very rudimentary activity that is not styled or made to look good. It may not follow
 * any good practices and may even implement bad practices. This is for demonstration purposes only!
 *
 * This does not support state retention (e.g. device rotation). The OSS community may contribute to
 * this by implementing it.
 */
class AccountsActivity : BaseActivity() {

    // The ArrayAdapter does not allow for null objects. E.G. Adding a null Account crashes the app.
    // Therefore, we maintain the List<Account?> separately so that we can retrieve the selected
    // Accounts via the checked item position. The null Account is the "Local Account".
    private lateinit var accountsAdapter: ArrayAdapter<String>
    private val selectableAccounts = mutableListOf<Account?>()

    private val selectedAccounts: List<Account?>
        get() = accountsListView
            .checkedItemPositions
            ?.trueKeys
            ?.map { selectableAccounts[it] }
            ?: emptyList()

    private val selectedAccountGroups: MutableMap<Account?, List<Group>> = mutableMapOf()

    // Not using any view binding libraries or plugins just for this.
    private lateinit var accountsListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accounts)
        setupAccountsListView()

        launch {
            addLocalAccount()
            addAllAccounts()
            initSelectedAccountGroups()
            checkSelectedAccounts()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        GroupsActivity.onSelectGroupsResult(
            requestCode, resultCode, data
        ) { account, selectedGroups ->
            selectedAccountGroups[account] = selectedGroups
        }
    }

    override fun finish() {
        setResult(RESULT_OK, Intent().apply {
            putParcelableArrayListExtra(SELECTED_ACCOUNTS, ArrayList(selectedAccounts))
            putParcelableArrayListExtra(SELECTED_GROUPS, ArrayList(
                selectedAccountGroups.flatMap { it.value }
            ))
        })
        super.finish()
    }

    private fun setupAccountsListView() {
        // [ANDROID X] Not using RecyclerView to avoid dependency on androidx.recyclerview.
        // Obviously, everyone should use RecyclerView. I'm just being stupid here by trying to
        // avoid adding dependencies to make this repo as lean as possible.
        accountsListView = findViewById(R.id.accountsListView)
        // For simple cases like this though, ListView actually saves us from writing a bit of code.
        // We can just use the built-in choice mode functionality instead of writing it ourselves =)
        accountsAdapter = ArrayAdapter(
            this,
            if (intent.choiceMode() == CHOICE_MODE_NONE) {
                android.R.layout.simple_list_item_1
            } else {
                android.R.layout.simple_list_item_multiple_choice
            }
        )
        accountsListView.adapter = accountsAdapter
        accountsListView.choiceMode = intent.choiceMode()
        if (intent.choiceMode() == CHOICE_MODE_NONE) {
            accountsListView.onItemClickListener = OnAccountClickListener()
        }
    }

    private fun addLocalAccount() {
        selectableAccounts.add(null)
        accountsAdapter.add("Local Account")
    }

    private suspend fun addAllAccounts() {
        val allAccounts = contacts.accounts().queryWithPermission().findWithContext()
        selectableAccounts.addAll(allAccounts)
        accountsAdapter.addAll(allAccounts.map { account ->
            """
                |${account.name}
                |${account.type}
            """.trimMargin()
        })
    }

    private fun initSelectedAccountGroups() {
        for (account in selectableAccounts) {
            selectedAccountGroups[account] =
                intent.selectedGroups().filter { it.account == account }
        }
    }

    private fun checkSelectedAccounts() {
        if (intent.choiceMode() == CHOICE_MODE_NONE) {
            return
        }

        for (account in intent.selectedAccounts()) {
            val itemPosition = selectableAccounts.indexOf(account)
            if (itemPosition > -1) {
                // The ListView, ArrayAdapter, and selectableAccounts all have the same list of
                // accounts in the same indices.
                accountsListView.setItemChecked(itemPosition, true)
            }
        }
    }

    private inner class OnAccountClickListener : AdapterView.OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val account = selectableAccounts[position]
            val selectedGroupIds = selectedAccountGroups[account]?.map { it.id } ?: emptyList()
            GroupsActivity.selectGroups(
                this@AccountsActivity, true, account, selectedGroupIds
            )
        }
    }

    companion object {

        // region SELECT ACCOUNTS

        fun selectAccounts(
            activity: Activity, multipleChoice: Boolean, selectedAccounts: ArrayList<Account?>
        ) {
            val intent = Intent(activity, AccountsActivity::class.java).apply {
                putExtra(
                    CHOICE_MODE,
                    if (multipleChoice) CHOICE_MODE_MULTIPLE else CHOICE_MODE_SINGLE
                )
                putParcelableArrayListExtra(SELECTED_ACCOUNTS, selectedAccounts)
            }

            activity.startActivityForResult(intent, REQUEST_SELECT_ACCOUNTS)
        }

        fun onSelectAccountsResult(
            requestCode: Int, resultCode: Int, data: Intent?,
            processSelectedAccounts: (selectedAccounts: List<Account?>) -> Unit
        ) {
            if (requestCode != REQUEST_SELECT_ACCOUNTS || resultCode != RESULT_OK || data == null) {
                return
            }

            processSelectedAccounts(data.selectedAccounts())
        }

        // endregion

        // region SELECT GROUPS

        fun selectGroups(activity: Activity, selectedGroups: ArrayList<Group>) {
            val intent = Intent(activity, AccountsActivity::class.java).apply {
                putExtra(CHOICE_MODE, CHOICE_MODE_NONE)
                putParcelableArrayListExtra(SELECTED_GROUPS, selectedGroups)
            }

            activity.startActivityForResult(intent, REQUEST_SELECT_GROUPS)
        }

        fun onSelectGroupsResult(
            requestCode: Int, resultCode: Int, data: Intent?,
            processSelectedGroups: (selectedGroups: List<Group>) -> Unit
        ) {
            if (requestCode != REQUEST_SELECT_GROUPS || resultCode != RESULT_OK || data == null) {
                return
            }

            processSelectedGroups(data.selectedGroups())
        }

        // endregion

        private fun Intent.choiceMode(): Int = getIntExtra(CHOICE_MODE, CHOICE_MODE_NONE)

        private fun Intent.selectedAccounts(): List<Account?> =
            getParcelableArrayListExtra(SELECTED_ACCOUNTS) ?: emptyList()

        private fun Intent.selectedGroups(): List<Group> =
            getParcelableArrayListExtra(SELECTED_GROUPS) ?: emptyList()

        private const val REQUEST_SELECT_ACCOUNTS = 101
        private const val REQUEST_SELECT_GROUPS = 102
        private const val CHOICE_MODE = "choiceMode"
        private const val SELECTED_ACCOUNTS = "selectedAccounts"
        private const val SELECTED_GROUPS = "selectedGroups"
    }
}