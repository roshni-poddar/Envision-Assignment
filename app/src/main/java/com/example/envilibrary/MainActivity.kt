package com.example.envilibrary

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import com.example.envilibrary.navigation.NavGraph
import com.example.envilibrary.ui.theme.EnviLibraryTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EnviLibraryTheme {
                MainActivityContent()
            }
        }
    }
}

@Composable
fun MainActivityContent() {
    val navController = rememberNavController()
    var selectedTab by remember { mutableStateOf(1) } // Start with Library tab
    val tabs = listOf("CAPTURE", "LIBRARY")

    Scaffold(
        content = {
            Column {
                TabRow(selectedTabIndex = selectedTab) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = {
                                selectedTab = index
                                navController.navigate(title.toLowerCase())
                            },
                            text = { Text(title) },
                            modifier = Modifier.semantics { contentDescription = "$title Tab" }
                        )
                    }
                }
                NavGraph(navController = navController, onTabChange = { selectedTab = it })
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun MainActivityPreview() {
    EnviLibraryTheme {
        MainActivityContent()
    }
}
