package ir.shahabazimi.smsbomber

import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import ir.shahabazimi.smsbomber.databinding.ActivityMainBinding
import org.apache.poi.hssf.usermodel.HSSFWorkbookFactory
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.xssf.usermodel.XSSFWorkbookFactory
import java.io.FileInputStream


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var excelPickerLauncher: ActivityResultLauncher<Array<String>>


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
                    if (file != null)
                        readExcelFile(file)
                    else
                        showToast(this, getString(R.string.read_file_error))
                } else {
                    showToast(this, getString(R.string.read_file_error))
                }
            }
    }

    private fun initViews() = with(binding) {
        button.setOnClickListener { openExcelFile() }
    }


    private fun openExcelFile() = excelPickerLauncher.launch(
        arrayOf(EXCEL_MIME)
    )

    private fun readExcelFile(filepath: String) {
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

    }


}