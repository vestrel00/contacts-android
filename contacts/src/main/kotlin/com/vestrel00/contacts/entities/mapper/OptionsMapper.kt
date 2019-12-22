package com.vestrel00.contacts.entities.mapper

import com.vestrel00.contacts.entities.Options
import com.vestrel00.contacts.entities.cursor.OptionsCursor

internal class OptionsMapper(private val optionsCursor: OptionsCursor) {

    val options: Options
        get() = Options(
            id = optionsCursor.id,

            starred = optionsCursor.starred,

            timesContacted = optionsCursor.timesContacted,
            lastTimeContacted = optionsCursor.lastTimeContacted,

            customRingtone = optionsCursor.customRingtone,

            sendToVoicemail = optionsCursor.sendToVoicemail
        )
}
