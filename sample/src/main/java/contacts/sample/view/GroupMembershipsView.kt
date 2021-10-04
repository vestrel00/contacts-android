package contacts.sample.view

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import contacts.async.util.groupsWithContext
import contacts.core.entities.GroupMembership
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * A [TextView] that displays the [GroupMembership] of a of a RawContact and handles the
 * modifications to it.
 *
 * Setting the [memberships] will automatically update the views and vice versa.
 *
 * ## Note
 *
 * This is a very rudimentary view that is not styled or made to look good. It may not follow any
 * good practices and may even implement bad practices. Consumers of the library may choose to use
 * this as is or simply as a reference on how to implement this part of native Contacts app.
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

    var memberships: MutableList<GroupMembership> = mutableListOf()
        set(value) {
            field = value

            setMemberships()
        }

    init {
        setOnClickListener { _ ->
            TODO("Create GroupsPickerDialog to set group memberships")
        }
    }

    private fun setMemberships() = launch {
        val groups = memberships.groupsWithContext(context)
            // Hide the default group, just like in the native Contacts app.
            .filter { !it.isDefaultGroup }

        text = groups.joinToString { it.title }
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancel()
    }
}