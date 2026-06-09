package com.example.pocketdev.GitHub

import android.util.Log
import com.example.pocketdev.Model.GitHubRepository
import com.example.pocketdev.Model.GitHubUser
import com.example.pocketdev.Model.GitHubContentItem
import com.example.pocketdev.Model.GitHubFileContent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

object GitHubRepositoryManager {
    private const val REPOSITORIES_PER_PAGE = 100

    suspend fun exchangeCodeForToken(
        clientId: String,
        clientSecret: String,
        code: String
    ): String = withContext(Dispatchers.IO) {

        RetrofitInstance.authApi.exchangeCodeForToken(
            clientId,
            clientSecret,
            code
        ).access_token
    }

    suspend fun getUser(
        accessToken: String
    ): GitHubUser = withContext(Dispatchers.IO) {

        RetrofitInstance.api.getUser(
            "Bearer $accessToken"
        )
    }

    suspend fun getRepositories(
        accessToken: String
    ): List<GitHubRepository> = withContext(Dispatchers.IO) {

        val repositories = mutableListOf<GitHubRepository>()
        var page = 1

        while (true) {
            val pageRepositories = RetrofitInstance.api.getRepositories(
                token = "Bearer $accessToken",
                perPage = REPOSITORIES_PER_PAGE,
                page = page
            )

            repositories.addAll(pageRepositories)

            if (pageRepositories.size < REPOSITORIES_PER_PAGE) {
                break
            }

            page++
        }

        repositories
    }

    suspend fun getRepositoryContents(
        accessToken: String,
        owner: String,
        repo: String,
        path: String = ""
    ): List<GitHubContentItem> = withContext(Dispatchers.IO) {

        RetrofitInstance.api.getRepositoryContents(
            "Bearer $accessToken",
            owner,
            repo,
            path
        )
    }

    suspend fun getRepositoryContentsRecursively(
        accessToken: String,
        owner: String,
        repo: String,
        path: String = ""
    ): List<GitHubContentItem> = withContext(Dispatchers.IO) {

        val allItems = mutableListOf<GitHubContentItem>()

        suspend fun traverse(currentPath: String) {

            val items = getRepositoryContents(
                accessToken = accessToken,
                owner = owner,
                repo = repo,
                path = currentPath
            )

            val sortedItems = items.sortedWith(
                compareBy<GitHubContentItem> { if (it.type == "dir") 0 else 1 }
                    .thenBy { it.name.lowercase() }
            )

            for (item in sortedItems) {

                allItems.add(item)

                if (item.type == "dir") {

                    traverse(item.path)
                }
            }
        }

        traverse(path)

        allItems
    }

    suspend fun getFileContent(
        accessToken: String,
        owner: String,
        repo: String,
        path: String
    ): GitHubFileContent = withContext(Dispatchers.IO) {

        RetrofitInstance.api.getFileContent(
            "Bearer $accessToken",
            owner,
            repo,
            path
        )
    }

    suspend fun loadGitHubData(
        accessToken: String
    ): Pair<GitHubUser, List<GitHubRepository>> {

        return withContext(Dispatchers.IO) {

            try {

                val user =
                    getUser(accessToken)

                val repositories =
                    getRepositories(accessToken)

                Pair(user, repositories)

            } catch (e: Exception) {

                Log.e(
                    "POCKETDEV",
                    "Failed loading GitHub data",
                    e
                )

                throw e
            }
        }
    }
}
