package contacts.sample.cheatsheet.blockednumbers.java;

import static contacts.core.WhereKt.*;

import android.app.Activity;

import java.util.List;

import contacts.core.*;
import contacts.core.entities.BlockedNumber;

public class QueryBlockedNumbersActivity extends Activity {

    List<BlockedNumber> getAllBlockedNumbers() {
        return ContactsFactory.create(this).blockedNumbers().query().find();
    }

    List<BlockedNumber> getBlockedNumbersContainingNumber(String number) {
        return ContactsFactory.create(this)
                .blockedNumbers()
                .query()
                .where(
                        or(
                                contains(BlockedNumbersFields.Number, number),
                                contains(BlockedNumbersFields.NormalizedNumber, number)
                        )
                )
                .find();
    }
}