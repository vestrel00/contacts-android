package com.vestrel00.contacts.util

import com.vestrel00.contacts.entities.mapper.EventMapper
import java.util.*

fun Date?.toWhereString(): String? = EventMapper.dateToString(this)