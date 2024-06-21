package com.example.envilibrary.screens

import android.Manifest
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun LibraryScreen(navController: NavController, onTabChange: () -> Unit) {
    val context = LocalContext.current
    val files = context.filesDir.listFiles()?.map { it.name } ?: emptyList()
    var selectedFileContent by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        onTabChange()
    }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Library") })
        },
        content = {
            Column {
                LazyColumn {
                    items(files) { fileName ->
                        val formattedDate = fileName.removeSuffix(".txt").parseFileNameToReadableDate()
                        Text(
                            fileName,
                            modifier = Modifier
                                .padding(16.dp)
                                .clickable {
                                    selectedFileContent = File(context.filesDir, fileName).readText()
                                }
                                .semantics { contentDescription = "$formattedDate" }
                        )
                    }
                }
                selectedFileContent?.let {
                    Divider(modifier = Modifier.padding(vertical = 8.dp))
                    Text(
                        text = it,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    )
}

fun String.parseFileNameToReadableDate(): String {
    val sdf = SimpleDateFormat("MM-dd-yy hh.mm a", Locale.getDefault())
    val date = sdf.parse(this)
    val outputFormat = SimpleDateFormat("d'th' MMMM yyyy 'at' h:mma", Locale.getDefault())
    return outputFormat.format(date)
}
