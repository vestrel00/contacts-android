package contacts.test.entities

import contacts.core.entities.custom.CustomDataCountRestriction

/**
 * A RawContact may have at most 1 test data.
 */
internal val TEST_DATA_COUNT_RESTRICTION = CustomDataCountRestriction.AT_MOST_ONE