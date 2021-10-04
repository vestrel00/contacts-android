package contacts.ui.view

import android.content.Context
import android.util.AttributeSet
import android.widget.LinearLayout
import contacts.entities.MutableCommonDataEntity
import contacts.entities.removeAll
import contacts.ui.entities.CommonDataEntityFactory

/**
 * A (vertical) [LinearLayout] that displays a list of [MutableCommonDataEntity] and handles
 * the modifications to the given mutable list.
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
abstract class CommonDataEntityListView<K : MutableCommonDataEntity, V : CommonDataEntityView<K>>(
    context: Context,
    attributeSet: AttributeSet?,
    defStyleAttr: Int,
    private val dataFactory: CommonDataEntityFactory<K>,
    private val dataViewFactory: CommonDataEntityView.Factory<K, V>,
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
     * A view with empty data. Used to add a new data to the [dataList].
     */
    private lateinit var emptyDataView: V

    init {
        orientation = VERTICAL
    }

    protected open fun onEmptyDataCreated(data: K) {
        // optional override for subclasses
    }

    private fun setDataViews() {
        removeAllViews()

        dataList.forEach(::addDataView)

        addEmptyDataView()
    }

    private fun addDataView(data: K): V {
        val dataView = dataViewFactory.create(context).also {
            it.data = data
            it.setEventListener(DataViewEventListener(it))

            // In case this new view gets added after the parent view has been disabled, it should
            // also be disabled.
            it.setThisAndDescendantsEnabled(isEnabled)
        }

        addView(dataView)

        return dataView
    }

    private fun addEmptyDataView() {
        val emptyData = dataFactory.create().apply(::onEmptyDataCreated)
        emptyDataView = addDataView(emptyData)
        dataList.add(emptyDataView.data ?: throw IllegalStateException("View data is null"))
    }

    private fun removeDataView(dataView: V) {
        // There may be duplicate data. Therefore, we need to remove the exact data instance.
        // Thus, we remove the data by reference equality instead of by content/structure equality.
        dataList.removeAll(
            dataView.data ?: throw IllegalStateException("View data is null"),
            byReference = true
        )
        removeView(dataView)
    }

    private inner class DataViewEventListener(
        private val dataView: V
    ) : CommonDataEntityView.EventListener {

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