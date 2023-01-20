package contacts.sample.view

import android.accounts.Account
import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.widget.TextView
import contacts.async.util.groupsWithContext
import contacts.core.Contacts
import contacts.core.entities.ExistingGroupEntity
import contacts.core.entities.GroupMembershipEntity
import contacts.core.util.newMemberships
import contacts.sample.GroupsActivity
import contacts.sample.R
import contacts.ui.view.activity
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * A [TextView] that displays the [GroupMembershipEntity] of a of a RawContact and handles the
 * modifications to it.
 *
 * Setting the [memberships] will automatically update the views and vice versa.
 *
 * ## Note
 *
 * This is a very rudimentary view that is not styled or made to look good. It may not follow any
 * good practices and may even implement bad practices. Consumers of the library may choose to use
 * this as is or simply as a reference on how to implement this part of AOSP Contacts app.
 *
 * This does not support state retention (e.g. device rotation). The OSS community may contribute to
 * this by implementing it.
 *
 * The community may contribute by styling and adding more features and customizations with these
 * views if desired.
 *
 * ## Developer Notes
 *
 * I usually am a proponent of passive views and don't add any logic to views. However, I will make
 * an exception for this basic view that I don't really encourage consumers to use.
 *
 * This is in the sample and not in the contacts-ui module because it requires concurrency. We
 * should not add coroutines and contacts-async as dependencies to contacts-ui just for this.
 * Consumers may copy and paste this into their projects or if the community really wants it, we may
 * move this to a separate module (contacts-ui-async).
 */
class GroupMembershipsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextView(context, attributeSet, defStyleAttr), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.Main

    private var account: Account? = null
    private var memberships: MutableList<GroupMembershipEntity> = mutableListOf()

    init {
        setHint(R.string.raw_contact_group_memberships_hint)
    }

    /**
     * Sets the group memberships shown and managed by this view to the given [memberships], assumed
     * to belong to the given [account], and uses the given [contacts] API to perform operations on
     * it.
     */
    fun setMemberships(
        memberships: MutableList<GroupMembershipEntity>,
        account: Account?,
        contacts: Contacts
    ) {
        this.account = account
        this.memberships = memberships
        showMemberships(contacts)
    }

    private fun showMemberships(contacts: Contacts) = launch {
        val groups = memberships.groupsWithContext(contacts)
            // Hide the default group, just like in the AOSP Contacts app.
            .filter { !it.isDefaultGroup }

        showMemberships(groups)
    }

    private fun showMemberships(groups: List<ExistingGroupEntity>) {
        text = groups.joinToString { it.title }
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        setOnClickListener {
            activity?.let { activity ->
                GroupsActivity.selectGroups(
                    activity,
                    true,
                    account,
                    memberships.mapNotNull { it.groupId }
                )
            }
        }
    }

    /**
     * Invoke this method on the host activity's onActivityResult in order to process the picked
     * groups (if any). This will do nothing if the request did not originate from this view.
     */
    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        GroupsActivity.onSelectGroupsResult(
            requestCode, resultCode, data
        ) { _, selectedGroups ->
            // There is no need for consumers to keep track of the previous group membership rows.
            // The API will internally reuse group membership rows that already exist in the DB
            // based on groupIds instead of performing a delete-and-insert.
            // Make sure to not replace the reference to memberships with a new one for the save
            // operation.
            memberships.clear()
            memberships.addAll(selectedGroups.newMemberships())
            showMemberships(selectedGroups)
        }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancel()
    }
}