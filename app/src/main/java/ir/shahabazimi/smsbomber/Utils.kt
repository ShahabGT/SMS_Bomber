package ir.shahabazimi.smsbomber

import android.content.Context
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.Calendar
import java.util.GregorianCalendar


fun String?.isNumber(): Boolean {
    if (this.isNullOrEmpty()) return false

    for (c in this) if (!c.isDigit()) return false

    return true
}

fun saveFile(
    context: Context, uri: Uri
): String? {
    val inputStream = context.contentResolver.openInputStream(uri)
    return if (inputStream != null) {
        val bytes = ByteArray(inputStream.available())
        inputStream.read(bytes)
        inputStream.close()
        saveFileInCache(
            context, bytes
        )
    } else {
        null
    }
}

private fun saveFileInCache(
    context: Context, data: ByteArray
): String {
    val directory = File("${context.cacheDir}/${context.getString(R.string.app_name)}")
    if (!directory.exists()) directory.mkdirs()
    val imageFile = File(directory, createFileName())
    val fOut = FileOutputStream(imageFile)
    return try {
        fOut.write(data)
        imageFile.absolutePath
    } catch (e: IOException) {
        showToast(context, e.toString())
        ""
    } finally {
        try {
            fOut.close()
        } catch (e: IOException) {
            showToast(context, e.toString())
        }
    }
}

private fun Int.formatToMilitary(): String = if (this < 10) "0$this" else this.toString()

private fun createFileName(): String {
    val calendar = GregorianCalendar()
    return calendar.get(Calendar.HOUR_OF_DAY).formatToMilitary() + calendar.get(Calendar.MINUTE)
        .formatToMilitary() + calendar.get(Calendar.SECOND).formatToMilitary() + ".xlsx"
}

fun showToast(context: Context, text: String?) =
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()

fun Array<String>.isPermissionGranted(context: Context): Boolean = this.all {
    if (it.contains("permission")) {
        ContextCompat.checkSelfPermission(
            context, it
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        false
    }
}