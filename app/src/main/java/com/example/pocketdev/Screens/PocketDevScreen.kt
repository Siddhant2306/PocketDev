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
    selectedContent: String,
    onFileClick: (GitHubContentItem) -> Unit,
    onBackClick: () -> Unit
) {
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
                text = "FILES",
                color = MaterialTheme.colorScheme.onSurface,
                style = MaterialTheme.typography.labelLarge
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (files.isEmpty()) {
                Text(
                    text = "No root files",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            } else {
                LazyColumn {
                    items(files) { file ->
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(
                                    if (file == selectedFile)
                                        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
                                    else
                                        Color.Transparent
                                )
                                .clickable {
                                    onFileClick(file)
                                }
                                .padding(8.dp)
                        ) {
                            Text(
                                text = file.name,
                                color =
                                    if (file == selectedFile)
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
                text = selectedFile?.name ?: "No file selected",
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
