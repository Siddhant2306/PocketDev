package com.example.pocketdev.Data
import com.example.pocketdev.Model.CodeFile

object DummyData {

    val files = listOf(
        CodeFile(
            "Player.cs",
            """
            public class Player
            {
                public int Health = 100;
            }
            """.trimIndent()
        ),

        CodeFile(
            "Enemy.cs",
            """
            public class Enemy
            {
                public int Damage = 20;
            }
            """.trimIndent()
        ),

        CodeFile(
            "GameManager.cs",
            """
            public class GameManager
            {
                public void StartGame()
                {
                }
            }
            """.trimIndent()
        )
    )
}