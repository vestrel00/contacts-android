package contacts.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.*
import contacts.entities.CommonDataEntity
import contacts.entities.MutableCommonDataEntityWithType
import contacts.ui.R
import contacts.ui.dialog.CustomLabelInputDialog
import contacts.ui.entities.CommonDataEntityType
import contacts.ui.entities.CommonDataEntityTypeFactory
import contacts.ui.text.AbstractTextWatcher

/**
 * A [RelativeLayout] that displays a [MutableCommonDataEntityWithType] [K] that has a
 * [CommonDataEntityType] and handles the modifications to it.
 *
 * Setting the [data] will automatically update the views and vice versa.
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
class CommonDataEntityWithTypeView
<T : CommonDataEntity.Type, K : MutableCommonDataEntityWithType<T>>
@JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    initialData: K? = null,
    dataFieldInputType: Int? = null,
    dataFieldHintResId: Int? = null,
    private val dataTypeFactory: CommonDataEntityTypeFactory<K, T>? = null,
) : RelativeLayout(context, attributeSet, defStyleAttr) {

    /**
     * The data entity that is shown in this view. Changing this will automatically update the views
     * and vice versa.
     */
    var data: K? = initialData
        set(value) {
            field = value

            setDataField()
            setDataTypeField()
            setDataDeleteButton()
        }

    private var eventListener: EventListener? = null

    private var selectedType: CommonDataEntityType<T>? = null
        set(value) {
            field = value

            value?.let {
                data?.type = it.type
                data?.label = if (it.type.isCustomType) it.typeLabel else null
            }
        }

    private val dataField: EditText
    private val dataTypeField: Spinner
    private val dataDeleteButton: View

    init {
        inflate(context, R.layout.view_mutable_common_data_entity_with_type, this)

        dataField = findViewById(R.id.dataField)
        dataTypeField = findViewById(R.id.dataTypeField)
        dataDeleteButton = findViewById(R.id.dataDeleteButton)

        dataFieldHintResId?.let(dataField::setHint)
        dataFieldInputType?.let(dataField::setInputType)

        dataField.apply {
            setOnFocusChangeListener { _, _ ->
                setDataTypeFieldVisibility()
            }
            addTextChangedListener(DataFieldTextChangeListener())
        }

        dataTypeField.apply {
            adapter = dataTypesAdapter
            onItemSelectedListener = OnDataTypeSelectedListener()
        }

        dataDeleteButton.setOnClickListener {
            eventListener?.onDataDeleteButtonClicked()
        }
    }

    private val dataTypesAdapter: ArrayAdapter<CommonDataEntityType<T>> =
        ArrayAdapter(context, android.R.layout.simple_list_item_1)

    fun setEventListener(eventListener: EventListener?) {
        this.eventListener = eventListener
    }

    private fun setDataField() {
        dataField.setText(data?.primaryValue)
    }

    private fun setDataTypeField() {
        val data = data
        if (dataTypeFactory == null || data == null) {
            return
        }

        val dataType = dataTypeFactory.from(resources, data)

        dataTypesAdapter.apply {
            setNotifyOnChange(false)
            clear()

            // If this field's data type is a custom type whose label has been created by the user,
            // add it as first entry.
            if (dataType.isUserCustomType) {
                add(dataType)
            }

            // Add all system data types.
            addAll(dataTypeFactory.systemTypes(resources))

            notifyDataSetChanged()
        }

        // Set the selected data type to this field.
        dataTypeField.setSelection(dataTypesAdapter.getPosition(dataType))

        setDataTypeFieldVisibility()
    }

    // Hide the dataTypeField if the dataField is empty and not focused.
    private fun setDataTypeFieldVisibility() {
        dataTypeField.visibility =
            if (dataField.text.isEmpty() && !dataField.hasFocus()) {
                View.GONE
            } else {
                View.VISIBLE
            }
    }

    private fun setDataDeleteButton() {
        setDataDeleteButtonVisibility()
    }

    private fun setDataDeleteButtonVisibility() {
        dataDeleteButton.visibility = if (dataField.text.isNullOrEmpty()) {
            View.INVISIBLE
        } else {
            View.VISIBLE
        }

    }

    private fun showCreateUserCustomTypeInputPrompt() {
        CustomLabelInputDialog(context)
            .show(R.string.contacts_ui_custom_label_input_dialog_title,
                onLabelEntered = { label ->
                    replaceUserCustomTypeWithLabel(label)
                }, onCancelled = {
                    // Revert the selection to the current selected type.
                    dataTypeField.setSelection(dataTypesAdapter.getPosition(selectedType))
                })
    }

    private fun replaceUserCustomTypeWithLabel(label: String) {
        val userCustomType = dataTypeFactory?.userCustomType(label) ?: return

        dataTypesAdapter.apply {
            setNotifyOnChange(false)

            selectedType?.let {
                if (it.isUserCustomType) {
                    // Remove the currently selected type if it is a user custom type.
                    remove(it)
                }
            }

            insert(userCustomType, USER_CUSTOM_DATA_TYPE_INDEX)
            notifyDataSetChanged()
        }

        // Set the selected data type to this new custom data type.
        dataTypeField.setSelection(USER_CUSTOM_DATA_TYPE_INDEX)
    }

    private inner class DataFieldTextChangeListener : AbstractTextWatcher {
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
            data?.primaryValue = s?.toString()

            if (s.isNullOrEmpty()) {
                eventListener?.onDataCleared()
            }

            if (start == 0 && count > 0) {
                eventListener?.onDataBegin()
            }

            setDataDeleteButtonVisibility()
        }
    }

    private inner class OnDataTypeSelectedListener : AdapterView.OnItemSelectedListener {

        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
            val dataType = dataTypesAdapter.getItem(position) ?: return
            if (dataType.isUserCustomType) {
                // Existing user custom type is selected. Nothing to do here.
                selectedType = dataType
            } else if (dataType.isSystemCustomType) {
                // If generic custom type is selected, show an input prompt for creating a new
                // user custom type.
                showCreateUserCustomTypeInputPrompt()
            } else {
                // A generic, non custom type is selected. Set the data type to this.
                selectedType = dataType
                //  Then, remove user custom type, if exist.
                dataTypesAdapter.getItem(USER_CUSTOM_DATA_TYPE_INDEX)?.let { maybeUserCustomType ->
                    if (maybeUserCustomType.isUserCustomType) {
                        dataTypesAdapter.remove(maybeUserCustomType)
                    }
                }
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }

    interface EventListener {
        /**
         * Invoked when the delete button is clicked.
         */
        fun onDataDeleteButtonClicked()

        /**
         * Invoked when the data field is cleared.
         */
        fun onDataCleared()

        /**
         * Invoked when the a piece of the data field is entered from a blank state.
         */
        fun onDataBegin()
    }
}

/**
 * The index of the user custom type. It should be the first on the list just like in the native
 * Contacts app.
 */
private const val USER_CUSTOM_DATA_TYPE_INDEX = 0