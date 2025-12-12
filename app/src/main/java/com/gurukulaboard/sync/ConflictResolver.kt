package com.gurukulaboard.sync

import android.app.AlertDialog
import android.content.Context
import com.gurukulaboard.models.Question
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ConflictResolver @Inject constructor() {
    
    fun showConflictDialog(
        context: Context,
        conflict: Conflict,
        onResolve: (Question) -> Unit
    ) {
        AlertDialog.Builder(context)
            .setTitle("Conflict Detected")
            .setMessage("This question was modified by another user. Choose which version to keep:")
            .setPositiveButton("Keep Local") { _, _ ->
                onResolve(conflict.localVersion)
            }
            .setNegativeButton("Keep Server") { _, _ ->
                onResolve(conflict.serverVersion)
            }
            .setNeutralButton("Merge", null) // Implement merge logic
            .show()
    }
}

