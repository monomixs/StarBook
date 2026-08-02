package com.starbook.core.scanner

import android.content.Context
import android.net.Uri
import com.starbook.core.logging.api.Logger
import dev.zacsweers.metro.Inject
import org.jaudiotagger.audio.AudioFileIO
import org.jaudiotagger.tag.FieldKey
import java.io.File

@Inject
public class TagWriter(private val context: Context) {

    public fun writeMetadata(uri: Uri, title: String?, author: String?, genre: String?) {
        try {
            val file = if (uri.scheme == "file") {
                File(uri.path!!)
            } else {
                // For content:// URIs, jaudiotagger needs a physical file.
                // We might need to copy to a temp file, tag it, and write back.
                // This is complex and potentially slow.
                // For now, let's try direct file if possible.
                null
            }

            if (file != null && file.exists()) {
                val audioFile = AudioFileIO.read(file)
                val tag = audioFile.tag ?: audioFile.createDefaultTag()

                title?.let { tag.setField(FieldKey.TITLE, it) }
                author?.let { tag.setField(FieldKey.ARTIST, it) }
                genre?.let { tag.setField(FieldKey.GENRE, it) }

                audioFile.commit()
                Logger.i("Successfully wrote metadata to ${file.absolutePath}")
            } else if (uri.scheme == "content") {
                // Workaround for content URIs:
                // 1. Copy to temp file
                // 2. Tag temp file
                // 3. Write back to content URI
                val tempFile = File(context.cacheDir, "temp_tagging_${System.currentTimeMillis()}")
                context.contentResolver.openInputStream(uri)?.use { input ->
                    tempFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                }

                if (tempFile.exists()) {
                    val audioFile = AudioFileIO.read(tempFile)
                    val tag = audioFile.tag ?: audioFile.createDefaultTag()

                    title?.let { tag.setField(FieldKey.TITLE, it) }
                    author?.let { tag.setField(FieldKey.ARTIST, it) }
                    genre?.let { tag.setField(FieldKey.GENRE, it) }

                    audioFile.commit()

                    context.contentResolver.openOutputStream(uri, "rwt")?.use { output ->
                        tempFile.inputStream().use { input ->
                            input.copyTo(output)
                        }
                    }
                    tempFile.delete()
                    Logger.i("Successfully wrote metadata to content URI: $uri")
                }
            }
        } catch (e: Exception) {
            Logger.w(e, "Failed to write metadata to $uri")
        }
    }
}
