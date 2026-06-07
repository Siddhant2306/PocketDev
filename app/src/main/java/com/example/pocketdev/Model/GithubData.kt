package com.example.pocketdev.Model

data class GitHubUser(
    val login: String,
    val name: String?,
    val avatar_url: String
)

data class GitHubRepository(
    val id: Long,
    val name: String,
    val full_name: String
)

data class GitHubTokenResponse(
    val access_token: String,
    val token_type: String,
    val scope: String
)

data class GitHubContentItem(
    val name: String,
    val path: String,
    val sha: String,
    val size: Long,
    val url: String,
    val html_url: String,
    val download_url: String?,
    val type: String // "file" or "dir"
)

data class GitHubFileContent(
    val name: String,
    val path: String,
    val size: Long,
    val encoding: String?,
    val content: String?
)