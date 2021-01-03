package contacts.entities.custom.gender

import contacts.entities.custom.CustomDataCountRestriction

/**
 * There should only be one gender entry per RawContact.
 */
internal val GENDER_COUNT_RESTRICTION = CustomDataCountRestriction.AT_MOST_ONE