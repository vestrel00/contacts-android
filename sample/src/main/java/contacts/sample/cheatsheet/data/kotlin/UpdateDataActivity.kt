package contacts.sample.cheatsheet.data.kotlin

import android.app.Activity
import contacts.core.Contacts
import contacts.core.Fields
import contacts.core.data.DataUpdate
import contacts.core.entities.*

class UpdateDataActivity : Activity() {

    fun updateDataSet(dataSet: Set<ExistingDataEntity>): DataUpdate.Result =
        Contacts(this).data().update().data(dataSet).commit()

    fun updateEmailAndPhone(email: Email, phone: Phone): DataUpdate.Result = Contacts(this)
        .data()
        .update()
        .data(
            email.mutableCopy {
                address = "myemail@email.com"
            },
            phone.mutableCopy {
                number = "(555) 555-5555"
            }
        )
        .commit()

    fun updateOnlyMiddleName(changedName: MutableName): DataUpdate.Result = Contacts(this)
        .data()
        .update()
        .data(changedName)
        .include(Fields.Name.MiddleName)
        .commit()
}