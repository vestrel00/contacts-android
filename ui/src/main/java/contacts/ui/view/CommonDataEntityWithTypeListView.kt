package contacts.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import contacts.entities.CommonDataEntity
import contacts.entities.MutableCommonDataEntityWithType
import contacts.entities.removeAll
import contacts.ui.entities.CommonDataEntityFactory
import contacts.ui.entities.CommonDataEntityTypeFactory

/**
 * A (vertical) [LinearLayout] that displays a list of [MutableCommonDataEntityWithType] and handles
 * the modifications to the given mutable list. Each of the mutable entity in the list is displayed
 * in a [CommonDataEntityWithTypeView].
 *
 * Setting the [dataList] will automatically update the views. Any modifications in the views will
 * also be made to the [dataList].
 *
 * This is not an actual ListView. It **does not implement any optimizations** like view recycling.
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
 */
open class CommonDataEntityWithTypeListView
<T : CommonDataEntity.Type, K : MutableCommonDataEntityWithType<T>>
@JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0,
    private var dataFieldInputType: Int? = null,
    private var dataFieldHintResId: Int? = null,
    private var dataFactory: CommonDataEntityFactory<K>? = null,
    private var dataTypeFactory: CommonDataEntityTypeFactory<K, T>? = null,
    private var defaultUnderlyingDataTypes: List<T>? = null
) : LinearLayout(context, attributeSet, defStyleAttr) {

    /**
     * The list of data that is shown in this view. Setting this will automatically update the
     * views. Any modifications in the views will also be made to the this.
     */
    var dataList: MutableList<K> = mutableListOf()
        set(value) {
            field = value

            setDataViews()
        }

    /**
     * A [CommonDataEntityWithTypeView] with empty data. Used to add a new data to the [dataList].
     */
    private lateinit var emptyDataView: CommonDataEntityWithTypeView<T, K>

    init {
        orientation = VERTICAL
    }

    private fun setDataViews() {
        removeAllViews()

        dataList.forEach(::addDataView)

        addEmptyDataView()
    }

    private fun addDataView(data: K): CommonDataEntityWithTypeView<T, K> {
        // FIXME? Extract this into a factory?
        val dataView = CommonDataEntityWithTypeView(
            context,
            dataFieldInputType = dataFieldInputType,
            dataFieldHintResId = dataFieldHintResId,
            dataTypeFactory = dataTypeFactory
        ).also {
            it.data = data
            it.setEventListener(DataViewEventListener(it))
        }

        addView(dataView)

        return dataView
    }

    private fun addEmptyDataView() {
        val dataFactory = dataFactory ?: return
        val defaultUnderlyingDataTypes = defaultUnderlyingDataTypes ?: return

        // In the native Contacts app, using phones as an example, the new empty phone that is added
        // has a phone type of either mobile, home, work, main, or other in that other (depends on
        // SDK version); which ever has not yet been added. If all of those phone types already
        // exist, it defaults to other (the last on the list).
        val existingUnderlyingDataTypes = dataList.map { it.type }
        val underlyingDataType = defaultUnderlyingDataTypes
            .minus(existingUnderlyingDataTypes)
            .firstOrNull()
            ?: defaultUnderlyingDataTypes.last()

        emptyDataView = addDataView(dataFactory.create().apply { type = underlyingDataType })
        dataList.add(emptyDataView.data ?: throw IllegalStateException("View data is null"))
    }

    private fun removeDataView(dataView: CommonDataEntityWithTypeView<T, K>) {
        // There may be duplicate data. Therefore, we need to remove the exact data instance.
        // Thus, we remove the data by reference equality instead of by content/structure equality.
        dataList.removeAll(
            emptyDataView.data ?: throw IllegalStateException("View data is null"),
            byReference = true
        )
        removeView(dataView)
    }

    private inner class DataViewEventListener(
        private val dataView: CommonDataEntityWithTypeView<T, K>
    ) : CommonDataEntityWithTypeView.EventListener {

        override fun onDataDeleteButtonClicked() {
            removeDataView(dataView)
        }

        override fun onDataCleared() {
            removeDataView(emptyDataView)
            emptyDataView = dataView
        }

        override fun onDataBegin() {
            addEmptyDataView()
        }
    }
}