package contacts.ui.view

import android.app.DatePickerDialog
import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import android.widget.DatePicker
import contacts.core.entities.Event
import contacts.core.entities.MutableEvent
import contacts.ui.R
import contacts.ui.entities.EventFactory
import contacts.ui.entities.EventTypeFactory
import java.util.*

/**
 * A [CommonDataEntityWithTypeListView] for [MutableEvent]s.
 */
class EventsView @JvmOverloads constructor(
    context: Context,
    attributeSet: AttributeSet? = null,
    defStyleAttr: Int = 0
) : CommonDataEntityWithTypeListView<Event.Type, MutableEvent>(
    context, attributeSet, defStyleAttr,
    dataFactory = EventFactory,
    dataViewFactory = EventsViewFactory,
    defaultUnderlyingDataTypes = Event.Type.values().filter { !it.isCustomType }
)

private class EventView(context: Context) : CommonDataEntityWithTypeView<Event.Type, MutableEvent>(
    context,
    dataFieldInputType = InputType.TYPE_NULL,
    dataFieldHintResId = R.string.contacts_ui_event_hint,
    dataFieldIsFocusable = false,
    dataTypeFactory = EventTypeFactory
), DatePickerDialog.OnDateSetListener {

    override fun onDataFieldClicked() {
        showDatePickerDialog()
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        val date = Calendar.getInstance().let {
            it.set(year, month, dayOfMonth)
            it.time
        }
        data?.date = date
        setDataField()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar[Calendar.YEAR]
        val monthOfYear = calendar[Calendar.MONTH]
        val dayOfMonth = calendar[Calendar.DAY_OF_MONTH]

        DatePickerDialog(context, this, year, monthOfYear, dayOfMonth).show()
    }
}

private object EventsViewFactory :
    CommonDataEntityWithTypeView.Factory<Event.Type, MutableEvent> {
    override fun create(context: Context): CommonDataEntityWithTypeView<Event.Type, MutableEvent> =
        EventView(context)
}