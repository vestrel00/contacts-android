package contacts.entities.custom.gender

import contacts.entities.custom.CustomDataCountRestriction

/**
 * A RawContact may have at most 1 gender.
 */
internal val GENDER_COUNT_RESTRICTION = CustomDataCountRestriction.AT_MOST_ONE