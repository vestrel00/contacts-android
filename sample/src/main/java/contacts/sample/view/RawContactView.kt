package contacts.sample.view

import android.content.Context
import android.content.Intent
import android.util.AttributeSet
import android.widget.LinearLayout
import contacts.entities.MutableName
import contacts.entities.MutableRawContact
import contacts.sample.R
import contacts.ui.view.EmailsView
import contacts.ui.view.NameView
import contacts.ui.view.PhonesView
import contacts.util.setName

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
) : LinearLayout(context, attributeSet, defStyleAttr) {

    /**
     * The RawContact that is shown in this view. Setting this will automatically update the views.
     * Any modifications in the views will also be made to the this.
     */
    var rawContact: MutableRawContact = MutableRawContact()
        set(value) {
            field = value

            setRawContactView()
        }

    // Not using any view binding libraries or plugins just for this.
    private val accountView: AccountView
    private val photoThumbnailView: RawContactPhotoThumbnailView
    private val nameView: NameView
    private val phonesView: PhonesView
    private val emailsView: EmailsView

    init {
        orientation = VERTICAL
        inflate(context, R.layout.view_raw_contact, this)

        accountView = findViewById(R.id.account)
        photoThumbnailView = findViewById(R.id.photoThumbnail)
        nameView = findViewById(R.id.name)
        phonesView = findViewById(R.id.phones)
        emailsView = findViewById(R.id.emails)
    }

    fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        photoThumbnailView.onActivityResult(requestCode, resultCode, data)
    }

    suspend fun savePhoto(): Boolean = photoThumbnailView.savePhoto()

    private fun setRawContactView() {
        setPhotoThumbnailView()
        setAccountView()
        setNameView()
        setPhonesView()
        setEmailsView()
        // TODO
    }

    private fun setAccountView() {
        accountView.rawContact = rawContact
    }

    private fun setPhotoThumbnailView() {
        photoThumbnailView.rawContact = rawContact
    }

    private fun setNameView() {
        nameView.name = rawContact.name ?: MutableName().apply(rawContact::setName)
    }

    private fun setPhonesView() {
        phonesView.dataList = rawContact.phones
    }

    private fun setEmailsView() {
        emailsView.dataList = rawContact.emails
    }
}