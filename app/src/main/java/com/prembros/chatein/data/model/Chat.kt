package com.prembros.chatein.data.model

import com.prembros.chatein.util.Annotations.ChatType
import org.apache.commons.lang.builder.EqualsBuilder
import org.apache.commons.lang.builder.HashCodeBuilder

@Suppress("PropertyName")
class Chat {

    var key: String? = null
    var message: String? = null
    var seen: Boolean = false
    var time_stamp: Long = 0
    @ChatType var type: String? = null
    var from: String? = null

    constructor()

    constructor(message: String?, seen: Boolean, time: Long, type: String?, from: String?) {
        this.message = message
        this.seen = seen
        this.time_stamp = time
        this.type = type
        this.from = from
    }

    override fun hashCode(): Int {
        return HashCodeBuilder()
                .append(key)
                .append(message)
                .append(seen)
                .append(time_stamp)
                .append(type)
                .append(from)
                .toHashCode()
    }

    override fun equals(other: Any?): Boolean {
        return other != null && other is Chat && EqualsBuilder()
                .append(key, other.key)
                .append(message, other.message)
                .append(seen, other.seen)
                .append(time_stamp, other.time_stamp)
                .append(type, other.type)
                .append(from, other.from)
                .isEquals
    }
}