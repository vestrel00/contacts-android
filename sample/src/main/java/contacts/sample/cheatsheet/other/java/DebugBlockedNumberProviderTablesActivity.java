package contacts.sample.cheatsheet.other.java;

import android.app.Activity;

import contacts.debug.LogBlockedNumbersKt;

public class DebugBlockedNumberProviderTablesActivity extends Activity {

    void debugBlockedNumberProviderTables() {
        LogBlockedNumbersKt.logBlockedNumbersTable(this);
    }
}