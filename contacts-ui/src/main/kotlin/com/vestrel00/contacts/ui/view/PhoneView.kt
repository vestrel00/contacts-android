package com.vestrel00.contacts.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import com.vestrel00.contacts.entities.MutablePhone
import com.vestrel00.contacts.entities.Phone
import com.vestrel00.contacts.ui.R
import com.vestrel00.contacts.ui.dialog.CustomLabelInputDialog
import com.vestrel00.contacts.ui.text.AbstractTextWatcher
import com.vestrel00.contacts.ui.util.PhoneType
import kotlinx.android.synthetic.main.view_phone.view.*

class PhoneView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attributeSet, defStyleAttr) {

    private val typesAdapter: ArrayAdapter<PhoneType>

    private var selectedPhoneType: PhoneType? = null
        set(value) {
            field = value

            value?.let {
                phone.type = it.type
                phone.label = if (it.type == Phone.Type.CUSTOM) it.typeLabel else null
            }
        }

    var onPhoneDeleteButtonClicked: ((phoneView: PhoneView) -> Unit)? = null
    var onPhoneNumberCleared: ((phoneView: PhoneView) -> Unit)? = null
    var onPhoneNumberBegin: (() -> Unit)? = null

    var phone: MutablePhone = MutablePhone()
        set(value) {
            field = value

            setPhoneNumberField()
            setPhoneTypeField()
            setPhoneDeleteButton()
        }

    init {
        inflate(context, R.layout.view_phone, this)

        phoneNumberField.apply {
            setOnFocusChangeListener { _, _ ->
                // Hide the phoneTypeField if the phoneNumberField is empty and not focused.
                setPhoneTypeFieldVisibility()
            }
            addTextChangedListener(PhoneNumberTextChangeListener())
        }

        phoneTypeField.apply {
            typesAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_1)
            adapter = typesAdapter
            onItemSelectedListener = OnPhoneTypeSelectedListener()
        }

        phoneDeleteButton.setOnClickListener {
            onPhoneDeleteButtonClicked?.invoke(this)
        }
    }

    private fun setPhoneNumberField() {
        phoneNumberField.setText(phone.number)
    }

    private fun setPhoneTypeField() {
        val phoneType = PhoneType.from(phone, resources)

        typesAdapter.apply {
            setNotifyOnChange(false)
            clear()

            // If this field's phone type is custom, add it as first entry.
            if (phoneType.type == Phone.Type.CUSTOM) {
                add(phoneType)
            }
            // Add all phone types.
            addAll(PhoneType.all(resources))

            notifyDataSetChanged()
        }

        // Set the selected phone type to this field.
        phoneTypeField.setSelection(typesAdapter.getPosition(phoneType))

        setPhoneTypeFieldVisibility()
    }

    private fun setPhoneTypeFieldVisibility() {
        phoneTypeField.visibility =
            if (phoneNumberField.text.isEmpty() && !phoneNumberField.hasFocus()) {
                View.GONE
            } else {
                View.VISIBLE
            }
    }

    private fun setPhoneDeleteButton() {
        setPhoneDeleteButtonVisibility()
    }

    private fun setPhoneDeleteButtonVisibility() {
        phoneDeleteButton.visibility = if (phoneNumberField.text.isNullOrEmpty()) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }

    }

    private fun showCustomTypeInputPrompt() {
        CustomLabelInputDialog(context)
            .show(R.string.contact_custom_label_input_dialog_title,
                onLabelEntered = { label ->
                    val userCustomType = PhoneType.userCustomType(label)
                    replaceUserCustomType(userCustomType)
                }, onCancelled = {
                    // Revert the selection to the current phone type.
                    phoneTypeField.setSelection(typesAdapter.getPosition(selectedPhoneType))
                })
    }

    private fun replaceUserCustomType(userCustomType: PhoneType) {
        typesAdapter.apply {
            setNotifyOnChange(false)

            selectedPhoneType?.let {
                if (it.userCustomType) {
                    remove(it)
                }
            }

            insert(userCustomType, USER_CUSTOM_TYPE_INDEX)
            notifyDataSetChanged()
        }

        // Set the selected phone type to this new custom phone type.
        phoneTypeField.setSelection(USER_CUSTOM_TYPE_INDEX)
    }

    private inner class PhoneNumberTextChangeListener : AbstractTextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            phone.number = s?.toString()

            if (s.isNullOrEmpty()) {
                onPhoneNumberCleared?.invoke(this@PhoneView)
            }

            if (start == 0 && count > 0) {
                onPhoneNumberBegin?.invoke()
            }

            setPhoneDeleteButtonVisibility()
        }
    }

    private inner class OnPhoneTypeSelectedListener : AdapterView.OnItemSelectedListener {

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val phoneType = typesAdapter.getItem(position)!!
            if (phoneType.type == Phone.Type.CUSTOM) {
                // If generic custom type is selected, show an input prompt for custom type label.
                if (!phoneType.userCustomType) {
                    showCustomTypeInputPrompt()
                } else {
                    // Else user custom type is selected.
                    selectedPhoneType = phoneType
                }
            } else {
                // A generic, non custom type is selected. Set the phone type to this and remove
                // user custom type, if exist.
                typesAdapter.getItem(USER_CUSTOM_TYPE_INDEX)?.let { possiblyUserCustomType ->
                    if (possiblyUserCustomType.userCustomType) {
                        typesAdapter.remove(possiblyUserCustomType)
                    }
                }

                selectedPhoneType = phoneType
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }
}

private const val USER_CUSTOM_TYPE_INDEX = 0