package com.vestrel00.contacts.debug

import android.content.Context

fun Context.logContactsProviderTables() {
    logGroupsTable()
    logContactsTable()
    logRawContactsTable()
    logAggregationExceptions()
    logDataTable()
}