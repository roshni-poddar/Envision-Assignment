package com.example.envilibrary.data.repository

import android.util.Log
import com.example.envilibrary.data.model.OCRResponse
import com.example.envilibrary.data.network.OCRService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.Response
import java.io.File
import javax.inject.Inject

sealed class Result<out T> {
    data class Success<out T>(val data: T) : Result<T>()
    data class NoTextFound(val message: String) : Result<Nothing>()
    data class Error(val message: String) : Result<Nothing>()
    object Loading : Result<Nothing>()
}

class OCRRepository @Inject constructor(private val ocrService: OCRService) {

    suspend fun uploadImage(file: File): Result<String> {
        return withContext(Dispatchers.IO) {
            try {
                val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
                val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)
                val response: Response<OCRResponse> = ocrService.uploadImage(body).execute()

                if (response.isSuccessful) {
                    val ocrResponse = response.body()

                    if (ocrResponse != null) {
                        if (ocrResponse.message == "No text found") {
                            Log.d("OCRRepository", "No text found in the image")
                            Result.NoTextFound("No text found in the image")
                        } else if (ocrResponse.response != null) {
                            val concatenatedText = ocrResponse.response.paragraphs.joinToString(" ") { it.paragraph }
                            Log.d("OCRRepository", "Upload successful: $concatenatedText")
                            Result.Success(concatenatedText)
                        } else {
                            val errorMessage = "No valid response received"
                            Log.e("OCRRepository", "Upload failed: $errorMessage")
                            Result.Error("Upload failed: $errorMessage")
                        }
                    } else {
                        val errorMessage = response.errorBody()?.string() ?: "Unknown error"
                        Log.e("OCRRepository", "Upload failed: $errorMessage")
                        Result.Error("Upload failed: $errorMessage")
                    }
                } else {
                    val errorMessage = response.errorBody()?.string() ?: response.message()
                    Log.e("OCRRepository", "Upload failed: $errorMessage")
                    Result.Error("Upload failed: $errorMessage")
                }
            } catch (e: Exception) {
                Log.e("OCRRepository", "Upload failed: ${e.message}")
                Result.Error("Upload failed: ${e.message}")
            }
        }
    }
}
