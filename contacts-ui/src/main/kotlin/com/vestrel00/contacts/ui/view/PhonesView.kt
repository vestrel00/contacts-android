package com.vestrel00.contacts.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import com.vestrel00.contacts.entities.MutableContact
import com.vestrel00.contacts.entities.MutablePhone
import com.vestrel00.contacts.util.addPhone
import com.vestrel00.contacts.util.phones
import com.vestrel00.contacts.util.removePhone

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
        contact?.let {
            emptyPhoneView = addPhone(MutablePhone())
            it.addPhone(emptyPhoneView.phone)
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