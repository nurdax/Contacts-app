package com.yadro.internship.service

import android.app.Service
import android.content.ContentResolver
import android.content.Intent
import android.os.IBinder
import android.provider.ContactsContract
import android.util.Log
import com.yadro.internship.IContactService
import com.yadro.internship.IOperationCallback

class ContactService : Service() {

    private val binder = object : IContactService.Stub() {
        override fun deleteDuplicateContacts(callback: IOperationCallback?) {
            val result = deleteDuplicates()
            when (result.first) {
                0 -> callback?.onOperationCompleted(0, "Удалено ${result.second} дубликатов")
                1 -> callback?.onOperationCompleted(1, "Дубликаты не найдены")
                else -> callback?.onOperationCompleted(2, "Ошибка при удалении дубликатов")
            }
        }
    }

    override fun onBind(intent: Intent?): IBinder = binder

    private fun deleteDuplicates(): Pair<Int, Int> {
        return try {
            val resolver: ContentResolver = contentResolver
            val uri = ContactsContract.CommonDataKinds.Phone.CONTENT_URI
            val cursor = resolver.query(
                uri,
                arrayOf(
                    ContactsContract.CommonDataKinds.Phone.CONTACT_ID,
                    ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME,
                    ContactsContract.CommonDataKinds.Phone.NUMBER
                ),
                null, null, null
            )
            val seen = mutableMapOf<String, Long>()
            val duplicates = mutableListOf<Long>()

            cursor?.use {
                while (it.moveToNext()) {
                    val id = it.getLong(0)
                    val name = it.getString(1)?.trim()?.lowercase() ?: ""
                    val number = it.getString(2)?.replace("[^0-9]".toRegex(), "") ?: ""
                    if (name.isEmpty() && number.isEmpty()) continue
                    val key = "$name|$number"
                    if (seen.containsKey(key)) {
                        duplicates.add(id)
                    } else {
                        seen[key] = id
                    }
                }
            }

            if (duplicates.isEmpty()) {
                Log.d("ContactService", "Дубликаты не найдены")
                return Pair(1, 0)
            }

            var deletedCount = 0
            for (id in duplicates) {
                val deleted = resolver.delete(
                    ContactsContract.RawContacts.CONTENT_URI,
                    "${ContactsContract.RawContacts.CONTACT_ID}=?",
                    arrayOf(id.toString())
                )
                if (deleted > 0) deletedCount++
            }
            Log.d("ContactService", "Удалено $deletedCount дубликатов")
            Pair(0, deletedCount)
        } catch (e: Exception) {
            Log.e("ContactService", "Ошибка при удалении дубликатов: ${e.message}")
            Pair(2, 0)
        }
    }
}