package com.example.handyhub

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
import com.example.handyhub.ui.theme.HandyHubTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query

// ---------------------- DATA MODELS ----------------------
data class ServiceProvider(
    val id: String = "",
    val name: String = "",
    val serviceType: String = "",
    val rating: Double = 0.0,
    val city: String = "",
    val phoneNumber: String = ""
)

data class Booking(
    val id: String = "",
    val providerName: String = "",
    val providerServiceType: String = "",
    val providerCity: String = "",
    val userId: String = "",
    val timestamp: Long = 0L
)

// ---------------------- SAMPLE PROVIDERS ----------------------
private val sampleProviders = listOf(
    ServiceProvider("1", "Clean & Shine Services", "Cleaner", 4.5, "Middlesbrough", "07123456789"),
    ServiceProvider("2", "SparkPro Electricians", "Electrician", 4.8, "Newcastle", "07111111111"),
    ServiceProvider("3", "QuickFix Plumbing", "Plumber", 4.3, "Leeds", "07222222222"),
    ServiceProvider("4", "HomeCare Cleaning", "Cleaner", 4.1, "York", "07333333333"),
    ServiceProvider("5", "PowerGrid Electric Solutions", "Electrician", 4.7, "Manchester", "07444444444")
)

// ---------------------- ACTIVITY ----------------------
class DashboardActivity : ComponentActivity() {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HandyHubTheme {
                MainDashboardScreen(
                    db = db,
                    onLogout = {
                        FirebaseAuth.getInstance().signOut()
                        startActivity(Intent(this, LoginActivity::class.java))
                        finish()
                    }
                )
            }
        }
    }
}

// ---------------------- MAIN SCREEN WITH BOTTOM NAV ----------------------
@Composable
fun MainDashboardScreen(
    db: FirebaseFirestore,
    onLogout: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(0) } // 0 = Dashboard, 1 = Booking History

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .statusBarsPadding()
    ) {
        // Content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (selectedTab) {
                0 -> DashboardScreenContent(db = db, onLogout = onLogout)
                1 -> BookingHistoryScreen(db = db)
            }
        }

        // Bottom navigation
        BottomNavBar(
            selectedTab = selectedTab,
            onTabSelected = { selectedTab = it }
        )
    }
}

// ---------------------- BOTTOM NAV BAR ----------------------
@Composable
fun BottomNavBar(
    selectedTab: Int,
    onTabSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.White)
            .padding(vertical = 10.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        BottomNavItem(
            label = "Dashboard",
            selected = selectedTab == 0,
            onClick = { onTabSelected(0) }
        )
        BottomNavItem(
            label = "Booking History",
            selected = selectedTab == 1,
            onClick = { onTabSelected(1) }
        )
    }
}

@Composable
fun BottomNavItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val textColor = if (selected) Color(0xFF1565C0) else Color.Gray

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 6.dp)
    ) {
        Text(text = label, color = textColor, fontSize = 14.sp, fontWeight = FontWeight.Medium)
    }
}

// ---------------------- DASHBOARD TAB CONTENT ----------------------
@Composable
fun DashboardScreenContent(
    db: FirebaseFirestore,
    onLogout: () -> Unit
) {
    val context = LocalContext.current

    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    var providers by remember { mutableStateOf<List<ServiceProvider>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    // load providers + seed sample into Firestore if empty
    LaunchedEffect(Unit) {
        db.collection("providers")
            .get()
            .addOnSuccessListener { snapshot ->
                if (snapshot.isEmpty) {
                    // Seed sample providers into Firestore
                    sampleProviders.forEach { provider ->
                        db.collection("providers").add(provider)
                    }
                    providers = sampleProviders
                } else {
                    providers = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(ServiceProvider::class.java)?.copy(id = doc.id)
                    }
                }
                isLoading = false
            }
            .addOnFailureListener {
                providers = sampleProviders
                isLoading = false
            }
    }

    val filteredProviders = remember(providers, selectedCategory, searchQuery) {
        providers.filter { p ->
            val matchesCategory = selectedCategory == "All" || p.serviceType == selectedCategory
            val q = searchQuery.lowercase()
            val matchesSearch = q.isEmpty()
                    || p.name.lowercase().contains(q)
                    || p.city.lowercase().contains(q)

            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
    ) {
        // Header + search + filters
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(16.dp)
        ) {
            Column {

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("HandyHub", fontSize = 24.sp, fontWeight = FontWeight.Bold)

                    Text(
                        text = "Logout",
                        color = Color(0xFFB00020),
                        fontSize = 14.sp,
                        modifier = Modifier.clickable { onLogout() }
                    )
                }

                Text(
                    text = "Find trusted home service providers.",
                    fontSize = 14.sp,
                    color = Color.Gray
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or city") },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    FilterChip("All", selectedCategory == "All") { selectedCategory = "All" }
                    FilterChip("Cleaner", selectedCategory == "Cleaner") { selectedCategory = "Cleaner" }
                    FilterChip("Electrician", selectedCategory == "Electrician") { selectedCategory = "Electrician" }
                    FilterChip("Plumber", selectedCategory == "Plumber") { selectedCategory = "Plumber" }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (isLoading) {
            LoadingView()
        } else if (filteredProviders.isEmpty()) {
            EmptyView()
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredProviders) { provider ->
                    ProviderCard(provider) {
                        val intent = Intent(context, ProviderDetailsActivity::class.java).apply {
                            putExtra("name", provider.name)
                            putExtra("serviceType", provider.serviceType)
                            putExtra("city", provider.city)
                            putExtra("rating", provider.rating)
                            putExtra("phoneNumber", provider.phoneNumber)
                        }
                        context.startActivity(intent)
                    }
                }
            }
        }
    }
}

// ---------------------- BOOKING HISTORY TAB ----------------------
@Composable
fun BookingHistoryScreen(
    db: FirebaseFirestore
) {
    val auth = FirebaseAuth.getInstance()
    val userId = auth.currentUser?.uid

    var bookings by remember { mutableStateOf<List<Booking>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(userId) {
        if (userId == null) {
            bookings = emptyList()
            isLoading = false
        } else {
            db.collection("bookings")
                .whereEqualTo("userId", userId)
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener { snapshot ->
                    bookings = snapshot.documents.mapNotNull { doc ->
                        doc.toObject(Booking::class.java)?.copy(id = doc.id)
                    }
                    isLoading = false
                }
                .addOnFailureListener {
                    bookings = emptyList()
                    isLoading = false
                }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp)
    ) {
        Text(
            text = "Booking History",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        if (isLoading) {
            LoadingView()
        } else if (bookings.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No bookings yet.")
            }
        } else {
            LazyColumn {
                items(bookings) { booking ->
                    BookingCard(booking)
                }
            }
        }
    }
}

@Composable
fun BookingCard(booking: Booking) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = booking.providerName,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = booking.providerServiceType,
                fontSize = 14.sp,
                color = Color(0xFF1565C0),
                modifier = Modifier.padding(vertical = 2.dp)
            )
            Text(text = "City: ${booking.providerCity}", fontSize = 13.sp)
            Text(
                text = "Time: ${booking.timestamp}",
                fontSize = 12.sp,
                color = Color.Gray,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// ---------------------- COMMON UI PIECES ----------------------
@Composable
fun LoadingView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Loading...")
    }
}

@Composable
fun EmptyView() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("No providers match your search.")
    }
}

@Composable
fun FilterChip(label: String, selected: Boolean, onClick: () -> Unit) {
    val bg = if (selected) Color(0xFFCDECE1) else Color(0xFFE0F2F1)
    val textColor = if (selected) Color(0xFF1B4332) else Color(0xFF455A64)

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(bg)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Text(label, fontSize = 13.sp, color = textColor)
    }
}

@Composable
fun ProviderCard(provider: ServiceProvider, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(4.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            Text(
                text = provider.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                text = provider.serviceType,
                fontSize = 14.sp,
                color = Color(0xFF1565C0),
                modifier = Modifier.padding(vertical = 4.dp)
            )

            Text("City: ${provider.city}", fontSize = 13.sp)
            Text(
                text = "Rating: ★ ${provider.rating}",
                fontSize = 13.sp,
                color = Color(0xFFFF8F00),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
