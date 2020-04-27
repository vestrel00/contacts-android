package com.vestrel00.contacts.sample

import android.accounts.Account
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView.*
import com.vestrel00.contacts.accounts.Accounts
import com.vestrel00.contacts.permissions.accounts.allAccountsWithPermission
import kotlinx.android.synthetic.main.activity_accounts.*
import kotlinx.coroutines.launch

/**
 * Shows the list of all available accounts, allowing the user to choose which account(s) to use.
 */
class AccountsActivity : BaseActivity() {

    private lateinit var accountsAdapter: ArrayAdapter<Account>

    private val selectedAccounts: List<Account>
        get() {
            val selectedAccounts = mutableListOf<Account>()

            val checkedItemPositions = accountsListView.checkedItemPositions
            for (i in 0 until checkedItemPositions.size()) {
                val position = checkedItemPositions.keyAt(i)
                val isChecked = checkedItemPositions.valueAt(i)

                if (isChecked) {
                    accountsAdapter.getItem(position)?.let(selectedAccounts::add)
                }
            }

            return selectedAccounts
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
            addAllAccounts()
            checkSelectedAccounts()
        }
    }

    private suspend fun addAllAccounts() {
        val accounts = Accounts().allAccountsWithPermission(this)
        accountsAdapter.addAll(accounts)
    }

    private fun checkSelectedAccounts() {
        for (account in intent.selectedAccounts()) {
            val itemPosition = accountsAdapter.getPosition(account)
            if (itemPosition > -1) {
                accountsListView.setItemChecked(itemPosition, true)
            }
        }
    }

    companion object {

        fun selectAccounts(
            activity: Activity, multipleChoice: Boolean, selectedAccounts: ArrayList<Account>
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
            processSelectedAccounts: (selectedAccounts: List<Account>) -> Unit
        ) {
            if (requestCode != REQUEST_SELECT_ACCOUNTS || resultCode != RESULT_OK ||
                data == null
            ) {
                return
            }

            processSelectedAccounts(data.selectedAccounts())
        }

        private fun Intent.choiceMode(): Int = getIntExtra(CHOICE_MODE, CHOICE_MODE_NONE)

        private fun Intent.selectedAccounts(): List<Account> =
            getParcelableArrayListExtra(SELECTED_ACCOUNTS)

        private const val REQUEST_SELECT_ACCOUNTS = 101
        private const val CHOICE_MODE = "choiceMode"
        private const val SELECTED_ACCOUNTS = "selectedAccounts"
    }
}

