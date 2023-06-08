package ir.shahabazimi.smsbomber

import android.Manifest
import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.telephony.SmsManager
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.transition.TransitionManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.shahabazimi.smsbomber.databinding.ActivityMainBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory
import java.io.FileInputStream

@SuppressLint("SetTextI18n")
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var excelPickerLauncher: ActivityResultLauncher<Array<String>>
    private val gatheredData: MutableList<Pair<String, String>> = mutableListOf()

    private val smsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            handleSMSPermission(it)
        }

    private val smsManager = SmsManager.getDefault()

    private fun isSMSPermissionGranted() =
        arrayOf(Manifest.permission.SEND_SMS).isPermissionGranted(this)

    private fun handleSMSPermission(isAllowed: Boolean) {
        if (isAllowed)
            lifecycleScope.launch {
                sendSms()
            }
        else
            showToast(this, getString(R.string.sms_permission_error))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        WorkbookFactory.addProvider(HSSFWorkbookFactory())
        WorkbookFactory.addProvider(XSSFWorkbookFactory())
        excelPickerLauncher =
            registerForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
                if (uri != null) {
                    val file = saveFile(this, uri)
                    if (file != null) {
                        gatheredData.clear()
                        gatheredData.addAll(readExcelFile(file))
                        handleDesign()
                    } else {
                        showToast(this, getString(R.string.read_file_error))
                    }
                } else {
                    showToast(this, getString(R.string.read_file_error))
                }
            }

        binding.uploadCard.setOnClickListener { openExcelFile() }
    }

    private fun handleDesign() = with(binding) {
        scrollView.visibility = View.VISIBLE
        countText.text = gatheredData.size.toString()
        countCard.setOnClickListener {
            if (gatheredData.isEmpty()) {
                showToast(this@MainActivity, getString(R.string.empty_file_error))
            } else {
                val contents = gatheredData.map { it.first + ": " + it.second }.toTypedArray()
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle(getString(R.string.list_entries))
                    .setIcon(R.drawable.list_icon)
                    .setItems(contents) { dialog, position ->
                        gatheredData.removeAt(position)
                        countText.text = gatheredData.size.toString()
                        dialog.dismiss()
                    }.show()
            }
        }

        sendCard.setOnClickListener {
            if (gatheredData.isEmpty())
                showToast(this@MainActivity, getString(R.string.empty_file_error))
            else
                checkSMSPermission()
        }

    }

    private fun checkSMSPermission() {
        if (!isSMSPermissionGranted()) {
            smsPermission.launch(Manifest.permission.SEND_SMS)
        } else {
            with(binding) {
                animateLayoutChanges()
                sendCard.visibility = View.GONE
                countCard.visibility = View.GONE
                progressCard.visibility = View.VISIBLE
                progressText.text = "0/${gatheredData.size}"
                uploadCard.isEnabled = false
            }
            lifecycleScope.launch {
                sendSms()
            }
        }
    }

    private suspend fun sendSms() {
        delay(1000)
        gatheredData.forEachIndexed { index, data ->
            val (phoneNumber, message) = data
            smsManager.sendTextMessage(phoneNumber, null, message, null, null)
            calculateProgress(index + 1)
            delay(500)
        }
        delay(1000)
        with(binding) {
            animateLayoutChanges()
            sendCard.visibility = View.VISIBLE
            countCard.visibility = View.VISIBLE
            progressCard.visibility = View.GONE
            progressIndicator.setProgress(0, false)
            uploadCard.isEnabled = true
        }

    }

    private fun calculateProgress(count: Int) {
        val max = gatheredData.size
        val progress = (count * 100) / max
        binding.progressIndicator.setProgress(progress, true)
        binding.progressText.text = "$count/$max"
    }

    private fun openExcelFile() = excelPickerLauncher.launch(
        arrayOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
    )

    private fun readExcelFile(filepath: String): List<Pair<String, String>> {
        val inputStream = FileInputStream(filepath)
        val xlWb = WorkbookFactory.create(inputStream)
        val firsColumn = 0
        val secondColumn = 1
        val xlWs = xlWb.getSheetAt(0)
        val data: MutableList<Pair<String, String>> = mutableListOf()

        xlWs.forEach { row ->
            val first = row.getCell(firsColumn).stringCellValue
            val second = row.getCell(secondColumn).stringCellValue
            if (first.isNumber()) data.add(Pair(first, second))
        }
        return data
    }

    private fun animateLayoutChanges() =
        TransitionManager.beginDelayedTransition(binding.rootView)

    override fun onDestroy() {
        super.onDestroy()
        smsPermission.unregister()
    }

}