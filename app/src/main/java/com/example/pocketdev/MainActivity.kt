package com.example.pocketdev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

import com.example.pocketdev.Config.AppConfig
import com.example.pocketdev.GitHub.GitHubRepositoryManager
import com.example.pocketdev.GitHub.GitHubAuthManager
import com.example.pocketdev.Model.GitHubRepository
import com.example.pocketdev.Model.GitHubUser
import com.example.pocketdev.Model.GitHubContentItem
import com.example.pocketdev.ui.theme.PocketDevTheme
import com.example.pocketdev.Screens.LoginScreen
import com.example.pocketdev.Screens.RepositoryScreen

enum class Screen {
    Login,
    Repositories,
    Editor
}

class MainActivity : ComponentActivity() {

    private var accessToken by mutableStateOf("")
    private var githubUser by mutableStateOf<GitHubUser?>(null)
    private var repos by mutableStateOf<List<GitHubRepository>>(emptyList())
    private var currentScreen by mutableStateOf(Screen.Login)
    private var isLoading by mutableStateOf(false)
    private var loadingMessage by mutableStateOf("Loading...")

    private var selectedRepository by mutableStateOf<GitHubRepository?>(null)
    private var repositoryFiles by mutableStateOf<List<GitHubContentItem>>(emptyList())
    private var selectedFile by mutableStateOf<GitHubContentItem?>(null)
    private var selectedFileContent by mutableStateOf("")

    private fun exchangeCodeForToken(code: String) {
        isLoading = true
        loadingMessage = "Authenticating with GitHub..."
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val token = GitHubRepositoryManager.exchangeCodeForToken(
                    AppConfig.GITHUB_CLIENT_ID,
                    AppConfig.GITHUB_CLIENT_SECRET,
                    code
                )
                accessToken = token
                Log.d("POCKETDEV", "Token exchanged successfully")
                fetchRepositories()
            } catch (e: Exception) {
                Log.e("POCKETDEV", "Token exchange failed", e)
                isLoading = false
            }
        }
    }

    private fun fetchRepositories() {
        isLoading = true
        loadingMessage = "Fetching repositories..."
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val (user, repositories) = GitHubRepositoryManager.loadGitHubData(accessToken)
                githubUser = user
                repos = repositories
                currentScreen = Screen.Repositories
            } catch (e: Exception) {
                Log.e("POCKETDEV", "GitHub Data Fetch Error", e)
            } finally {
                isLoading = false
            }
        }
    }

    private fun selectRepository(repo: GitHubRepository) {
        selectedRepository = repo
        isLoading = true
        loadingMessage = "Loading repository files..."
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val parts = repo.full_name.split("/")
                val owner = parts[0]
                val name = parts[1]
                val contents = GitHubRepositoryManager.getRepositoryContents(accessToken, owner, name, "")
                
                // Show files only
                repositoryFiles = contents.filter { it.type == "file" }
                
                if (repositoryFiles.isNotEmpty()) {
                    val firstFile = repositoryFiles.first()
                    selectedFile = firstFile
                    fetchFileContent(firstFile)
                } else {
                    selectedFile = null
                    selectedFileContent = "This repository has no files at the root level."
                }
                currentScreen = Screen.Editor
            } catch (e: Exception) {
                Log.e("POCKETDEV", "Failed to fetch repository files", e)
            } finally {
                isLoading = false
            }
        }
    }

    private fun fetchFileContent(file: GitHubContentItem) {
        selectedFile = file
        selectedFileContent = "Loading file content..."
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val repo = selectedRepository ?: return@launch
                val parts = repo.full_name.split("/")
                val owner = parts[0]
                val name = parts[1]
                val fileContentResponse = GitHubRepositoryManager.getFileContent(
                    accessToken,
                    owner,
                    name,
                    file.path
                )
                
                val rawContent = fileContentResponse.content ?: ""
                val decoded = if (fileContentResponse.encoding == "base64") {
                    try {
                        val cleanBase64 = rawContent.replace("\n", "").replace("\r", "")
                        String(android.util.Base64.decode(cleanBase64, android.util.Base64.DEFAULT))
                    } catch (e: Exception) {
                        "Error decoding base64 content: ${e.message}"
                    }
                } else {
                    rawContent
                }
                selectedFileContent = decoded
            } catch (e: Exception) {
                Log.e("POCKETDEV", "Failed to load file content", e)
                selectedFileContent = "Error loading content: ${e.message}"
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Parse deep link code
        val code = intent?.data?.getQueryParameter("code")
        if (code != null) {
            Log.d("POCKETDEV", "Code query parameter found: $code")
            exchangeCodeForToken(code)
        }

        setContent {
            PocketDevTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (currentScreen) {
                            Screen.Login -> {
                                LoginScreen(
                                    onGithubLoginClick = {
                                        GitHubAuthManager.login(this@MainActivity)
                                    }
                                )
                            }
                            Screen.Repositories -> {
                                RepositoryScreen(
                                    user = githubUser,
                                    repositories = repos,
                                    onRepositorySelected = { repo ->
                                        selectRepository(repo)
                                    },
                                    onLogout = {
                                        accessToken = ""
                                        githubUser = null
                                        repos = emptyList()
                                        currentScreen = Screen.Login
                                    }
                                )
                            }
                            Screen.Editor -> {
                                PocketDevScreen(
                                    repoName = selectedRepository?.name ?: "Editor",
                                    files = repositoryFiles,
                                    selectedFile = selectedFile,
                                    selectedContent = selectedFileContent,
                                    onFileClick = { file ->
                                        fetchFileContent(file)
                                    },
                                    onBackClick = {
                                        currentScreen = Screen.Repositories
                                    }
                                )
                            }
                        }

                        if (isLoading) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color.Black.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Card(
                                    colors = CardDefaults.cardColors(
                                        containerColor = MaterialTheme.colorScheme.surface
                                    ),
                                    shape = RoundedCornerShape(12.dp),
                                    elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
                                ) {
                                    Column(
                                        modifier = Modifier.padding(24.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                                        Spacer(modifier = Modifier.height(16.dp))
                                        Text(
                                            text = loadingMessage,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

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