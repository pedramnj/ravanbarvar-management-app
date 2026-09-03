package com.ravanbarvar.patientmanager.util

import android.content.ContentResolver
import android.content.Context
import android.provider.ContactsContract

data class ContactSummary(val id: Long, val name: String, val phone: String)

object ContactsUtils {

    /** Reads every phone-having contact from the device. Run off the main thread. */
    fun queryContacts(context: Context): List<ContactSummary> {
        val resolver = context.contentResolver
        val result = mutableListOf<ContactSummary>()
        resolver.query(
            ContactsContract.Contacts.CONTENT_URI,
            arrayOf(ContactsContract.Contacts._ID, ContactsContract.Contacts.DISPLAY_NAME),
            "${ContactsContract.Contacts.HAS_PHONE_NUMBER} > 0",
            null,
            "${ContactsContract.Contacts.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val idIdx = cursor.getColumnIndexOrThrow(ContactsContract.Contacts._ID)
            val nameIdx = cursor.getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(idIdx)
                val name = cursor.getString(nameIdx)?.trim().orEmpty()
                if (name.isBlank()) continue
                result.add(ContactSummary(id, name, queryPrimaryPhone(resolver, id)))
            }
        }
        return result
    }

    private fun queryPrimaryPhone(resolver: ContentResolver, contactId: Long): String {
        resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(ContactsContract.CommonDataKinds.Phone.NUMBER),
            "${ContactsContract.CommonDataKinds.Phone.CONTACT_ID} = ?",
            arrayOf(contactId.toString()),
            null
        )?.use { cursor ->
            if (cursor.moveToFirst()) {
                val numIdx = cursor.getColumnIndexOrThrow(ContactsContract.CommonDataKinds.Phone.NUMBER)
                return cursor.getString(numIdx)?.trim().orEmpty()
            }
        }
        return ""
    }

    /** "Sara Ahmadi" -> ("Sara", "Ahmadi"); a single-word name goes entirely into firstName. */
    fun splitName(fullName: String): Pair<String, String> {
        val trimmed = fullName.trim()
        val spaceIdx = trimmed.indexOf(' ')
        return if (spaceIdx == -1) trimmed to "" else trimmed.substring(0, spaceIdx) to trimmed.substring(spaceIdx + 1).trim()
    }
}
