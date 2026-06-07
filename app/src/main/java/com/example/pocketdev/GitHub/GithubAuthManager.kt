package com.example.pocketdev.GitHub

import android.content.Context
import android.content.Intent
import android.net.Uri
import com.example.pocketdev.Config.AppConfig

object GitHubAuthManager {

    fun login(context: Context) {

        val authUrl =
            "https://github.com/login/oauth/authorize" +
                    "?client_id=${AppConfig.GITHUB_CLIENT_ID}" +
                    "&scope=repo,user"

        val intent =
            Intent(
                Intent.ACTION_VIEW,
                Uri.parse(authUrl)
            )

        context.startActivity(intent)
    }
}