package contacts.sample.view

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.widget.LinearLayout
import contacts.Contacts
import contacts.async.commitWithContext
import contacts.entities.MutableContact
import contacts.entities.MutableRawContact
import contacts.permissions.updateWithPermission
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlin.coroutines.CoroutineContext


/**
 * A (vertical) [LinearLayout] that displays a [MutableContact] and handles the modifications to the
 * given [contact]. Each of the RawContact is displayed in a [RawContactView].
 *
 * Setting the [contact] will automatically update the views. Any modifications in the views will
 * also be made to the [contact].
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
class ContactView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAttr), CoroutineScope {

    /**
     * The Contact that is shown in this view. Setting this will automatically update the views. Any
     * modifications in the views will also be made to the this.
     */
    var contact: MutableContact? = null
        set(value) {
            field = value

            setContactView()
        }

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val job: Job = SupervisorJob()

    init {
        orientation = VERTICAL
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        for (index in 0 until childCount) {
            val rawContactView = getChildAt(index) as RawContactView
            rawContactView.onActivityResult(requestCode, resultCode, data)
        }
    }

    /**
     * Saves the [contact] and all of the associated RawContacts.
     *
     * Returns true if the save succeeded regardless of whether the RawContact photos save succeeded
     * of not.
     */
    suspend fun saveContact(contacts: Contacts = Contacts(context)): Boolean {
        val contact = contact ?: return false

        // Save photos first so that the (Raw)Contacts does not get deleted if it only has a photo.
        // Blank (Raw)Contacts are by default deleted in updates.
        for (index in 0 until childCount) {
            val rawContactView = getChildAt(index) as RawContactView
            rawContactView.savePhoto()
        }

        // Save changes. Ignore if photos save succeeded or not :D
        val contactSaveResult = contacts.updateWithPermission()
            // This is implicitly true by default. We are just being explicitly verbose here.
            .deleteBlanks(true)
            .contacts(contact)
            .commitWithContext()

        return contactSaveResult.isSuccessful
    }

    private fun setContactView() {
        removeAllViews()

        contact?.rawContacts?.forEach { rawContact ->
            addRawContactView(rawContact)
        }
    }

    private fun addRawContactView(rawContact: MutableRawContact) {
        val rawContactView = RawContactView(context).also {
            it.rawContact = rawContact
        }
        addView(rawContactView)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        job.cancel()
    }
}