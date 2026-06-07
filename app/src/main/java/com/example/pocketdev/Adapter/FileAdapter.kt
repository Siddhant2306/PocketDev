package com.example.pocketdev.Adapter
import com.example.pocketdev.Model.CodeFile

class FileAdapter(
    private val files: List<CodeFile>,
    private val onClick: (CodeFile) -> Unit
)