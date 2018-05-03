package com.prembros.chatein.data.model

import java.util.*

class UserUpdater private constructor() {

    private val map: MutableMap<String, Any>

    init {
        map = HashMap()
    }

    fun setName(name: String): UserUpdater {
        map["name"] = name
        return this
    }

    fun setStatus(status: String): UserUpdater {
        map["status"] = status
        return this
    }

    fun setProfileImage(profileImage: String): UserUpdater {
        map["profile_image"] = profileImage
        return this
    }

    fun setThumbImage(thumbImage: String): UserUpdater {
        map["thumb_image"] = thumbImage
        return this
    }

    fun getMap(): Map<String, Any> {
        return map
    }

    companion object {
        val new: UserUpdater
            get() = UserUpdater()
    }
}