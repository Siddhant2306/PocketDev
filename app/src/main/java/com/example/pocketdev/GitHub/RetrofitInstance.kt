package com.example.pocketdev.GitHub

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {

    private const val BASE_URL = "https://api.github.com/"

    val api: GitHubApi by lazy {

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(GitHubApi::class.java)
    }

    val authApi: GitHubAuthApi by lazy {

        Retrofit.Builder()
            .baseUrl("https://github.com/")
            .addConverterFactory(
                GsonConverterFactory.create()
            )
            .build()
            .create(GitHubAuthApi::class.java)
    }
}