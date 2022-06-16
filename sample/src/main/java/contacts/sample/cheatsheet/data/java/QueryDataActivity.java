package contacts.sample.cheatsheet.data.java;

import static contacts.core.OrderByKt.desc;
import static contacts.core.WhereKt.*;

import android.accounts.Account;
import android.app.Activity;

import java.util.List;

import contacts.core.ContactsFactory;
import contacts.core.Fields;
import contacts.core.entities.*;

public class QueryDataActivity extends Activity {

    List<Email> getAllEmails() {
        return ContactsFactory.create(this).data().query().emails().find();
    }

    List<Email> getEmailsForAccount(Account account) {
        return ContactsFactory.create(this).data().query().emails().accounts(account).find();
    }

    List<Email> getGmailEmailsInDescendingOrder() {
        return ContactsFactory.create(this)
                .data()
                .query()
                .emails()
                .where(endsWith(Fields.Email.Address, "@gmail.com"))
                .orderBy(desc(Fields.Email.Address, true))
                .find();
    }

    List<Phone> getWorkPhones() {
        return ContactsFactory.create(this)
                .data()
                .query()
                .phones()
                .where(equalTo(Fields.Phone.Type, PhoneEntity.Type.WORK))
                .find();
    }

    List<Relation> getUpTo10Mothers() {
        return ContactsFactory.create(this)
                .data()
                .query()
                .relations()
                .where(equalTo(Fields.Relation.Type, RelationEntity.Type.MOTHER))
                .limit(10)
                .find();
    }

    Event getContactBirthday(Long contactId) {
        return ContactsFactory.create(this)
                .data()
                .query()
                .events()
                .where(
                        and(
                                equalTo(Fields.Contact.Id, contactId),
                                equalTo(Fields.Event.Type, EventEntity.Type.BIRTHDAY)
                        )
                )
                .find()
                .get(0);
    }
}