package com.example.envilibrary.data.model

data class OCRResponse(
    val response: Response?,
    val message: String?
)

data class Response(
    val paragraphs: List<Paragraph>
)

data class Paragraph(
    val paragraph: String,
    val language: String
)
