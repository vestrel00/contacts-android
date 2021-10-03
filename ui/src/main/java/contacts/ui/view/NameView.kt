package contacts.ui.view

import android.content.Context
import android.text.Editable
import android.util.AttributeSet
import android.widget.EditText
import android.widget.LinearLayout
import contacts.entities.MutableName
import contacts.ui.R
import contacts.ui.text.AbstractTextWatcher

/**
 * A (vertical) [LinearLayout] that displays a [MutableName] and handles the modifications to the
 * given [data].
 *
 * Setting the [data] will automatically update the views. Any modifications in the views will also
 * be made to the [data].
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
class NameView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attributeSet, defStyleAttr) {

    /**
     * The name that is shown in this view. Setting this will automatically update the views. Any
     * modifications in the views will also be made to the this.
     */
    var data = MutableName()
        set(value) {
            field = value

            setNameFields()
        }

    // Not using any view binding libraries or plugins just for this.
    private val namePrefixField: EditText
    private val firstNameField: EditText
    private val middleNameField: EditText
    private val lastNameField: EditText
    private val nameSuffixField: EditText

    init {
        orientation = VERTICAL
        inflate(context, R.layout.view_name, this)

        namePrefixField = findViewById(R.id.namePrefixField)
        firstNameField = findViewById(R.id.firstNameField)
        middleNameField = findViewById(R.id.middleNameField)
        lastNameField = findViewById(R.id.lastNameField)
        nameSuffixField = findViewById(R.id.nameSuffixField)

        setNameFieldsListeners()
    }

    private fun setNameFieldsListeners() {
        namePrefixField.addTextChangedListener(object : AbstractTextWatcher {
            override fun afterTextChanged(s: Editable?) {
                data.prefix = s?.toString()
            }
        })

        firstNameField.addTextChangedListener(object : AbstractTextWatcher {
            override fun afterTextChanged(s: Editable?) {
                data.givenName = s?.toString()
            }
        })

        middleNameField.addTextChangedListener(object : AbstractTextWatcher {
            override fun afterTextChanged(s: Editable?) {
                data.middleName = s?.toString()
            }
        })

        lastNameField.addTextChangedListener(object : AbstractTextWatcher {
            override fun afterTextChanged(s: Editable?) {
                data.familyName = s?.toString()
            }
        })

        nameSuffixField.addTextChangedListener(object : AbstractTextWatcher {
            override fun afterTextChanged(s: Editable?) {
                data.suffix = s?.toString()
            }
        })
    }

    private fun setNameFields() {
        namePrefixField.setText(data.prefix)
        firstNameField.setText(data.givenName)
        middleNameField.setText(data.middleName)
        lastNameField.setText(data.familyName)
        nameSuffixField.setText(data.suffix)
    }
}