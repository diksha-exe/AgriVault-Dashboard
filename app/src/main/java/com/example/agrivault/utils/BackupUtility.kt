package com.example.agrivault.utils

import com.example.agrivault.data.BackupData
import com.google.gson.Gson
import com.google.gson.GsonBuilder

object BackupUtility {
    private val gson: Gson = GsonBuilder()
        .setPrettyPrinting()
        .create()

    fun serializeBackup(data: BackupData): String {
        return gson.toJson(data)
    }

    fun deserializeBackup(json: String): BackupData? {
        return try {
            gson.fromJson(json, BackupData::class.java)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
