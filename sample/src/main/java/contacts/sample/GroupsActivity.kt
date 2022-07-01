package contacts.sample

import android.accounts.Account
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ListView.*
import contacts.async.groups.findWithContext
import contacts.core.entities.Group
import contacts.permissions.groups.queryWithPermission
import kotlinx.coroutines.launch

/**
 * Shows the list of all the groups of a given account, allowing the user to choose which groups to
 * use.
 *
 * The chosen group(s) will be included in the result of this activity.
 *
 * ## Note
 *
 * This is a very rudimentary activity that is not styled or made to look good. It may not follow
 * any good practices and may even implement bad practices. This is for demonstration purposes only!
 *
 * This does not support state retention (e.g. device rotation). The OSS community may contribute to
 * this by implementing it.
 */
class GroupsActivity : BaseActivity() {

    private lateinit var groupsAdapter: ArrayAdapter<String>
    private val selectableGroups = mutableListOf<Group>()

    private val selectedGroups: List<Group>
        get() = mutableListOf<Group>().apply {

            val checkedItemPositions = groupsListView.checkedItemPositions
            for (i in 0 until checkedItemPositions.size()) {
                val position = checkedItemPositions.keyAt(i)
                val isChecked = checkedItemPositions.valueAt(i)

                if (isChecked) {
                    // The ListView, ArrayAdapter, and selectableGroups all have the same list of
                    // groups in the same indices.
                    add(selectableGroups[position])
                }
            }
        }

    // Not using any view binding libraries or plugins just for this.
    private lateinit var groupsListView: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // TODO Issue #167 Remove this check as account restrictions for groups will be removed
        if (intent.account() == null) {
            showToast(R.string.groups_local_account_not_yet_supported)
            finish()
            return
        }

        setContentView(R.layout.activity_groups)
        setupGroupsListView()

        launch {
            addAllGroupsForAccount()
            checkSelectedGroups()
        }
    }

    override fun finish() {
        setResult(RESULT_OK, Intent().apply {
            putExtra(ACCOUNT, intent.account())
            putParcelableArrayListExtra(SELECTED_GROUPS, ArrayList(selectedGroups))
        })
        super.finish()
    }

    private fun setupGroupsListView() {
        // [ANDROID X] Not using RecyclerView to avoid dependency on androidx.recyclerview.
        // Obviously, everyone should use RecyclerView. I'm just being stupid here by trying to
        // avoid adding dependencies to make this repo as lean as possible.
        groupsListView = findViewById(R.id.groupsListView)
        // For simple cases like this though, ListView actually saves us from writing a bit of code.
        // We can just use the built-in choice mode functionality instead of writing it ourselves =)
        groupsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice)
        groupsListView.adapter = groupsAdapter
        groupsListView.choiceMode = intent.choiceMode()
    }

    private suspend fun addAllGroupsForAccount() {
        val accountGroups = contacts
            .groups()
            .queryWithPermission()
            // TODO Issue #167 Remove !! as account restrictions for groups will be removed
            .accounts(intent.account()!!)
            .findWithContext()
            // Hide the default group, just like in the native Contacts app.
            .filter { !it.isDefaultGroup }

        selectableGroups.addAll(accountGroups)
        groupsAdapter.addAll(
            accountGroups.map {
                if (it.isFavoritesGroup) {
                    // The title for the favorites group is "Starred in Android". We'll show
                    // "Favorites" instead.
                    "Favorites"
                } else {
                    it.title
                }
            }
        )
    }

    private fun checkSelectedGroups() {
        for (groupId in intent.selectedGroupIds()) {
            val itemPosition = selectableGroups.indexOfFirst { it.id == groupId }
            if (itemPosition > -1) {
                // The ListView, ArrayAdapter, and selectableGroups all have the same list of
                // groups in the same indices.
                groupsListView.setItemChecked(itemPosition, true)
            }
        }
    }

    companion object {

        fun selectGroups(
            activity: Activity,
            multipleChoice: Boolean,
            account: Account?,
            selectedGroupIds: List<Long>
        ) {
            val intent = Intent(activity, GroupsActivity::class.java).apply {
                putExtra(
                    CHOICE_MODE,
                    if (multipleChoice) CHOICE_MODE_MULTIPLE else CHOICE_MODE_SINGLE
                )
                putExtra(
                    SELECTED_GROUP_IDS,
                    LongArray(selectedGroupIds.size) { index -> selectedGroupIds[index] }
                )
                putExtra(ACCOUNT, account)
            }

            activity.startActivityForResult(intent, REQUEST_SELECT_GROUPS)
        }

        fun onSelectGroupsResult(
            requestCode: Int, resultCode: Int, data: Intent?,
            processSelectedGroups: (account: Account?, selectedGroups: List<Group>) -> Unit
        ) {
            if (requestCode != REQUEST_SELECT_GROUPS || resultCode != RESULT_OK || data == null) {
                return
            }

            processSelectedGroups(data.account(), data.selectedGroups())
        }

        private fun Intent.choiceMode(): Int = getIntExtra(CHOICE_MODE, CHOICE_MODE_NONE)

        private fun Intent.account(): Account? = getParcelableExtra(ACCOUNT)

        private fun Intent.selectedGroupIds(): List<Long> =
            getLongArrayExtra(SELECTED_GROUP_IDS)?.asList() ?: emptyList()

        private fun Intent.selectedGroups(): List<Group> =
            getParcelableArrayListExtra(SELECTED_GROUPS) ?: emptyList()

        private const val REQUEST_SELECT_GROUPS = 112
        private const val CHOICE_MODE = "choiceMode"
        private const val ACCOUNT = "account"
        private const val SELECTED_GROUPS = "selectedGroups"
        private const val SELECTED_GROUP_IDS = "selectedGroupIds"
    }
}