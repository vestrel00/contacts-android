package contacts.debug

import android.content.Context

fun Context.logContactsProviderTables() {
    logBlockedNumbersTable()
    logGroupsTable()
    logAggregationExceptionsTable()
    logProfile()
    logContactsTable()
    logRawContactsTable()
    logDataTable()
}