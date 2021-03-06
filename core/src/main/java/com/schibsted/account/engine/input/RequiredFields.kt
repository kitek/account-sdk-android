/*
 * Copyright (c) 2018 Schibsted Products & Technology AS. Licensed under the terms of the MIT license. See LICENSE in the project root.
 */

package com.schibsted.account.engine.input

import android.os.Parcel
import android.os.Parcelable
import com.google.gson.JsonObject
import com.schibsted.account.engine.integration.InputProvider
import com.schibsted.account.engine.integration.ResultCallback
import com.schibsted.account.model.NoValue

data class RequiredFields(val fields: Map<String, String>) : Parcelable {
    @Suppress("UNCHECKED_CAST")
    constructor(source: Parcel) : this(
            source.readHashMap(Map::class.java.classLoader) as Map<String, String>
    )

    override fun describeContents() = 0

    override fun writeToParcel(dest: Parcel, flags: Int) = with(dest) {
        writeMap(fields)
    }

    interface Provider {
        /**
         * Called when additional fields are needed to log in the user
         * @param fields The fields required by the client
         */
        fun onRequiredFieldsRequested(requiredFieldsProvider: InputProvider<RequiredFields>, fields: Set<String>)
    }

    companion object {
        private const val BIRTHDAY_KEY = "birthday"
        private const val DISPLAY_NAME_KEY = "displayName"
        private const val OLD_FAMILY_NAME_KEY = "name.family_name"
        private const val OLD_GIVEN_NAME_KEY = "name.given_name"
        private const val FAMILY_NAME_KEY = "familyName"
        private const val GIVEN_NAME_KEY = "givenName"
        private const val NAME_KEY = "name"

        const val FIELD_EMAIL = "email"

        private val DATE_PATTERN = Regex("[0-9]{4}-[0-9]{2}-[0-9]{2}")

        val SUPPORTED_FIELDS = setOf(BIRTHDAY_KEY, DISPLAY_NAME_KEY, OLD_FAMILY_NAME_KEY, OLD_GIVEN_NAME_KEY, FIELD_EMAIL)

        @JvmField
        val CREATOR: Parcelable.Creator<RequiredFields> = object : Parcelable.Creator<RequiredFields> {
            override fun createFromParcel(source: Parcel): RequiredFields = RequiredFields(source)
            override fun newArray(size: Int): Array<RequiredFields?> = arrayOfNulls(size)
        }

        internal fun request(provider: Provider, missingFields: Set<String>, onProvided: (RequiredFields, ResultCallback<NoValue>) -> Unit) {
            provider.onRequiredFieldsRequested(InputProvider(onProvided, { validation ->
                when {
                    !validation.fields.keys.containsAll(missingFields) -> {
                        val missing = missingFields.filterNot { validation.fields.contains(it) }.joinToString(", ")
                        "Missing fields: $missing"
                    }
                    validation.fields[BIRTHDAY_KEY]?.let { DATE_PATTERN.matches(it) } == false -> {
                        "Invalid date format. Input <${validation.fields[BIRTHDAY_KEY]}> did not match <YYYY-MM-DD>"
                    }
                    else -> null
                }
            }), missingFields)
        }

        @Deprecated("Provide the proper JSON object instead")
        internal fun transformFieldsToProfile(fields: Map<String, String>): Map<String, Any> {
            val map = hashMapOf<String, Any>()
            map.putAll(fields)

            val birthdayValue = fields[BIRTHDAY_KEY]
            val familyName = fields[OLD_FAMILY_NAME_KEY]
            val givenName = fields[OLD_GIVEN_NAME_KEY]

            map.remove(BIRTHDAY_KEY)
            map.remove(OLD_FAMILY_NAME_KEY)
            map.remove(OLD_GIVEN_NAME_KEY)

            familyName?.let {
                val jsonObject = JsonObject()
                jsonObject.addProperty(FAMILY_NAME_KEY, familyName)
                givenName?.let {
                    jsonObject.addProperty(GIVEN_NAME_KEY, givenName)
                }
                map.put(NAME_KEY, jsonObject)
            }

            birthdayValue?.let { map.put(BIRTHDAY_KEY, birthdayValue) }

            return map
        }
    }
}
