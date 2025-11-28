package com.example.mhikeandroidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.mhikeandroidapp.data.AppDatabase
import com.example.mhikeandroidapp.data.hike.HikeRepository
import com.example.mhikeandroidapp.screens.hike.HikeListScreen
import com.example.mhikeandroidapp.screens.hike.mockHikes
import com.example.mhikeandroidapp.ui.theme.MhikeAndroidAppTheme
import com.example.mhikeandroidapp.viewmodel.HikeViewModel
import com.example.mhikeandroidapp.viewmodel.HikeViewModelFactory

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        // Khởi tạo database và repository
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "mhike-db"
        ).build()

        val repository = HikeRepository(db)
        val factory = HikeViewModelFactory(repository)

        setContent {
            MhikeAndroidAppTheme {
                val hikeViewModel: HikeViewModel = viewModel(factory = factory)
                val hikes by hikeViewModel.hikes.collectAsState(initial = emptyList())

                Scaffold { innerPadding ->
                    HikeListScreen(
                        modifier = Modifier.padding(innerPadding),
//                      hikes = hikes,
                        hikes = mockHikes, // dùng mock thay vì viewModel
                        onSearch = { query -> /* TODO: search */ },
                        onHikeClick = { hike -> /* TODO: navigate to detail */ }
                    )
                }
            }
        }
    }
}
