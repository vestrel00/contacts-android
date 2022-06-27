package contacts.sample.cheatsheet.other.java;

import android.app.Activity;

import contacts.debug.LogSimContactsTableKt;

public class DebugSimContactsTablesActivity extends Activity {

    void debugSimContactsTables() {
        LogSimContactsTableKt.logSimContactsTable(this);
    }
}