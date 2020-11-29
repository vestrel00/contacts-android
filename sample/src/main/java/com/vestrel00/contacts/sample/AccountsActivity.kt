package com.vestrel00.contacts.sample

import android.accounts.Account
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView.*
import com.vestrel00.contacts.accounts.Accounts
import contacts.permissions.accounts.queryWithPermission
import kotlinx.android.synthetic.main.activity_accounts.*
import kotlinx.coroutines.launch

/**
 * Shows the list of all available accounts, allowing the user to choose which account(s) to use.
 */
class AccountsActivity : BaseActivity() {

    // The ArrayAdapter does not allow for null objects. E.G. Adding a null Account crashes the app.
    // Therefore, we maintain the List<Account?> separately so that we can retrieve the selected
    // Accounts via the checked item position. The null Account is the "Local Account".
    private lateinit var accountsAdapter: ArrayAdapter<String>
    private val selectableAccounts = mutableListOf<Account?>()

    private val selectedAccounts: List<Account?>
        get() = mutableListOf<Account?>().apply {

            val checkedItemPositions = accountsListView.checkedItemPositions
            for (i in 0 until checkedItemPositions.size()) {
                val position = checkedItemPositions.keyAt(i)
                val isChecked = checkedItemPositions.valueAt(i)

                if (isChecked) {
                    // The ListView, ArrayAdapter, and selectableAccounts all have the same list of
                    // accounts in the same indices.
                    add(selectableAccounts[position])
                }
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_accounts)
        setupAccountsListView()
    }

    override fun finish() {
        setResult(RESULT_OK, Intent().apply {
            putParcelableArrayListExtra(SELECTED_ACCOUNTS, ArrayList(selectedAccounts))
        })
        super.finish()
    }

    private fun setupAccountsListView() {
        // [ANDROID X] Not using RecyclerView to avoid dependency on androidx.recyclerview.
        // Obviously, everyone should use RecyclerView. I'm just being stupid here by trying to
        // avoid adding dependencies to make this repo as lean as possible.
        // For simple cases like this though, ListView actually saves us from writing a bit of code.
        // We can just use the built-in choice mode functionality instead of writing it ourselves =)
        accountsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice)
        accountsListView.adapter = accountsAdapter
        accountsListView.choiceMode = intent.choiceMode()

        launch {
            addLocalAccount()
            addAllAccounts()
            checkSelectedAccounts()
        }
    }

    private fun addLocalAccount() {
        selectableAccounts.add(null)
        accountsAdapter.add("Local Account")
    }

    private suspend fun addAllAccounts() {
        val allAccounts = Accounts(this).queryWithPermission().allAccounts()
        selectableAccounts.addAll(allAccounts)
        accountsAdapter.addAll(allAccounts.map { account ->
            """
                |${account.name}
                |${account.type}
            """.trimMargin()
        })
    }

    private fun checkSelectedAccounts() {
        for (account in intent.selectedAccounts()) {
            val itemPosition = selectableAccounts.indexOf(account)
            if (itemPosition > -1) {
                // The ListView, ArrayAdapter, and selectableAccounts all have the same list of
                // accounts in the same indices.
                accountsListView.setItemChecked(itemPosition, true)
            }
        }
    }

    companion object {

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
            if (requestCode != REQUEST_SELECT_ACCOUNTS || resultCode != RESULT_OK ||
                data == null
            ) {
                return
            }

            processSelectedAccounts(data.selectedAccounts())
        }

        private fun Intent.choiceMode(): Int = getIntExtra(CHOICE_MODE, CHOICE_MODE_NONE)

        private fun Intent.selectedAccounts(): List<Account?> =
            getParcelableArrayListExtra(SELECTED_ACCOUNTS) ?: emptyList()

        private const val REQUEST_SELECT_ACCOUNTS = 101
        private const val CHOICE_MODE = "choiceMode"
        private const val SELECTED_ACCOUNTS = "selectedAccounts"
    }
}