package com.example.somp

import android.content.Context
import android.provider.MediaStore

class AudioClass {
    data class Audio(
        var id: Long,
        var name: String,
        var artist: String,
        var data: String,
        var dur: Long
    )
    fun mp3ReaderNew(application: Context): List<Audio>{

        val audioProjection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DATA,
            MediaStore.Audio.Media.DURATION,
        )

        val list = mutableListOf<Audio>()
        application.contentResolver.query(
            MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            audioProjection,
            "${MediaStore.Audio.Media.DURATION} > 70000",
            null,
            "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"
        )?.use { cursor ->
            val columnId = cursor.getColumnIndexOrThrow(audioProjection[0])
            val columnName = cursor.getColumnIndexOrThrow(audioProjection[1])
            val columnArtist = cursor.getColumnIndexOrThrow(audioProjection[2])
            val columnPath = cursor.getColumnIndexOrThrow(audioProjection[3])
            val columnDur = cursor.getColumnIndexOrThrow(audioProjection[4])
            while (cursor.moveToNext()) {
                val id = cursor.getLong(columnId)
                val name = cursor.getString(columnName)
                val artist = cursor.getString(columnArtist)
                val path = cursor.getString(columnPath)
                val dur = cursor.getLong(columnDur)
                list.add(Audio(id, name, artist, path, dur))
            }
        }
        return list.toList()
    }
}