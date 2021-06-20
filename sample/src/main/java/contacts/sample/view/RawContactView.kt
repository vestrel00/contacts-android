package contacts.sample.view

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.widget.LinearLayout
import android.widget.TextView
import contacts.accounts.Accounts
import contacts.async.accounts.accountForWithContext
import contacts.entities.MutableName
import contacts.entities.MutableRawContact
import contacts.sample.R
import contacts.ui.view.NameView
import contacts.ui.view.PhonesView
import contacts.util.setName
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * A (vertical) [LinearLayout] that displays a [MutableRawContact] and handles the modifications to
 * the given [rawContact].
 *
 * Setting the [rawContact] will automatically update the views. Any modifications in the views will
 * also be made to the [rawContact].
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
class RawContactView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAttr), CoroutineScope {

    /**
     * The RawContact that is shown in this view. Setting this will automatically update the views.
     * Any modifications in the views will also be made to the this.
     */
    var rawContact: MutableRawContact = MutableRawContact()
        set(value) {
            field = value

            setRawContactView()
        }

    override val coroutineContext: CoroutineContext
        get() = job + Dispatchers.Main

    private val job: Job = SupervisorJob()

    // Not using any view binding libraries or plugins just for this.
    private val rawContactInfoView: TextView
    private val photoView: PhotoView
    private val nameView: NameView
    private val phonesView: PhonesView

    init {
        orientation = VERTICAL
        inflate(context, R.layout.view_raw_contact, this)

        rawContactInfoView = findViewById(R.id.rawContactInfo)
        photoView = findViewById(R.id.photoView)
        nameView = findViewById(R.id.nameView)
        phonesView = findViewById(R.id.phonesView)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        photoView.onActivityResult(requestCode, resultCode, data)
    }

    suspend fun savePhoto(): Boolean = photoView.savePhoto()

    private fun setRawContactView() {
        setRawContactInfoView()
        setPhotoView()
        setNameView()
        setPhonesView()
        // TODO
    }

    private fun setRawContactInfoView() = launch {
        val account = Accounts(context, rawContact.isProfile)
            .query()
            .accountForWithContext(rawContact)
        rawContactInfoView.text = if (account == null) {
            "Local Account"
        } else {
            """
                Account Name: ${account.name}
                Account Type: ${account.type}
            """.trimIndent()
        }
    }

    private fun setPhotoView() {
        photoView.rawContact = rawContact
    }

    private fun setNameView() {
        nameView.name = rawContact.name ?: MutableName().apply(rawContact::setName)
    }

    private fun setPhonesView() {
        phonesView.phones = rawContact.phones
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        job.cancel()
    }
}