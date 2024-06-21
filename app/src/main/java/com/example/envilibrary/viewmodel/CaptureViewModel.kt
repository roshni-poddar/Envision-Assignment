package com.example.envilibrary.viewmodel

import android.content.Context
import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.envilibrary.data.repository.OCRRepository
import com.example.envilibrary.data.repository.Result
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val ocrRepository: OCRRepository
) : ViewModel() {

    var ocrResponse = mutableStateOf<Result<String>>(Result.Loading)
        private set

    var textSaved = mutableStateOf(false)
        private set

    fun uploadImage(file: File) {
        Log.d("CaptureViewModel", "Uploading image $file")
        ocrResponse.value = Result.Loading
        viewModelScope.launch {
            val result = ocrRepository.uploadImage(file)
            Log.d("CaptureViewModel", "Received response value $result")
            ocrResponse.value = result
        }
    }

    fun saveToLibrary(context: Context, text: String) {
        val sdf = SimpleDateFormat("MM-dd-yy hh.mm a", Locale.getDefault())
        val currentDateAndTime = sdf.format(Date())
        Log.d("CaptureViewModel", "Saving text to file $currentDateAndTime")
        val fileName = "$currentDateAndTime.txt"

        val file = File(context.filesDir, fileName)
        file.parentFile?.mkdirs() // Create parent directories if not exist
        file.writeText(text)
        textSaved.value = true
    }
}
