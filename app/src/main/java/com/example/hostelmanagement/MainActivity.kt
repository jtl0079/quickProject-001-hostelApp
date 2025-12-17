package com.example.hostelmanagement

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.hostelmanagement.booking.App
import com.example.hostelmanagement.booking.bookingHistory.BookingHistoryScreen
import com.example.hostelmanagement.booking.bookingHistory.BookingHistoryViewModel
import com.example.hostelmanagement.booking.bookingHostel.BookingHostelScreen
import com.example.hostelmanagement.booking.bookingRoom.BookingRoomScreen
import com.example.hostelmanagement.booking.bookingRoom.BookingRoomViewModel
import com.example.hostelmanagement.booking.tenantInformation.TenantInformationScreen
import com.example.hostelmanagement.maintenance.MaintenanceRecordsScreen
import com.example.hostelmanagement.maintenance.ItemViewModel
import com.example.hostelmanagement.maintenance.MaintenanceRequestScreen
import com.example.hostelmanagement.maintenance.MaintenanceViewModel
import com.example.hostelmanagement.report.infrastructure.repository.HostelReportRepositoryImpl
import com.example.hostelmanagement.report.presentation.ui.HostelReportScreen
import com.example.hostelmanagement.report.presentation.viewmodel.HostelReportViewModel
import com.example.hostelmanagement.report.presentation.viewmodel.HostelReportViewModelFactory
import com.example.hostelmanagement.report.usecase.GetReportUseCase
import com.example.hostelmanagement.report.usecase.SaveReportUseCase
import com.example.hostelmanagement.ui.theme.HostelManagementTheme
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth

class MainActivity : ComponentActivity() {

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Firebase
        FirebaseApp.initializeApp(this)
        enableEdgeToEdge()
        auth = Firebase.auth

        setContent {
            HostelManagementTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val itemViewModel: ItemViewModel = viewModel()
                    val maintenanceViewModel: MaintenanceViewModel = viewModel()
                    val navController = rememberNavController()
                    val startDestination = if (auth.currentUser != null) {
                        "home"
                    } else {
                        "start"
                    }





                    NavHost(
                        navController = navController,
                        startDestination = startDestination
                    ) {
                        composable("start") {
                            StartPage (
                                onNavigateToRegister = { navController.navigate("register") },
                                onNavigateToLogin = { navController.navigate("login") },
                            )
                        }
                        composable("register") {
                            Registration(
                                onNavigateToLogin = { navController.navigate("login") },
                                onBack = { navController.navigate("start") },
                            )
                        }
                        composable("login") {
                            LoginScreen(
                                onNavigateToForgotPassword = { navController.navigate("forgot") },
                                onLoginSuccess = { navController.navigate("home") },
                                onNavigateToRegister = { navController.navigate("register") },
                                onBack = { navController.navigate("start") },
                            )
                        }

                        composable("forgot") {
                            ForgotPage (
                                onResetNavigate = { docId -> navController.navigate("reset/$docId") },
                                onBack = { navController.navigate("login") }
                            )
                        }
                        composable(
                            "reset/{docId}",
                            arguments = listOf(navArgument("docId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val docId = backStackEntry.arguments?.getString("docId") ?: ""
                            ResetPage(
                                docId = docId,
                                onNavigateToLogin = { navController.navigate("login") },
                                onBack = { navController.popBackStack() }
                            )
                        }

                        composable("home") {
                            HomePage(
                                onHome = {navController.navigate("home")},
                                onAddItem = {navController.navigate("hostel")},
                                onBooking = {navController.navigate("booking_hostel")},
                                onReport = {navController.navigate("hostel_report")},
                                onProfile = { navController.navigate("profile") },
                                onOpenRooms={ hostelId -> navController.navigate("rooms/$hostelId")},
                                maintenanceViewModel = maintenanceViewModel
                            )
                        }
                        composable("hostel"){
                            HostelPage(
                                onCancel = {navController.navigate("home")},
                                maintenanceViewModel = maintenanceViewModel

                            )
                        }

                        composable("rooms/{hostelId}") { backStackEntry ->
                            val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                            RoomListPage(
                                hostelId = hostelId,
                                onBack = {navController.popBackStack()},//
                                onAddRoom = { navController.navigate("addRoom/$hostelId") },
                                onRoomSelected = {room -> navController.navigate("editRoom/${hostelId}/${room.roomId}") }
                            )
                        }

                        composable("addRoom/{hostelId}") { backStackEntry ->
                            val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                            RoomPage(
                                hostelId = hostelId,
                                onDone = {navController.popBackStack()}
                            )
                        }

                        composable ("editRoom/{hostelId}/{roomId}") { backStackEntry ->
                            val hostelId = backStackEntry.arguments?.getString("hostelId") ?: ""
                            val roomId = backStackEntry.arguments?.getString("roomId") ?: ""
                            val viewModel: RoomViewModel= viewModel()
                            val rooms=viewModel.roomsState.collectAsState().value
                            val room=rooms.find { it.roomId==roomId }

                            if (room==null){
                                LaunchedEffect(hostelId) {
                                    viewModel.loadRoomsOnce(hostelId)
                                }
                                Box(
                                    modifier = Modifier.fillMaxSize(),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }else{
                                RoomEditPage(
                                    hostelId = hostelId,
                                    room = room,
                                    onDone = { navController.popBackStack() },
                                    onMaintenanceRequest = { navController.navigate("maintenanceRequest") },
                                    onMaintenanceRecords = { navController.navigate("maintenanceHistory") },
                                    maintenanceViewModel = maintenanceViewModel
                                )
                            }
                        }

                        composable("profile") {
                            ProfilePage(
                                onLogout = {
                                    auth.signOut()
                                    navController.navigate("start") {
                                        popUpTo("home") { inclusive = true}
                                    } },
                                onHome = {navController.navigate("home")},
                                onBooking = {navController.navigate("booking_hostel")},
                                onReport = { navController.navigate("hostel_report") },
                                onProfile = { navController.navigate("profile") },
                                maintenanceViewModel = maintenanceViewModel
                            )
                        }

                        composable("maintenanceRequest") {
                            MaintenanceRequestScreen(itemViewModel, maintenanceViewModel, onDone = {navController.popBackStack()})
                        }
                        composable("maintenanceHistory") {
                            MaintenanceRecordsScreen(maintenanceViewModel, navController, onDone = {navController.popBackStack()})

                        }

                        composable("booking_hostel") {
                            BookingHostelScreen(navController)
                        }
                        composable("booking_history") {
                            val repository = (application as App).bookingRepository
                            val viewModel: BookingHistoryViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer {
                                        BookingHistoryViewModel(repository)
                                    }
                                }
                            )
                            BookingHistoryScreen(navController,viewModel)
                        }
                        composable("booking_room/{hostelId}") { backStackEntry ->
                            val name = backStackEntry.arguments?.getString("hostelId")?: ""
                            val repository = (application as App).bookingRepository
                            val viewModel: BookingRoomViewModel = viewModel(
                                factory = viewModelFactory {
                                    initializer {
                                        BookingRoomViewModel(repository = repository)
                                    }
                                }
                            )
                            BookingRoomScreen(name,navController,viewModel)
                        }

                        composable("tenant_Information/{hostelId}/{roomId}/{sem}/{year}/{month}") { backStackEntry ->
                            val hostelId = backStackEntry.arguments?.getString("hostelId")?: ""
                            val roomId = backStackEntry.arguments?.getString("roomId")?: ""
                            val sem = backStackEntry.arguments?.getString("sem")?: ""
                            val year = backStackEntry.arguments?.getString("year")?: ""
                            val month = backStackEntry.arguments?.getString("month")?: ""
                            TenantInformationScreen(hostelId,roomId,sem,year,month,navController)
                        }

                        composable("hostel_report"){

                            val bookingRepository = (application as App).bookingRepository
                            val HostelReportRepositoryImpl = HostelReportRepositoryImpl(bookingRepo = bookingRepository)

                            val getReportUseCase = GetReportUseCase(HostelReportRepositoryImpl)
                            val saveReportUseCase = SaveReportUseCase(HostelReportRepositoryImpl)


                            val viewModel: HostelReportViewModel = viewModel(
                                factory = HostelReportViewModelFactory(getReportUseCase, saveReportUseCase)
                            )


                            HostelReportScreen(
                                navController = navController,
                                viewModel = viewModel
                            )
                        }

                    }
                }
            }
        }
    }
}