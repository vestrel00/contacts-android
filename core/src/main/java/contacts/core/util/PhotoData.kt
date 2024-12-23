package contacts.core.util

import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import java.io.ByteArrayOutputStream
import java.io.InputStream

/**
 * Holds photo data for insert and update APIs.
 */
sealed interface PhotoData {
    companion object {
        @JvmStatic
        fun from(inputStream: InputStream) = PhotoInputStream(inputStream)

        @JvmStatic
        fun from(byteArray: ByteArray) = PhotoByteArray(byteArray)

        @JvmStatic
        fun from(bitmap: Bitmap) = PhotoBitmap(bitmap)

        @JvmStatic
        fun from(bitmapDrawable: BitmapDrawable) = PhotoBitmapDrawable(bitmapDrawable)
    }
}

/**
 * Determines what photo operation to perform for insert and update APIs.
 */
internal sealed interface PhotoDataOperation {
    class SetPhoto(val photoData: PhotoData) : PhotoDataOperation
    data object RemovePhoto : PhotoDataOperation
}

internal fun PhotoData.bytes(): ByteArray = when (this) {
    is PhotoInputStream -> inputStream.readBytes()
    is PhotoByteArray -> byteArray
    is PhotoBitmap -> bitmap.bytes()
    is PhotoBitmapDrawable -> bitmapDrawable.bitmap.bytes()
}

private fun Bitmap.bytes(): ByteArray {
    val outputStream = ByteArrayOutputStream()
    compress(Bitmap.CompressFormat.PNG, 100, outputStream)
    return outputStream.toByteArray()
}

/**
 * Holds an [inputStream] containing the photo data to be used for insert and update APIs.
 *
 * It is up to you to close the [inputStream] after use.
 */
class PhotoInputStream(val inputStream: InputStream) : PhotoData

/**
 * Holds a [byteArray] containing the photo data to be used for insert and update APIs.
 */
class PhotoByteArray(val byteArray: ByteArray) : PhotoData

/**
 * Holds a [bitmap] containing the photo data to be used for insert and update APIs.
 */
class PhotoBitmap(val bitmap: Bitmap) : PhotoData

/**
 * Holds a [bitmapDrawable] containing the photo data to be used for insert and update APIs.
 */
class PhotoBitmapDrawable(val bitmapDrawable: BitmapDrawable) : PhotoData