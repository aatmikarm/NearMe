// Add this utility class for image compression
// Create a new file: app/src/main/java/com/aatmik/nearme/util/ImageCompressor.kt
package com.aatmik.nearme.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Log
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * Utility for compressing images before upload
 */
object ImageCompressor {
    private const val TAG = "ImageCompressor"
    private const val MAX_IMAGE_SIZE = 100 * 1024 // 100 KB
    private const val INITIAL_QUALITY = 80 // Start with 80% quality for compression
    private const val MIN_QUALITY = 40 // Don't go below 40% quality to maintain decent images
    private const val QUALITY_STEP = 5 // Decrease quality by 5% each iteration

    /**
     * Compress a photo from a Uri and return a new Uri to the compressed file
     */
    fun compressImage(context: Context, imageUri: Uri): Uri? {
        try {
            // Open input stream from Uri
            val inputStream = context.contentResolver.openInputStream(imageUri)
                ?: throw IOException("Cannot open input stream from Uri")

            // Decode the image to a bitmap
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()

            // Start compression
            var quality = INITIAL_QUALITY
            var outputStream = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)

            // Continue reducing quality until the size is less than the max
            while (outputStream.size() > MAX_IMAGE_SIZE && quality > MIN_QUALITY) {
                // Reset the output stream
                outputStream.reset()

                // Reduce quality and try again
                quality -= QUALITY_STEP
                bitmap.compress(Bitmap.CompressFormat.JPEG, quality, outputStream)
                Log.d(TAG, "Compressed to quality: $quality, size: ${outputStream.size() / 1024} KB")
            }

            // Create a temporary file for the compressed image
            val outputFile = File(context.cacheDir, "compressed_${System.currentTimeMillis()}.jpg")
            val fileOutputStream = FileOutputStream(outputFile)
            outputStream.writeTo(fileOutputStream)
            fileOutputStream.close()
            outputStream.close()

            Log.d(TAG, "Image compressed from ${getSizeFromUri(context, imageUri) / 1024} KB to ${outputFile.length() / 1024} KB")

            // Return Uri for the compressed file
            return Uri.fromFile(outputFile)
        } catch (e: Exception) {
            Log.e(TAG, "Error compressing image: ${e.message}", e)
            return null
        }
    }

    /**
     * Get the file size from a Uri
     */
    private fun getSizeFromUri(context: Context, uri: Uri): Long {
        try {
            val inputStream = context.contentResolver.openInputStream(uri)
            if (inputStream != null) {
                val size = inputStream.available().toLong()
                inputStream.close()
                return size
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting file size: ${e.message}", e)
        }
        return 0L
    }
}