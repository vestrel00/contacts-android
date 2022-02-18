package contacts.entities.custom.pokemon

import contacts.core.entities.MimeType

internal object PokemonMimeType : MimeType.Custom() {

    // Following Contacts Provider convention of "vnd.android.cursor.item/<package>.<mimetype>"
    override val value: String = "vnd.android.cursor.item/contacts.entities.custom.pokemon"
}