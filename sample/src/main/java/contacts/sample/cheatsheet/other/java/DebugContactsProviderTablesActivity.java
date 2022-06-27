package contacts.sample.cheatsheet.other.java;

import android.app.Activity;

import contacts.debug.*;

public class DebugContactsProviderTablesActivity extends Activity {

    void debugContactsProviderTables() {
        LogGroupsTableKt.logGroupsTable(this);
        LogAggregationExceptionsTableKt.logAggregationExceptionsTable(this);
        LogProfileKt.logProfile(this);
        LogContactsTableKt.logContactsTable(this);
        LogRawContactsTableKt.logRawContactsTable(this);
        LogDataTableKt.logDataTable(this);
    }
}