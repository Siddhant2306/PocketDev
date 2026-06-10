package com.example.pocketdev.ViewModel

import android.util.Log
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

import com.example.pocketdev.Config.AppConfig
import com.example.pocketdev.GitHub.GitHubRepositoryManager
import com.example.pocketdev.Model.GitHubRepository
import com.example.pocketdev.Model.GitHubUser
import com.example.pocketdev.Model.GitHubContentItem
import com.example.pocketdev.Model.Screen

class MainViewModel : ViewModel() {
    var accessToken by mutableStateOf("")
        private set

    var githubUser by mutableStateOf<GitHubUser?>(null)
        private set

    var repos by mutableStateOf<List<GitHubRepository>>(emptyList())
        private set

    var currentScreen by mutableStateOf(Screen.Login)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var loadingMessage by mutableStateOf("Loading...")
        private set

    var selectedRepository by mutableStateOf<GitHubRepository?>(null)
        private set

    var repositoryFiles by mutableStateOf<List<GitHubContentItem>>(emptyList())
        private set

    var selectedFile by mutableStateOf<GitHubContentItem?>(null)
        private set

    var selectedFolder by mutableStateOf<GitHubContentItem?>(null)
        private set
    var selectedFileContent by mutableStateOf("")
        private set
    var editorStatusMessage by mutableStateOf("")
        private set
    var canSaveFile by mutableStateOf(false)
        private set

    fun exchangeCodeForToken(
        code: String,
        onTokenReceived: (String) -> Unit = {}
    ) {
        isLoading = true
        loadingMessage = "Authenticating with GitHub..."
        viewModelScope.launch {
            try {
                val token = GitHubRepositoryManager.exchangeCodeForToken(
                    AppConfig.GITHUB_CLIENT_ID,
                    AppConfig.GITHUB_CLIENT_SECRET,
                    code
                )
                accessToken = token
                onTokenReceived(token)
                Log.d("POCKETDEV", "Token exchanged successfully")
                fetchRepositories()
            } catch (e: Exception) {
                Log.e("POCKETDEV", "Token exchange failed", e)
                isLoading = false
            }
        }
    }

    fun restoreSession(savedAccessToken: String) {
        if (savedAccessToken.isBlank() || accessToken == savedAccessToken) return

        accessToken = savedAccessToken
        fetchRepositories()
    }

    fun fetchRepositories() {
        isLoading = true
        loadingMessage = "Fetching repositories..."
        viewModelScope.launch {
            try {
                val (user, repositories) = GitHubRepositoryManager.loadGitHubData(accessToken)
                githubUser = user
                repos = repositories
                currentScreen = Screen.Repositories
                repositories.forEach { repo ->
                    Log.d("POCKETDEV", "Repository: ${repo.name}")
                }
            } catch (e: Exception) {
                Log.e("POCKETDEV", "GitHub Data Fetch Error", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun selectRepository(repo: GitHubRepository) {
        selectedRepository = repo
        isLoading = true
        loadingMessage = "Loading repository files..."
        viewModelScope.launch {
            try {
                val parts = repo.full_name.split("/")
                val owner = parts[0]
                val name = parts[1]
                val contents = GitHubRepositoryManager.getRepositoryContentsRecursively(
                    accessToken = accessToken,
                    owner = owner,
                    repo = name,
                    path = ""
                )

                repositoryFiles = contents
                selectedFolder = null

                val firstFile = repositoryFiles.firstOrNull { it.type == "file" }
                if (firstFile != null) {
                    selectedFile = firstFile
                    fetchFileContent(firstFile)
                } else {
                    selectedFile = null
                    selectedFileContent = "This repository has no files."
                }
                currentScreen = Screen.Editor
            } catch (e: Exception) {
                Log.e("POCKETDEV", "Failed to fetch repository files", e)
            } finally {
                isLoading = false
            }
        }
    }

    fun selectRepositoryItem(item: GitHubContentItem) {
        if (item.type == "dir") {
            selectedFolder = item
            selectedFile = null
            selectedFileContent = item.path
            editorStatusMessage = ""
            canSaveFile = false
        } else {
            selectedFolder = null
            fetchFileContent(item)
        }
    }

    fun updateSelectedFileContent(content: String) {
        selectedFileContent = content
        editorStatusMessage = ""
    }

    fun fetchFileContent(file: GitHubContentItem) {
        selectedFile = file
        selectedFileContent = "Loading file content..."
        editorStatusMessage = ""
        canSaveFile = false
        viewModelScope.launch {
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
                editorStatusMessage = ""
                canSaveFile = true
            } catch (e: Exception) {
                Log.e("POCKETDEV", "Failed to load file content", e)
                selectedFileContent = "Error loading content: ${e.message}"
                canSaveFile = false
            }
        }
    }

    fun saveSelectedFileContent() {
        val file = selectedFile ?: return
        val repo = selectedRepository ?: return

        isLoading = true
        loadingMessage = "Saving file..."
        editorStatusMessage = ""
        canSaveFile = false

        viewModelScope.launch {
            try {
                val parts = repo.full_name.split("/")
                val owner = parts[0]
                val name = parts[1]
                val updatedFile = GitHubRepositoryManager.updateFileContent(
                    accessToken = accessToken,
                    owner = owner,
                    repo = name,
                    path = file.path,
                    sha = file.sha,
                    content = selectedFileContent
                )

                repositoryFiles = repositoryFiles.map { item ->
                    if (item.path == updatedFile.path) updatedFile else item
                }
                selectedFile = updatedFile
                editorStatusMessage = "Saved"
            } catch (e: Exception) {
                Log.e("POCKETDEV", "Failed to save file", e)
                editorStatusMessage = "Save failed: ${e.message ?: "unknown error"}"
            } finally {
                canSaveFile = selectedFile != null
                isLoading = false
            }
        }
    }

    fun logout() {
        accessToken = ""
        githubUser = null
        repos = emptyList()
        selectedRepository = null
        repositoryFiles = emptyList()
        selectedFile = null
        selectedFileContent = ""
        editorStatusMessage = ""
        canSaveFile = false
        currentScreen = Screen.Login
    }

    fun navigateToRepositories() {
        currentScreen = Screen.Repositories
    }
}
