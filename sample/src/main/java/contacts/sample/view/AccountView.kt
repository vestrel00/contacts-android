package contacts.sample.view

import android.accounts.Account
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import contacts.async.accounts.findWithContext
import contacts.core.Contacts
import contacts.core.entities.ExistingRawContactEntity
import contacts.core.entities.RawContactEntity
import contacts.permissions.accounts.queryWithPermission
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * A [TextView] that displays an immutable [Account] from the given [RawContactEntity].
 *
 * Setting the [rawContact] will automatically update the views.
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
class AccountView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : TextView(context, attributeSet, defStyleAttr), CoroutineScope {

    override val coroutineContext: CoroutineContext
        get() = SupervisorJob() + Dispatchers.Main

    private var rawContact: RawContactEntity? = null

    /**
     * Sets the RawContact account shown and managed by this view to the given [rawContact] and uses
     * the given [contacts] API to perform operations on it.
     */
    fun setRawContact(rawContact: RawContactEntity?, contacts: Contacts) {
        this.rawContact = rawContact
        setAccount(contacts)
    }

    private fun setAccount(contacts: Contacts) = launch {
        val account = rawContact?.let {
            if (it is ExistingRawContactEntity) {
                contacts.accounts(it.isProfile)
                    .queryWithPermission()
                    .associatedWith(it)
                    .findWithContext()
                    .firstOrNull()
            } else {
                // TODO Support selecting account when creating new RawContact
                null
            }
        }

        text = if (account == null) {
            """
                Local account (device only)
                Not synced
            """
        } else {
            """
                Account Name: ${account.name}
                Account Type: ${account.type}
            """
        }.trimIndent()
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        cancel()
    }
}