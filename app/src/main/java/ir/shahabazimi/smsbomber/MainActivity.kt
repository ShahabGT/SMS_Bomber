package ir.shahabazimi.smsbomber

import android.Manifest
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import ir.shahabazimi.smsbomber.databinding.ActivityMainBinding
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory
import java.io.FileInputStream


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var excelPickerLauncher: ActivityResultLauncher<Array<String>>
    private val gatheredData: MutableList<Pair<String, String>> = mutableListOf()

    private val smsPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            handleSMSPermission(it)
        }

    private fun isSMSPermissionGranted() =
        arrayOf(Manifest.permission.SEND_SMS).isAllPermissionGranted(this)

    private fun handleSMSPermission(isAllowed: Boolean) {
        if (isAllowed)
            sendSms()
        else
            showToast(this, getString(R.string.sms_permission_error))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        initViews()
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
                    } else
                        showToast(this, getString(R.string.read_file_error))
                } else {
                    showToast(this, getString(R.string.read_file_error))
                }
            }
    }

    private fun initViews() = with(binding) {
        uploadCard.setOnClickListener { openExcelFile() }
    }

    private fun handleDesign() = with(binding) {
        countText.text = gatheredData.size.toString()
        countCard.setOnClickListener {
            if (gatheredData.isEmpty()) {
                showToast(this@MainActivity, getString(R.string.empty_file_error))
            } else {
                val contents = gatheredData.map { it.first + ": " + it.second }.toTypedArray()
                MaterialAlertDialogBuilder(this@MainActivity)
                    .setTitle("List of Entities")
                    .setItems(contents) { a, b ->
                        gatheredData.removeAt(b)
                        countText.text = gatheredData.size.toString()
                        a.dismiss()
                    }
                    .show()
            }
        }

        sendCard.setOnClickListener {
            checkSMSPermission()
        }

    }

    private fun checkSMSPermission() {
        if (!isSMSPermissionGranted()) {
            smsPermission.launch(Manifest.permission.SEND_SMS)
        } else {
            sendSms()
        }
    }

    private fun sendSms() {
        showToast(this, "SEND SMS")
    }


    private fun openExcelFile() = excelPickerLauncher.launch(
        arrayOf(EXCEL_MIME)
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
            if (first.isNumber())
                data.add(Pair(first, second))
        }
        return data
    }


    override fun onDestroy() {
        super.onDestroy()
        smsPermission.unregister()
    }

}