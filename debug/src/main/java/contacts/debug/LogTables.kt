package contacts.debug

import android.content.Context

fun Context.logContactsProviderTables() {
    logGroupsTable()
    logAggregationExceptionsTable()
    logProfile()
    logContactsTable()
    logRawContactsTable()
    logDataTable()
}