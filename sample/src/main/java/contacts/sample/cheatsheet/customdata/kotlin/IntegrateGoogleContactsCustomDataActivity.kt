package contacts.sample.cheatsheet.customdata.kotlin

import android.app.Activity
import contacts.core.*
import contacts.core.data.*
import contacts.core.entities.*
import contacts.core.entities.custom.CustomDataRegistry
import contacts.entities.custom.googlecontacts.*
import contacts.entities.custom.googlecontacts.fileas.*
import contacts.entities.custom.googlecontacts.userdefined.*

class IntegrateGoogleContactsCustomDataActivity : Activity() {

    val contacts = Contacts(this, false, CustomDataRegistry().register(GoogleContactsRegistration()))

    fun getContactsWithGoogleContactsCustomData(): List<Contact> = contacts
        .query()
        .where {
            GoogleContactsFields.FileAs.Name.isNotNull()
                .or(GoogleContactsFields.UserDefined.Field.isNotNull())
        }
        .find()

    fun insertRawContactWithGoogleContactsCustomData(): Insert.Result = contacts
        .insert()
        .rawContact {
            setFileAs(contacts) {
                name = "Lucky"
            }
            addUserDefined(contacts) {
                field = "Lucky Field"
                label = "Lucky Label"
            }
        }
        .commit()

    fun updateRawContactGoogleContactsCustomData(rawContact: RawContact): Update.Result = contacts
        .update()
        .rawContacts(
            rawContact.mutableCopy {
                fileAs(contacts)?.name = "Unfortunate"
                userDefined(contacts).firstOrNull()?.apply {
                    field = "Unfortunate Field"
                    label = "Unfortunate Label"
                }
            }
        )
        .commit()

    fun deleteGoogleContactsCustomDataFromRawContact(rawContact: RawContact): Update.Result =
        contacts
            .update()
            .rawContacts(
                rawContact.mutableCopy {
                    setFileAs(contacts, null)
                    removeAllUserDefined(contacts)
                }
            )
            .commit()

    fun getAllFileAs(): List<FileAs> = contacts.data().query().fileAs().find()

    fun getAllUserDefined(): List<UserDefined> = contacts.data().query().userDefined().find()

    fun updateFileAsAndUserDefined(
        fileAs: MutableFileAs, userDefined: MutableUserDefined
    ): DataUpdate.Result = contacts.data().update().data(fileAs, userDefined).commit()

    fun deleteFileAsAndUserDefined(fileAs: FileAs, userDefined: UserDefined): DataDelete.Result =
        contacts.data().delete().data(fileAs, userDefined).commit()
}