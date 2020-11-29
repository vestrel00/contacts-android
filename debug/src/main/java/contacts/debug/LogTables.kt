package contacts.debug

import android.content.Context

fun Context.logContactsProviderTables() {
    logGroupsTable()
    logProfile()
    logContactsTable()
    logRawContactsTable()
    logAggregationExceptions()
    logDataTable()
}