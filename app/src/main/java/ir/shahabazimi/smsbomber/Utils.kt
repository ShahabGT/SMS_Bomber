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
import java.util.Date
import java.util.GregorianCalendar


fun String?.isNumber(): Boolean {
    if (this.isNullOrEmpty()) return false

    for (c in this)
        if (!c.isDigit())
            return false

    return true
}

fun saveFile(
    context: Context,
    uri: Uri
): String? {
    val inputStream = context.contentResolver.openInputStream(uri)
    return if (inputStream != null) {
        val size = inputStream.available()
        val bytes = ByteArray(size)
        inputStream.read(bytes)
        inputStream.close()
        saveFileInCache(
            context,
            bytes
        )
    } else null
}

private fun saveFileInCache(
    context: Context,
    data: ByteArray
): String {
    val directory = File("${context.cacheDir}/smsbomber")
    if (!directory.exists()) directory.mkdirs()
    val imageFile = File(
        directory,
        createFileName()
    )
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
    val gregorianCalendar = GregorianCalendar()
    val persianCalendar = Date(System.currentTimeMillis())
    val persianYear = persianCalendar.year
    val persianMonth = persianCalendar.month
    val persianDay = persianCalendar.day
    return "" + persianYear + "_" +
            persianMonth + "_" +
            persianDay + "_" +
            gregorianCalendar.get(Calendar.HOUR_OF_DAY).formatToMilitary() +
            gregorianCalendar.get(Calendar.MINUTE).formatToMilitary() +
            gregorianCalendar.get(Calendar.SECOND).formatToMilitary() +
            ".xlsx"
}


fun showToast(context: Context, text: String?) =
    Toast.makeText(context, text, Toast.LENGTH_SHORT).show()

fun Array<String>.isAllPermissionGranted(context: Context): Boolean = this.all {
    if (it.contains("permission")) {
        ContextCompat.checkSelfPermission(
            context, it
        ) == PackageManager.PERMISSION_GRANTED
    } else {
        false
    }
}