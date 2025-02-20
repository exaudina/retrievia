package com.exaudina.retrivia

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        storeDataIntoFile()
        val beveragesFromCache = readDataFromFile()

        val tvBeverageList = findViewById<TextView>(R.id.tv_beverage_list)

        tvBeverageList.text = beveragesFromCache.toString()

    }

    private fun storeDataIntoFile(){
        val beveragesInJson = Gson().toJson(listOfBeverages)

        val file = File(application.filesDir, "beverages.json")
        file.writeText(beveragesInJson)
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
}