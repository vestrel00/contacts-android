package contacts.sample.cheatsheet.data.java;

import static contacts.core.WhereKt.equalTo;

import android.app.Activity;

import java.util.ArrayList;
import java.util.List;

import contacts.core.ContactsFactory;
import contacts.core.Fields;
import contacts.core.data.DataDelete;
import contacts.core.entities.*;

public class DeleteActivity extends Activity {

    DataDelete.Result deleteData(ExistingDataEntity data) {
        return ContactsFactory.create(this).data().delete().data(data).commit();
    }

    DataDelete.Result deleteEmailsAndPhones(List<Email> emails, List<Phone> phones) {
        List<ExistingDataEntity> dataSet = new ArrayList<>();
        dataSet.addAll(emails);
        dataSet.addAll(phones);

        return ContactsFactory.create(this)
                .data()
                .delete()
                .data(dataSet)
                .commit();
    }

    DataDelete.Result deleteDataWithId(Long dataId) {
        return ContactsFactory.create(this).data().delete().dataWithId(dataId).commit();
    }

    DataDelete.Result deleteAllWorkEmails() {
        return ContactsFactory.create(this)
                .data()
                .delete()
                .dataWhere(
                        equalTo(Fields.Email.Type, EmailEntity.Type.WORK)
                )
                .commit();
    }
}