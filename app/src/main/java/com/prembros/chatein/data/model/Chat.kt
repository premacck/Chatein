package com.prembros.chatein.data.model

import com.prembros.chatein.util.Annotations.ChatType

@Suppress("PropertyName")
class Chat {

    var message: String? = null
    var seen: Boolean? = null
    var time_stamp: Long? = null
    @ChatType var type: String? = null
    var from: String? = null

    constructor()

    constructor(message: String?, seen: Boolean?, time: Long?, type: String?, from: String?) {
        this.message = message
        this.seen = seen
        this.time_stamp = time
        this.type = type
        this.from = from
    }
}