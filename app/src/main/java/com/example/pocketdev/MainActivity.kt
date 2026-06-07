package com.example.pocketdev

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import android.util.Log

import com.example.pocketdev.GitHub.GitHubAuthManager
import com.example.pocketdev.Model.Screen
import com.example.pocketdev.Screens.LoginScreen
import com.example.pocketdev.Screens.RepositoryScreen
import com.example.pocketdev.Screens.PocketDevScreen
import com.example.pocketdev.ViewModel.MainViewModel
import com.example.pocketdev.ui.theme.PocketDevTheme

class MainActivity : ComponentActivity() {

    private val viewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Parse deep link code
        val code = intent?.data?.getQueryParameter("code")
        if (code != null) {
            Log.d("POCKETDEV", "Code query parameter found: $code")
            viewModel.exchangeCodeForToken(code)
        }

        setContent {
            PocketDevTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        when (viewModel.currentScreen) {
                            Screen.Login -> {
                                LoginScreen(
                                    onGithubLoginClick = {
                                        GitHubAuthManager.login(this@MainActivity)
                                    }
                                )
                            }
                            Screen.Repositories -> {
                                RepositoryScreen(
                                    user = viewModel.githubUser,
                                    repositories = viewModel.repos,
                                    onRepositorySelected = { repo ->
                                        viewModel.selectRepository(repo)
                                    },
                                    onLogout = {
                                        viewModel.logout()
                                    }
                                )
                            }
                            Screen.Editor -> {
                                PocketDevScreen(
                                    repoName = viewModel.selectedRepository?.name ?: "Editor",
                                    files = viewModel.repositoryFiles,
                                    selectedFile = viewModel.selectedFile,
                                    selectedContent = viewModel.selectedFileContent,
                                    onFileClick = { file ->
                                        viewModel.fetchFileContent(file)
                                    },
                                    onBackClick = {
                                        viewModel.navigateToRepositories()
                                    }
                                )
                            }
                        }

                        if (viewModel.isLoading) {
                            LoadingOverlay(message = viewModel.loadingMessage)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun LoadingOverlay(message: String) {
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
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}