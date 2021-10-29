package contacts.core.entities.mapper

import contacts.core.entities.Options
import contacts.core.entities.cursor.OptionsCursor

/**
 * Mapper for Contacts or RawContacts options from the Data, RawContacts, or Contacts table.
 */
internal class OptionsMapper(private val optionsCursor: OptionsCursor<*>) : EntityMapper<Options> {

    override val value: Options
        get() = Options(
            id = optionsCursor.id,

            starred = optionsCursor.starred,

            // Deprecated in API 29 - contains useless value for all Android versions in Play store.
            // timesContacted = optionsCursor.timesContacted,
            // lastTimeContacted = optionsCursor.lastTimeContacted,

            customRingtone = optionsCursor.customRingtone,

            sendToVoicemail = optionsCursor.sendToVoicemail
        )
}
