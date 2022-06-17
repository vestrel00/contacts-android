package contacts.sample.cheatsheet.data.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.data.DataDelete
import contacts.core.entities.*
import contacts.core.equalTo

class DeleteDataActivity : Activity() {

    fun deleteData(data: ExistingDataEntity): DataDelete.Result =
        Contacts(this).data().delete().data(data).commit()

    fun deleteEmailsAndPhones(emails: Set<Email>, phones: Set<Phone>): DataDelete.Result =
        Contacts(this)
            .data()
            .delete()
            .data(emails + phones)
            .commit()

    fun deleteDataWithId(dataId: Long): DataDelete.Result =
        Contacts(this).data().delete().dataWithId(dataId).commit()

    fun deleteAllWorkEmails(): DataDelete.Result =
        Contacts(this)
            .data()
            .delete()
            .dataWhere {
                Email.Type equalTo EmailEntity.Type.WORK
            }
            .commit()
}