package contacts.sample.cheatsheet.other.kotlin

import android.app.Activity
import contacts.debug.*

class DebugContactsProviderTablesActivity : Activity() {

    fun debugContactsProviderTables() {
        logGroupsTable()
        logAggregationExceptionsTable()
        logProfile()
        logContactsTable()
        logRawContactsTable()
        logDataTable()
    }
}