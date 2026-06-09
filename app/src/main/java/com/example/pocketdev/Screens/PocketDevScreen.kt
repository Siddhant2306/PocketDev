package com.example.pocketdev.Screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.pocketdev.Model.GitHubContentItem

@Composable
fun PocketDevScreen(
    repoName: String,
    files: List<GitHubContentItem>,
    selectedFile: GitHubContentItem?,
    selectedFolder: GitHubContentItem?,
    selectedContent: String,
    onItemClick: (GitHubContentItem) -> Unit,
    onBackClick: () -> Unit
) {
    val expandedFolders = remember(repoName, files) {
        mutableStateMapOf<String, Boolean>()
    }
    val selectedPath = selectedFile?.path ?: selectedFolder?.path
    LaunchedEffect(expandedFolders, selectedPath) {
        selectedPath?.let { path ->
            parentFolderPaths(path).forEach { parentPath ->
                expandedFolders[parentPath] = true
            }
        }
    }
    val visibleFiles = files.filter { item ->
        parentFolderPaths(item.path).all { parentPath ->
            expandedFolders[parentPath] == true
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // File Explorer
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.4f)
                .background(MaterialTheme.colorScheme.surface)
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EXPLORER",
                    color = MaterialTheme.colorScheme.onSurface,
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.weight(1f)
                )
                
                Text(
                    text = "◀ Repos",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier
                        .clickable { onBackClick() }
                        .padding(4.dp)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "▼ $repoName",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "TREE",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (files.isEmpty()) {
                Text(
                    text = "No files",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                LazyColumn {
                    items(visibleFiles, key = { item -> item.path }) { item ->
                        val isDirectory = item.type == "dir"
                        val isExpanded = expandedFolders[item.path] == true
                        val isSelected = item == selectedFile || item == selectedFolder
                        val depth = item.path.count { it == '/' }

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (isSelected)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else
                                        Color.Transparent
                                )
                                .clickable {
                                    if (isDirectory) {
                                        expandedFolders[item.path] = !isExpanded
                                    }
                                    onItemClick(item)
                                }
                                .padding(
                                    start = (depth * 12).dp,
                                    top = 6.dp,
                                    end = 8.dp,
                                    bottom = 6.dp
                                ),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (isDirectory) {
                                    if (isExpanded) "▾" else "▸"
                                } else {
                                    ""
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.width(18.dp)
                            )
                            Text(
                                text = item.name,
                                color =
                                    if (isSelected)
                                        MaterialTheme.colorScheme.primary
                                    else
                                        MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }

        // Vertical Divider
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(1.dp)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.25f))
        )

        // Editor Area
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.6f)
                .padding(12.dp)
        ) {
            Text(
                text = selectedFile?.name ?: selectedFolder?.name ?: "No file selected",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            val scrollState = rememberScrollState()
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .verticalScroll(scrollState)
                    .padding(12.dp)
            ) {
                Text(
                    text = selectedContent,
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    }
}

private fun parentFolderPaths(path: String): List<String> {
    val parts = path.split("/")
    if (parts.size <= 1) return emptyList()

    return (1 until parts.size).map { index ->
        parts.take(index).joinToString("/")
    }
}
