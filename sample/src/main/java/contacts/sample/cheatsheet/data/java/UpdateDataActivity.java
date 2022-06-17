package contacts.sample.cheatsheet.data.java;

import android.app.Activity;

import java.util.Set;

import contacts.core.ContactsFactory;
import contacts.core.Fields;
import contacts.core.data.DataUpdate;
import contacts.core.entities.*;

public class UpdateDataActivity extends Activity {

    DataUpdate.Result updateDataSet(Set<ExistingDataEntity> dataSet) {
        return ContactsFactory.create(this).data().update().data(dataSet).commit();
    }

    DataUpdate.Result updateEmailAndPhone(Email email, Phone phone) {
        MutableEmail mutableEmail = email.mutableCopy();
        mutableEmail.setAddress("myemail@email.com");

        MutablePhone mutablePhone = phone.mutableCopy();
        mutablePhone.setNumber("(555) 555-5555");

        return ContactsFactory.create(this)
                .data()
                .update()
                .data(mutableEmail, mutablePhone)
                .commit();
    }

    DataUpdate.Result updateOnlyMiddleName(MutableName changedName) {
        return ContactsFactory.create(this)
                .data()
                .update()
                .data(changedName)
                .include(Fields.Name.MiddleName)
                .commit();
    }
}