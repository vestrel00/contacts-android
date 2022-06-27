package contacts.sample.cheatsheet.other.kotlin

import android.app.Activity
import contacts.debug.logSimContactsTable

class DebugSimContactsTablesActivity : Activity() {

    fun debugSimContactsTables() {
        logSimContactsTable()
    }
}