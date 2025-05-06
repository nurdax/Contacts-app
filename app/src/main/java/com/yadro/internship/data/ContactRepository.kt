package com.yadro.internship.data

import android.content.Context
import android.provider.ContactsContract
import android.util.Log

class ContactRepository {
    fun getContacts(context: Context): List<Contact> {
        val contacts = mutableListOf<Contact>()
        val resolver = context.contentResolver
        val cursor = resolver.query(
            ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
            arrayOf(
                ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE,
                ContactsContract.CommonDataKinds.Phone.PHOTO_URI
            ),
            null, null,
            ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME + " ASC"
        )
        cursor?.use {
            val idIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID)
            val nameIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME)
            val phoneIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER)
            val typeIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE)
            val photoIdx = it.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI)
            val seenIds = mutableSetOf<String>()
            while (it.moveToNext()) {
                val id = it.getString(idIdx)
                if (seenIds.contains(id)) continue
                seenIds.add(id)
                val name = it.getString(nameIdx) ?: ""
                val phone = it.getString(phoneIdx)?.replace("[^0-9]".toRegex(), "") ?: ""
                val phoneType = it.getString(typeIdx)
                val photoUri = it.getString(photoIdx)
                if (name.isNotEmpty() || phone.isNotEmpty()) {
                    contacts.add(Contact(id, name, phone, phoneType, photoUri))
                }
            }
        }
        Log.d("ContactRepository", "Загружено ${contacts.size} контактов")
        return contacts
    }
}