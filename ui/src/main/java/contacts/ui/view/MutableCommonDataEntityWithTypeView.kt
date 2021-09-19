package contacts.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.*
import contacts.entities.CommonDataEntity
import contacts.entities.MutableCommonDataEntity
import contacts.ui.R
import contacts.ui.dialog.CustomLabelInputDialog
import contacts.ui.text.AbstractTextWatcher
import contacts.ui.util.CommonDataEntityType

/**
 * A [RelativeLayout] that displays a [MutableCommonDataEntity] [K] that has a
 * [CommonDataEntityType] [V] (which wraps a [CommonDataEntity.Type] [T]) and handles the
 * modifications to it.
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
abstract class MutableCommonDataEntityWithTypeView
<K : MutableCommonDataEntity, T : CommonDataEntity.Type, V : CommonDataEntityType<T>>
@JvmOverloads constructor(
    context: Context,
    initialData: K,
    private val dataTypeFactory: CommonDataEntityType.Factory<K, T, V>,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RelativeLayout(context, attributeSet, defStyleAttr) {

    /**
     * The data entity that is shown in this view. Changing this will automatically update the views
     * and vice versa.
     */
    var data: K = initialData
        set(value) {
            field = value

            setDataField()
            setDataTypeField()
            setDataDeleteButton()
        }

    private var eventListener: EventListener? = null

    private var selectedType: V? = null
        set(value) {
            field = value

            value?.let {
                setDataTypeAndTypeLabelValue(it.type, it.typeLabel)
            }
        }

    protected abstract val dataField: EditText
    protected abstract val dataTypeField: Spinner
    protected abstract val dataDeleteButton: View

    protected abstract val dataValue: String?

    protected abstract fun setDataValue(value: String?)
    protected abstract fun setDataTypeAndTypeLabelValue(type: T, typeLabel: String?)

    private val dataTypesAdapter: ArrayAdapter<V> =
        ArrayAdapter(context, android.R.layout.simple_list_item_1)

    fun setEventListener(eventListener: EventListener?) {
        this.eventListener = eventListener
    }

    // Must be called by subclass after immediately after creation to complete initialization.
    protected fun initViews() {
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

    private fun setDataField() {
        dataField.setText(dataValue)
    }

    private fun setDataTypeField() {
        val dataType = dataTypeFactory.from(resources, data)

        dataTypesAdapter.apply {
            setNotifyOnChange(false)
            clear()

            // If this field's data type is a custom type whose label has been created by the user,
            // add it as first entry.
            if (dataType.type.isCustomType) {
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
                    replaceUserCustomType(dataTypeFactory.userCustomType(label))
                }, onCancelled = {
                    // Revert the selection to the current selected type.
                    dataTypeField.setSelection(dataTypesAdapter.getPosition(selectedType))
                })
    }

    private fun replaceUserCustomType(userCustomType: V) {
        dataTypesAdapter.apply {
            setNotifyOnChange(false)

            selectedType?.let {
                val selectedTypePosition = getPosition(it)
                if (it.type.isCustomType && selectedTypePosition == USER_CUSTOM_DATA_TYPE_INDEX) {
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
            setDataValue(s?.toString())

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
            if (dataType.type.isCustomType) {
                if (position != USER_CUSTOM_DATA_TYPE_INDEX) {
                    // If generic custom type is selected, show an input prompt for creating a new
                    // custom type label.
                    showCreateUserCustomTypeInputPrompt()
                } else {
                    // Else existing user custom type is selected.
                    selectedType = dataType
                }
            } else {
                // A generic, non custom type is selected. Set the data type to this.
                selectedType = dataType
                //  Then, remove user custom type, if exist.
                dataTypesAdapter.getItem(USER_CUSTOM_DATA_TYPE_INDEX)?.let { maybeUserCustomType ->
                    if (maybeUserCustomType.type.isCustomType) {
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