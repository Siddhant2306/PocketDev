package com.example.pocketdev.GitHub

import com.example.pocketdev.Model.GitHubRepository
import com.example.pocketdev.Model.GitHubUser
import com.example.pocketdev.Model.GitHubTokenResponse
import com.example.pocketdev.Model.GitHubContentItem
import com.example.pocketdev.Model.GitHubFileContent
import retrofit2.http.*

interface GitHubAuthApi {
    @FormUrlEncoded
    @POST("login/oauth/access_token")
    @Headers("Accept: application/json")
    suspend fun exchangeCodeForToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String
    ): GitHubTokenResponse
}

interface GitHubApi {

    @GET("user")
    suspend fun getUser(
        @Header("Authorization")
        token: String
    ): GitHubUser

    @GET("user/repos")
    suspend fun getRepositories(
        @Header("Authorization")
        token: String
    ): List<GitHubRepository>

    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getRepositoryContents(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path(value = "path", encoded = true) path: String
    ): List<GitHubContentItem>

    @GET("repos/{owner}/{repo}/contents/{path}")
    suspend fun getFileContent(
        @Header("Authorization") token: String,
        @Path("owner") owner: String,
        @Path("repo") repo: String,
        @Path(value = "path", encoded = true) path: String
    ): GitHubFileContent
}