package com.example.mhikeandroidapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.room.Room
import com.example.mhikeandroidapp.data.AppDatabase
import com.example.mhikeandroidapp.data.hike.HikeRepository
import com.example.mhikeandroidapp.screens.hike.HikeListScreen
import com.example.mhikeandroidapp.ui.theme.MhikeAndroidAppTheme
import com.example.mhikeandroidapp.viewmodel.HikeViewModel
import com.example.mhikeandroidapp.viewmodel.HikeViewModelFactory
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.mhikeandroidapp.data.hike.HikeModel
import com.example.mhikeandroidapp.data.observation.ObservationRepository
import com.example.mhikeandroidapp.screens.hike.AddHikeScreen
import com.example.mhikeandroidapp.screens.hike.EditHikeScreen
import com.example.mhikeandroidapp.screens.hike.HikeDetailScreen
import com.example.mhikeandroidapp.ui.theme.PrimaryGreen
import com.example.mhikeandroidapp.viewmodel.ObservationViewModel
import com.google.firebase.FirebaseApp

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        installSplashScreen()

        FirebaseApp.initializeApp(this)

        // custom status bar
        window.statusBarColor = android.graphics.Color.parseColor("#FFFBFE") //background
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightStatusBars = true //icons

        // Khởi tạo database and repository
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java,
            "mhike-db"
        ).build()

        val hikeRepository = HikeRepository(db)
        val observationRepository = ObservationRepository(db)
        val hikeFactory = HikeViewModelFactory(hikeRepository, observationRepository)
        val observationFactory = ObservationViewModel.Factory(observationRepository)

        setContent {
            MhikeAndroidAppTheme {
                val navController = rememberNavController()

                // Hikes
                val hikeViewModel: HikeViewModel = viewModel(factory = hikeFactory)

                // Observations
                val observationViewModel: ObservationViewModel = viewModel(factory = observationFactory)


                NavHost(
                    navController = navController,
                    startDestination = "hike_list"
                ) {
                    composable("hike_list") {
                        Scaffold { innerPadding ->
                            HikeListScreen(
                                viewModel = hikeViewModel,
                                modifier = Modifier.padding(innerPadding),
                                navController = navController,
                                onSearch = { query -> /* TODO: search */ },
                                onHikeClick = { hike -> /* TODO: navigate to detail */ }
                            )
                        }
                    }

                    // add hike screen
                    composable("add_hike") {
                        AddHikeScreen(
                            onBack = { navController.popBackStack() },
                            onSave = { hike ->
                                hikeViewModel.addHike(hike)
                                navController.popBackStack()
                            }
                        )
                    }

                    // hike detail screen by id
                    composable("hike_detail/{hikeId}") { backStackEntry ->
                        val hikeId = backStackEntry.arguments?.getString("hikeId")?.toLongOrNull()

                        val hike by produceState<HikeModel?>(initialValue = null, hikeId) {
                            value = hikeId?.let { hikeViewModel.getHikeById(it) }
                        }

                        if (hike == null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = PrimaryGreen)
                            }
                        } else {
                            hike?.let {
                                HikeDetailScreen(
                                    hike = it,
                                    hikeViewModel = hikeViewModel,
                                    observationViewModel = observationViewModel, // observation
                                    onEdit = {
                                        navController.navigate("edit_hike/${it.id}")
                                    },
                                    onDelete = {
                                        hikeViewModel.deleteHike(it)
                                        navController.popBackStack()
                                    },
                                    onAddObservation = { /* TODO: add observation */ },
                                    onBack = { navController.popBackStack() }
                                )
                            }
                        }
                    }

                    //Edit Hike screen
                    composable("edit_hike/{hikeId}") { backStackEntry ->
                        val hikeId = backStackEntry.arguments?.getString("hikeId")?.toLongOrNull()

                        val hike by produceState<HikeModel?>(initialValue = null, hikeId) {
                            value = hikeId?.let { hikeViewModel.getHikeById(it) }
                        }

                        if (hike == null) {
                            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                CircularProgressIndicator(color = PrimaryGreen)
                            }
                        } else {
                            EditHikeScreen(
                                hike = hike!!,
                                onBack = { navController.popBackStack() },
                                onSave = { updated ->
                                    hikeViewModel.updateHike(updated)
                                    navController.popBackStack()
                                }
                            )
                        }
                    }
                }
            }
        }
    }

}

