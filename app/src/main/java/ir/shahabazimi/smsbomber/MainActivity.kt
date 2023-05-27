package ir.shahabazimi.smsbomber

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.DocumentsContract
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.net.toFile
import ir.shahabazimi.smsbomber.databinding.ActivityMainBinding
import org.apache.poi.ss.usermodel.WorkbookFactory
import java.io.File
import java.io.FileInputStream


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var photoPickerLauncher: ActivityResultLauncher<String>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
        initViews()
    }

    private fun init() {
        photoPickerLauncher =
            registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
                //todo get file path
            }
    }

    private fun initViews() = with(binding) {
        button.setOnClickListener { openExcelFile() }
    }


    private fun openExcelFile() = photoPickerLauncher.launch(
        EXCEL_MIME
    )

    private fun readExcelFile(filepath: File) {
        // val inputStream = FileInputStream(filepath)
        //Instantiate Excel workbook using existing file:
        val xlWb = WorkbookFactory.create(filepath)

        //Row index specifies the row in the worksheet (starting at 0):
        val rowNumber = 0
        //Cell index specifies the column within the chosen row (starting at 0):
        val columnNumber = 0

        //Get reference to first sheet:
        val xlWs = xlWb.getSheetAt(0)
        Toast.makeText(
            this,
            xlWs.getRow(rowNumber).getCell(columnNumber).stringCellValue,
            Toast.LENGTH_SHORT
        ).show()
    }


}