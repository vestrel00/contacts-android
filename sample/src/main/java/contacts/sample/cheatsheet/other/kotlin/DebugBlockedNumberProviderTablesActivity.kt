package contacts.sample.cheatsheet.other.kotlin

import android.app.Activity
import contacts.debug.logBlockedNumbersTable

class DebugBlockedNumberProviderTablesActivity : Activity() {

    fun debugBlockedNumberProviderTables() {
        logBlockedNumbersTable()
    }
}