package com.prembros.chatein.data.model

import java.util.*

class UserBuilder private constructor() {

    private val map: MutableMap<String, String>

    init {
        map = HashMap()
    }

    fun setToken(token: String): UserBuilder {
        map["device_token"] = token
        return this
    }

    fun setName(name: String): UserBuilder {
        map["name"] = name
        return this
    }

    fun setStatus(status: String): UserBuilder {
        map["status"] = status
        return this
    }

    fun setProfileImage(profileImage: String): UserBuilder {
        map["profile_image"] = profileImage
        return this
    }

    fun setThumbImage(thumbImage: String): UserBuilder {
        map["thumb_image"] = thumbImage
        return this
    }

    fun getMap(): Map<String, String> {
        return map
    }

    companion object {
        val new: UserBuilder
            get() = UserBuilder()
    }
}