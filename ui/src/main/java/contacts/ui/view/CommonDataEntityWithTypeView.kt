package contacts.ui.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.RelativeLayout
import android.widget.Spinner
import contacts.core.entities.CommonDataEntity
import contacts.core.entities.MutableCommonDataEntityWithType
import contacts.ui.R
import contacts.ui.entities.CommonDataEntityType
import contacts.ui.entities.CommonDataEntityTypeFactory
import contacts.ui.util.CustomLabelInputDialog

/**
 * A [RelativeLayout] that displays a [MutableCommonDataEntityWithType] [E] that has a
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
open class CommonDataEntityWithTypeView
<T : CommonDataEntity.Type, E : MutableCommonDataEntityWithType<T>>
@JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    dataFieldInputType: Int? = null,
    dataFieldHintResId: Int? = null,
    dataFieldIsFocusable: Boolean = true,
    private val dataTypeFactory: CommonDataEntityTypeFactory<E, T>? = null,
) : CommonDataEntityView<E>(
    context, attributeSet, defStyleAttr,
    layoutRes = R.layout.view_common_data_entity_with_type,
    dataFieldInputType = dataFieldInputType,
    dataFieldHintResId = dataFieldHintResId,
    dataFieldIsFocusable = dataFieldIsFocusable
) {

    private var selectedType: CommonDataEntityType<T>? = null
        set(value) {
            field = value

            value?.let {
                data?.type = it.type
                data?.label = if (it.type.isCustomType) it.typeLabel else null
            }
        }

    private val dataTypeField: Spinner = findViewById(R.id.dataTypeField)
    private val dataTypesAdapter: ArrayAdapter<CommonDataEntityType<T>>

    init {
        dataTypeField.apply {
            dataTypesAdapter = ArrayAdapter(context, android.R.layout.simple_list_item_1)
            adapter = dataTypesAdapter
            onItemSelectedListener = OnDataTypeSelectedListener()
        }
    }

    override fun onDataSet() {
        super.onDataSet()
        setDataTypeField()
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
                        // There is one less item so the indices shifter by 1. We could just do
                        // position - 1 but let's re-evaluate the position to be safe.
                        dataTypeField.setSelection(dataTypesAdapter.getPosition(selectedType))
                    }
                }
            }
        }

        override fun onNothingSelected(parent: AdapterView<*>?) {
        }
    }

    interface Factory<T : CommonDataEntity.Type, E : MutableCommonDataEntityWithType<T>> :
        CommonDataEntityView.Factory<E, CommonDataEntityWithTypeView<T, E>>
}

/**
 * The index of the user custom type. It should be the first on the list just like in the native
 * Contacts app.
 */
private const val USER_CUSTOM_DATA_TYPE_INDEX = 0