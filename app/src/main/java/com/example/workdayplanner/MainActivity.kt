package com.example.workdayplanner

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.workdayplanner.ui.PlannerApp
import com.example.workdayplanner.ui.WorkdayPlannerTheme

class MainActivity : ComponentActivity() {
    private val viewModel: PlannerViewModel by viewModels()
    private var requestedTaskId by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestedTaskId = intent.getStringExtra(EXTRA_OPEN_TASK_ID)
        setContent {
            val state by viewModel.state.collectAsStateWithLifecycle()
            WorkdayPlannerTheme(darkMode = state.darkMode, accentStyle = state.accentStyle) {
                val notificationPermission = rememberLauncherForActivityResult(
                    ActivityResultContracts.RequestPermission()
                ) {}

                LaunchedEffect(Unit) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        notificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    PlannerApp(
                        viewModel = viewModel,
                        requestedTaskId = requestedTaskId,
                        onTaskRequestHandled = { requestedTaskId = null }
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        requestedTaskId = intent.getStringExtra(EXTRA_OPEN_TASK_ID)
    }

    companion object {
        const val EXTRA_OPEN_TASK_ID = "open_task_id"
    }
}
