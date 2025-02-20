package com.exaudina.retrivia

import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import android.Manifest

class MainActivity : AppCompatActivity() {

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ){ isGranted ->
            if(isGranted){
                val rootView = findViewById<View>(android.R.id.content)
                Snackbar.make(
                    rootView,
                    "Permission is Granted",
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        storeDataIntoFile()
        val beveragesFromCache = readDataFromFile()

        val tvBeverageList = findViewById<TextView>(R.id.tv_beverage_list)

        tvBeverageList.text = beveragesFromCache.toString()


        checkStoragePermissionForRetrivia()
    }

    private fun storeDataIntoFile(){
        val beveragesInJson = Gson().toJson(listOfBeverages)

        val file = File(application.filesDir, "beverages.json")
        file.writeText(beveragesInJson)
    }

    private fun storeDataIntoExternalStorageWithPermission(){
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            val beveragesInJson = Gson().toJson(listOfBeverages)

            val file = File(application.getExternalFilesDir("beverages"), "beverages.json")
            file.writeText(beveragesInJson)
        } else {
            requestPermissionLauncher.launch(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }

    }
    private fun readDataFromFile(): List<Beverage>{
        val file = File(application.filesDir, "beverages.json")
        val dataInJson = if(file.exists()) file.readText() else null

        return if(dataInJson == null) {
            emptyList()
        } else {
            val listType = object : TypeToken<List<Beverage>>() {}.type
            Gson().fromJson(dataInJson, listType)
        }
    }

    private fun readDataFromExternalStorageWithPermission(): List<Beverage>? {
        if(ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED){
            val file = File(application.getExternalFilesDir("beverages"), "beverages.json")

            val dataInJson = if(file.exists()) file.readText() else null

            return if(dataInJson == null) {
                emptyList()
            } else {
                val listType = object : TypeToken<List<Beverage>>() {}.type
                Gson().fromJson(dataInJson, listType)
            }
        }else {
            requestPermissionLauncher.launch(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
        return null
    }

    private fun checkStoragePermissionForRetrivia(){
        if(ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) != PackageManager.PERMISSION_GRANTED){
            requestPermissionLauncher.launch(
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            )
        }
    }
}