import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import java.io.ByteArrayOutputStream
import java.io.Closeable
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

class ImageHelper(private val mContext: Context) {

    companion object {
        fun getStreamByteFromImage(imageFile: File): ByteArray {
            var photoBitmap = BitmapFactory.decodeFile(imageFile.path)
            val stream = ByteArrayOutputStream()

            val imageRotation = getImageRotation(imageFile)

            if (imageRotation != 0)
                photoBitmap = getBitmapRotatedByDegree(photoBitmap, imageRotation)

            photoBitmap.compress(Bitmap.CompressFormat.JPEG, 60, stream)

            return stream.toByteArray()
        }

        private fun getImageRotation(imageFile: File): Int {
            var exif: ExifInterface? = null
            var exifRotation = 0

            try {
                exif = ExifInterface(imageFile.path)
                exifRotation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } catch (e: IOException) {
                e.printStackTrace()
            }

            return exif?.let { exifToDegrees(exifRotation) } ?: 0
        }

        private fun exifToDegrees(rotation: Int): Int {
            return when (rotation) {
                ExifInterface.ORIENTATION_ROTATE_90 -> 90
                ExifInterface.ORIENTATION_ROTATE_180 -> 180
                ExifInterface.ORIENTATION_ROTATE_270 -> 270
                else -> 0
            }
        }

        private fun getBitmapRotatedByDegree(bitmap: Bitmap, rotationDegree: Int): Bitmap {
            val matrix = Matrix()
            matrix.preRotate(rotationDegree.toFloat())
            return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
        }

        fun getPathFromGooglePhotosUri(context: Context, uriPhoto: Uri?): String? {
            if (uriPhoto == null) return null

            var input: FileInputStream? = null
            var output: FileOutputStream? = null
            try {
                val pfd = context.contentResolver.openFileDescriptor(uriPhoto, "r")
                val fd = pfd?.fileDescriptor
                input = FileInputStream(fd)

                val tempFilename = getTempFilename(context)
                output = FileOutputStream(tempFilename)

                val bytes = ByteArray(4096)
                var read: Int
                while (input.read(bytes).also { read = it } != -1) {
                    output.write(bytes, 0, read)
                }
                return tempFilename
            } catch (ignored: IOException) {
                // Nothing we can do
            } finally {
                closeSilently(input)
                closeSilently(output)
            }
            return null
        }

        private fun closeSilently(c: Closeable?) {
            try {
                c?.close()
            } catch (ignored: Throwable) {
                // Do nothing
            }
        }

        @Throws(IOException::class)
        private fun getTempFilename(context: Context): String {
            val outputDir: File = context.cacheDir
            val outputFile = File.createTempFile("image", "tmp", outputDir)
            return outputFile.absolutePath
        }
    }
}
