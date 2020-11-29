package contacts.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.vestrel00.contacts.entities.MutableContact
import com.vestrel00.contacts.entities.MutablePhone
import com.vestrel00.contacts.entities.Phone
import com.vestrel00.contacts.util.addPhone
import com.vestrel00.contacts.util.phones
import com.vestrel00.contacts.util.removePhone

/**
 * A (vertical) [LinearLayout] that displays the [MutablePhone]s of the [contact] and handles its
 * addition, removal, and modification.
 *
 * Each [MutablePhone] is displayed using a [PhoneView].
 *
 * ## Note
 *
 * This is a very simple view that is not styled or made to look good. Consumers of the library may
 * choose to use this as is or simply as a reference on how to implement this part of native
 * Contacts app.
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
 */
class PhonesView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAttr) {

    private lateinit var emptyPhoneView: PhoneView

    var contact: MutableContact? = null
        set(value) {
            field = value

            setPhonesViews()
        }

    private fun setPhonesViews() {
        removeAllViews()

        contact?.phones()?.forEach { phone ->
            addPhone(phone)
        }

        addEmptyPhoneView()
    }

    private fun addPhone(phone: MutablePhone): PhoneView {
        val phoneView = PhoneView(context).apply {
            this.phone = phone
            onPhoneDeleteButtonClicked = ::onPhoneDeleteButtonClicked
            onPhoneNumberCleared = ::onPhoneNumberCleared
            onPhoneNumberBegin = ::onPhoneNumberBegin
        }

        addView(phoneView)

        return phoneView
    }

    private fun addEmptyPhoneView() {
        contact?.let { contact ->
            // In the native Contacts app, the new empty phone that is added has a phone type of
            // either mobile, home, work, or other in that other; which ever has not yet been added.
            // If all those phone types already exist, it defaults to other.
            val existingPhoneTypes = contact.phones().map { it.type }
            val phoneType = DEFAULT_PHONE_TYPES.minus(existingPhoneTypes).firstOrNull()
                ?: DEFAULT_PHONE_TYPES.last()

            emptyPhoneView = addPhone(MutablePhone().apply { type = phoneType })
            contact.addPhone(emptyPhoneView.phone)
        }
    }

    private fun onPhoneNumberCleared(phoneView: PhoneView) {
        removePhoneView(emptyPhoneView)
        emptyPhoneView = phoneView
    }

    private fun onPhoneNumberBegin() {
        addEmptyPhoneView()
    }

    private fun onPhoneDeleteButtonClicked(phoneView: PhoneView) {
        removePhoneView(phoneView)
    }

    private fun removePhoneView(phoneView: PhoneView) {
        // There may be duplicate phones. Therefore, we need to remove the exact phone instance.
        // Thus, we remove the phone by reference equality instead of by content/structure equality.
        contact?.removePhone(phoneView.phone, byReference = true)
        removeView(phoneView)
    }
}

private val DEFAULT_PHONE_TYPES = sequenceOf(
    Phone.Type.MOBILE, Phone.Type.HOME, Phone.Type.WORK, Phone.Type.OTHER
)