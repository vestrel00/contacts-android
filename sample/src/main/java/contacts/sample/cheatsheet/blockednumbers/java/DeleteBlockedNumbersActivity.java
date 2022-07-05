package contacts.sample.cheatsheet.blockednumbers.java;

import static contacts.core.WhereKt.contains;

import android.app.Activity;

import contacts.core.BlockedNumbersFields;
import contacts.core.ContactsFactory;
import contacts.core.blockednumbers.BlockedNumbersDelete;
import contacts.core.entities.BlockedNumber;

public class DeleteBlockedNumbersActivity extends Activity {

    BlockedNumbersDelete.Result deleteBlockedNumber(BlockedNumber blockedNumber) {
        return ContactsFactory.create(this)
                .blockedNumbers()
                .delete()
                .blockedNumbers(blockedNumber)
                .commit();
    }

    BlockedNumbersDelete.Result deleteBlockedNumberWithId(Long blockedNumberId) {
        return ContactsFactory.create(this)
                .blockedNumbers()
                .delete()
                .blockedNumbersWithId(blockedNumberId)
                .commit();
    }

    BlockedNumbersDelete.Result deleteBlockedNumbersContaining555() {
        return ContactsFactory.create(this)
                .blockedNumbers()
                .delete()
                .blockedNumbersWhere(contains(BlockedNumbersFields.Number, "555"))
                .commit();
    }
}