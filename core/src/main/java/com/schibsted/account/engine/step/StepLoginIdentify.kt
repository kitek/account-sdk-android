/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.step

import android.os.Parcel
import android.os.Parcelable
import com.schibsted.account.engine.input.Credentials
import com.schibsted.account.network.response.AgreementLinksResponse
import com.schibsted.account.session.User

data class StepLoginIdentify(
    val credentials: Credentials,
    val user: User,
    val agreementsAccepted: Boolean,
    val missingFields: Set<String>,
    val agreementLinks: AgreementLinksResponse? = null
) : Step(), Parcelable {
    constructor(source: Parcel) : this(
            source.readParcelable<Credentials>(Credentials::class.java.classLoader),
            source.readParcelable<User>(User::class.java.classLoader),
            1 == source.readInt(),
            source.readStringSet(),
            source.readParcelable<AgreementLinksResponse>(AgreementLinksResponse::class.java.classLoader)
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeParcelable(credentials, 0)
        writeParcelable(user, 0)
        writeInt((if (agreementsAccepted) 1 else 0))
        writeStringSet(missingFields)
        writeParcelable(agreementLinks, 0)
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<StepLoginIdentify> = object : Parcelable.Creator<StepLoginIdentify> {
            override fun createFromParcel(source: Parcel): StepLoginIdentify = StepLoginIdentify(source)
            override fun newArray(size: Int): Array<StepLoginIdentify?> = arrayOfNulls(size)
        }
    }
}
