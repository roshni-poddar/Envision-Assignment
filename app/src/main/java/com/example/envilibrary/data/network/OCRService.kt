package com.example.envilibrary.data.network

import com.example.envilibrary.data.model.OCRResponse
import okhttp3.MultipartBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface OCRService {
    @Multipart
    @POST("test/readDocument")
    fun uploadImage(@Part photo: MultipartBody.Part): Call<OCRResponse>
}
