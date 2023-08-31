package contacts.sample

import android.accounts.Account
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.AdapterView
import android.widget.AdapterView.OnItemClickListener
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.TextView
import contacts.async.groups.commitInOneTransactionWithContext
import contacts.async.groups.commitWithContext
import contacts.async.groups.findWithContext
import contacts.core.entities.ExistingGroupEntity
import contacts.core.entities.Group
import contacts.core.entities.NewGroup
import contacts.core.groups.GroupsInsert
import contacts.core.groups.GroupsUpdate
import contacts.permissions.groups.deleteWithPermission
import contacts.permissions.groups.insertWithPermission
import contacts.permissions.groups.queryWithPermission
import contacts.permissions.groups.updateWithPermission
import contacts.sample.util.getParcelableArrayListExtraCompat
import contacts.sample.util.getParcelableExtraCompat
import contacts.ui.util.UserInputDialog
import kotlinx.coroutines.launch

/**
 * Shows the list of all the groups of a given account, allowing the user to choose which groups to
 * use.
 *
 * The chosen group(s) will be included in the result of this activity.
 *
 * #### Options Menu
 *
 * - Create: Opens an input dialog to allow user to create a new group.
 *     - This is visible no matter how many groups are selected, including zero groups.
 * - Edit: Opens an input dialog to allow user to edit the group title.
 *     - This is only visible when there is exactly one group selected. Only allow one group at
 *       a time to be edited so that errors that pop up are specific to one group.
 * - Delete: Deletes all selected groups.
 *     - This is only visible when there is exactly one group selected. Only allow one group at
 *       a time to be deleted so that errors that pop up are specific to one group.
 *
 * ## Note
 *
 * This is a very rudimentary activity that is not styled or made to look good. It may not follow
 * any good practices and may even implement bad practices. This is for demonstration purposes only!
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
    private lateinit var emptyGroupsTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groups)

        // [ANDROID X] Not using RecyclerView to avoid dependency on androidx.recyclerview.
        // Obviously, everyone should use RecyclerView. I'm just being stupid here by trying to
        // avoid adding dependencies to make this repo as lean as possible.
        groupsListView = findViewById(R.id.groupsListView)
        emptyGroupsTextView = findViewById(R.id.emptyGroupsTextView)

        setupGroupsListView()

        launch {
            refreshGroupsList(
                if (savedInstanceState != null) {
                    savedInstanceState.getLongArray(SELECTED_GROUP_IDS)?.asList() ?: emptyList()
                } else {
                    intent.selectedGroupIds()
                }
            )
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_groups, menu)
        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        if (menu != null) {
            val editMenuItem = menu.findItem(R.id.edit)
            val deleteMenuItem = menu.findItem(R.id.delete)

            val exactlyOneGroupIsSelected = groupsListView.checkedItemCount == 1
            editMenuItem.isVisible = exactlyOneGroupIsSelected
            deleteMenuItem.isVisible = exactlyOneGroupIsSelected
        }
        return super.onPrepareOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.create -> showGroupCreationDialog()
            R.id.edit -> showGroupEditDialog()
            R.id.delete -> launch { deleteSelectedGroups() }
        }

        return super.onOptionsItemSelected(menuItem)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putLongArray(SELECTED_GROUP_IDS, selectedGroups.map { it.id }.toLongArray())
    }

    override fun finish() {
        setResult(RESULT_OK, Intent().apply {
            putExtra(ACCOUNT, intent.account())
            putParcelableArrayListExtra(SELECTED_GROUPS, ArrayList(selectedGroups))
        })
        super.finish()
    }

    private fun setupGroupsListView() {
        // For simple cases like this though, ListView actually saves us from writing a bit of code.
        // We can just use the built-in choice mode functionality instead of writing it ourselves =)
        groupsAdapter = ArrayAdapter(this, android.R.layout.simple_list_item_multiple_choice)
        groupsListView.adapter = groupsAdapter
        groupsListView.choiceMode = ListView.CHOICE_MODE_MULTIPLE
        groupsListView.onItemClickListener = OnGroupSelectedListener()
    }

    private suspend fun addAllGroupsForAccount() {
        val accountGroups = contacts
            .groups()
            .queryWithPermission()
            .accounts(intent.account())
            .findWithContext()
            // Hide the default and favorites group, just like in the AOSP Contacts app.
            .filter { !it.isDefaultGroup && !it.isFavoritesGroup }

        if (accountGroups.isEmpty()) {
            groupsListView.visibility = View.GONE
            emptyGroupsTextView.visibility = View.VISIBLE
        } else {
            groupsListView.visibility = View.VISIBLE
            emptyGroupsTextView.visibility = View.GONE

            selectableGroups.addAll(accountGroups)
            groupsAdapter.addAll(accountGroups.map { it.title })
        }
    }

    private fun checkGroupsWithIds(groupIds: List<Long>) {
        for (groupId in groupIds) {
            val itemPosition = selectableGroups.indexOfFirst { it.id == groupId }
            if (itemPosition > -1) {
                // The ListView, ArrayAdapter, and selectableGroups all have the same list of
                // groups in the same indices.
                groupsListView.setItemChecked(itemPosition, true)
            }
        }
    }

    private fun showGroupCreationDialog() {
        UserInputDialog(this).show(
            titleRes = R.string.groups_create_input_dialog_title,
            onTextEntered = {
                launch { insertGroup(it) }
            }
        )
    }

    private fun showGroupEditDialog() {
        // We are assuming that there is only one selected group.
        val selectedGroup = selectedGroups.firstOrNull() ?: return

        UserInputDialog(this).show(
            titleRes = R.string.groups_edit_input_dialog_title,
            initialText = selectedGroup.title,
            onTextEntered = {
                launch { updateGroup(selectedGroup.mutableCopy { title = it }) }
            }
        )
    }

    private suspend fun insertGroup(groupTitle: String) {
        val newGroup = NewGroup(groupTitle, intent.account())
        val insertResult = contacts
            .groups()
            .insertWithPermission()
            .groups(newGroup)
            .commitWithContext()

        if (insertResult.isSuccessful) {
            val selectedGroupIds = selectedGroups.map { it.id }
            refreshGroupsList(selectedGroupIds + insertResult.groupIds)
            showToast(R.string.groups_create_success)
        } else when (insertResult.failureReason(newGroup)) {
            GroupsInsert.Result.FailureReason.TITLE_ALREADY_EXIST ->
                showToast(R.string.groups_create_error_title_already_exist)

            else -> showToast(R.string.groups_create_error)
        }
    }

    private suspend fun updateGroup(group: ExistingGroupEntity) {
        val updateResult = contacts
            .groups()
            .updateWithPermission()
            .groups(group)
            .commitWithContext()

        if (updateResult.isSuccessful) {
            refreshGroupsList(selectedGroups.map { it.id })
            showToast(R.string.groups_edit_success)
        } else when (updateResult.failureReason(group)) {
            GroupsUpdate.Result.FailureReason.TITLE_ALREADY_EXIST ->
                showToast(R.string.groups_edit_error_title_already_exist)

            else -> showToast(R.string.groups_edit_error)
        }
    }

    private suspend fun deleteSelectedGroups() {
        val deleteResult = contacts
            .groups()
            .deleteWithPermission()
            .groups(selectedGroups)
            .commitInOneTransactionWithContext()

        if (deleteResult.isSuccessful) {
            refreshGroupsList(emptyList())
            showToast(R.string.groups_delete_success)
        } else {
            showToast(R.string.groups_delete_error)
        }
    }

    private suspend fun refreshGroupsList(groupsWithIdsToCheck: List<Long>) {
        groupsListView.clearChoices()
        groupsAdapter.clear()
        selectableGroups.clear()

        addAllGroupsForAccount()
        checkGroupsWithIds(groupsWithIdsToCheck)
        invalidateOptionsMenu()
    }

    private inner class OnGroupSelectedListener : OnItemClickListener {
        override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            invalidateOptionsMenu()
        }
    }

    companion object {

        fun selectGroups(
            activity: Activity,
            account: Account?,
            selectedGroupIds: List<Long>
        ) {
            val intent = Intent(activity, GroupsActivity::class.java).apply {
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

        private fun Intent.account(): Account? = getParcelableExtraCompat(ACCOUNT)

        private fun Intent.selectedGroupIds(): List<Long> =
            getLongArrayExtra(SELECTED_GROUP_IDS)?.asList() ?: emptyList()

        private fun Intent.selectedGroups(): List<Group> =
            getParcelableArrayListExtraCompat(SELECTED_GROUPS) ?: emptyList()

        private const val REQUEST_SELECT_GROUPS = 112
        private const val ACCOUNT = "account"
        private const val SELECTED_GROUPS = "selectedGroups"
        private const val SELECTED_GROUP_IDS = "selectedGroupIds"
    }
}