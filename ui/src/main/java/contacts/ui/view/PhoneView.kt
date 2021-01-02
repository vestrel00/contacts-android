package contacts.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.*
import contacts.entities.MutablePhone
import contacts.entities.Phone
import contacts.ui.R
import contacts.ui.dialog.CustomLabelInputDialog
import contacts.ui.text.AbstractTextWatcher
import contacts.ui.util.PhoneType

/**
 * A [RelativeLayout] that displays a [MutablePhone] and handles the modifications to the given
 * [phone].
 *
 * This is used in the [PhonesView].
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
class PhoneView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attributeSet, defStyleAttr) {

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

    // Not using any view binding libraries or plugins just for this.
    private val phoneNumberField: EditText
    private val phoneTypeField: Spinner
    private val phoneDeleteButton: View

    private val typesAdapter: ArrayAdapter<PhoneType>

    private var selectedPhoneType: PhoneType? = null
        set(value) {
            field = value

            value?.let {
                phone.type = it.type
                phone.label = if (it.type == Phone.Type.CUSTOM) it.typeLabel else null
            }
        }


    init {
        inflate(context, R.layout.view_phone, this)

        phoneNumberField = findViewById(R.id.phoneNumberField)
        phoneTypeField = findViewById(R.id.phoneTypeField)
        phoneDeleteButton = findViewById(R.id.phoneDeleteButton)

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
        val phoneType = PhoneType.from(resources, phone)

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
            val phoneType = typesAdapter.getItem(position) ?: return
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