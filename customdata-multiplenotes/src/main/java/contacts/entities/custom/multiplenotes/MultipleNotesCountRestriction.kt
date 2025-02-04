package contacts.entities.custom.multiplenotes

import contacts.core.entities.custom.CustomDataCountRestriction

/**
 * A RawContact may have 0, 1, or more multiple notes.
 *
 * This overrides the built-in note count restriction of [CustomDataCountRestriction.AT_MOST_ONE].
 */
internal val MULTIPLE_NOTES_COUNT_RESTRICTION = CustomDataCountRestriction.NO_LIMIT