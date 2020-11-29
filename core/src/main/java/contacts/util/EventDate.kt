package contacts.util

import contacts.entities.mapper.EventMapper
import java.util.*

/**
 * Converts [Date]s to Strings for matching [com.vestrel00.contacts.entities.Event.date]s in where
 * clauses.
 *
 * Unlike other dates in other entities in this library that are stored as milliseconds, the
 * [com.vestrel00.contacts.entities.Event.date]s are stored in the Content Provider DB as strings in
 * the format of yyyy-MM-dd (e.g. 2019-08-21). Therefore, in order to correctly perform queries
 * with event dates in the where clause, the date value must first be converted using this function.
 */
fun Date?.toWhereString(): String? = EventMapper.dateToString(this)