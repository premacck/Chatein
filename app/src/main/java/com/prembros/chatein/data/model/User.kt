package com.prembros.chatein.data.model

import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.database.DataSnapshot
import java.util.*

@Suppress("PropertyName", "MemberVisibilityCanBePrivate")
class User : Parcelable {

    var token: String? = null
    var name: String? = null
    var profile_image: String? = null
    var thumb_image: String? = null
    var status: String? = null

    constructor(parcel: Parcel) : this() {
        token = parcel.readString()
        name = parcel.readString()
        profile_image = parcel.readString()
        thumb_image = parcel.readString()
        status = parcel.readString()
    }

    constructor()

    constructor(dataSnapshot: DataSnapshot) {
        try {
            name = Objects.requireNonNull<Any>(dataSnapshot.child("name").value).toString()
            status = Objects.requireNonNull<Any>(dataSnapshot.child("status").value).toString()
            profile_image = Objects.requireNonNull<Any>(dataSnapshot.child("profile_image").value).toString()
            thumb_image = Objects.requireNonNull<Any>(dataSnapshot.child("thumb_image").value).toString()
            token = Objects.requireNonNull<Any>(dataSnapshot.child("device_token").value).toString()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(token)
        parcel.writeString(name)
        parcel.writeString(profile_image)
        parcel.writeString(thumb_image)
        parcel.writeString(status)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<User> {
        override fun createFromParcel(parcel: Parcel): User {
            return User(parcel)
        }

        override fun newArray(size: Int): Array<User?> {
            return arrayOfNulls(size)
        }
    }
}