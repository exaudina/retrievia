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
import android.content.Context
import android.content.SharedPreferences
import android.preference.PreferenceManager
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.datastore.preferences.preferencesDataStoreFile
import androidx.lifecycle.LiveData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class MainActivity : AppCompatActivity() {

    companion object {
        const val NEW_VALUE_KEY_PREF = "NEW_VALUE_KEY_PREF"
    }
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

    val Context.dataStore : DataStore<Preferences> by preferencesDataStore(name = "settings")
    val valueInDataStore = intPreferencesKey(NEW_VALUE_KEY_PREF)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        storeDataIntoFile()
        val beveragesFromCache = readDataFromFile()

        val tvBeverageList = findViewById<TextView>(R.id.tv_beverage_list)
        tvBeverageList.text = beveragesFromCache.toString()

        checkStoragePermissionForRetrivia()

        saveValueToSharedPref()

        storeDataIntoExternalStorageWithPermission()
        readDataFromExternalStorageWithPermission()
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

    private fun saveValueToSharedPref(){
        val newValue = 10;
        PreferenceManager.getDefaultSharedPreferences(this)
            .edit()
            .putInt(NEW_VALUE_KEY_PREF, newValue)
            .commit()
    }

    private suspend fun saveValueToSharedPrefDataStore(){
        application.dataStore.edit {
            val currentValue = it[valueInDataStore] ?: 0
            it[valueInDataStore] = currentValue + 1
        }
    }

    val valueFromSharedPrefDataStore: Flow<Int> = application.dataStore.data.map {
        it[valueInDataStore] ?: 0
    }
}