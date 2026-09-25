package com.sepantartd.shirokhorshid.emoji

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.sepantartd.shirokhorshid.R
import java.io.File
import java.io.FileOutputStream

object StickerHelper {

    fun getLionAndSunStickerUri(context: Context): Uri? {
        return try {
            val stickersDir = File(context.cacheDir, "stickers")
            if (!stickersDir.exists()) {
                stickersDir.mkdirs()
            }

            val stickerFile = File(stickersDir, "lion_and_sun.png")

            if (!stickerFile.exists()) {
                val drawable = ContextCompat.getDrawable(context, R.drawable.ic_lion_and_sun) ?: return null
                val bitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)
                val canvas = Canvas(bitmap)
                drawable.setBounds(0, 0, canvas.width, canvas.height)
                drawable.draw(canvas)

                FileOutputStream(stickerFile).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }
            }

            FileProvider.getUriForFile(
                context,
                "com.sepantartd.shirokhorshid.fileprovider",
                stickerFile
            )
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
}
