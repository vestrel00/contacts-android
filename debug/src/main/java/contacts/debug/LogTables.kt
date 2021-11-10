package contacts.debug

import android.content.Context

fun Context.logContactsProviderTables() {
    logGroupsTable()
    logAggregationExceptions()
    logProfile()
    logContactsTable()
    logRawContactsTable()
    logDataTable()
}