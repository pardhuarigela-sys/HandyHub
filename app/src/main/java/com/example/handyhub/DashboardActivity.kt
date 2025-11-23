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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.windowInsetsPadding
import com.example.handyhub.ui.theme.HandyHubTheme
import com.google.firebase.auth.FirebaseAuth

data class ServiceProvider(
    val id: String = "",
    val name: String = "",
    val serviceType: String = "",
    val rating: Double = 0.0,
    val city: String = ""
)

class DashboardActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            HandyHubTheme {
                DashboardScreen(
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

// SAMPLE DATA
private val sampleProviders = listOf(
    ServiceProvider("1", "Clean & Shine Services", "Cleaner", 4.5, "Middlesbrough"),
    ServiceProvider("2", "SparkPro Electricians", "Electrician", 4.8, "Newcastle"),
    ServiceProvider("3", "QuickFix Plumbing", "Plumber", 4.3, "Leeds"),
    ServiceProvider("4", "HomeCare Cleaning", "Cleaner", 4.1, "York")
)

@Composable
fun DashboardScreen(
    onLogout: () -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }
    var searchQuery by remember { mutableStateOf("") }

    // FILTERED LIST
    val filteredProviders = remember(selectedCategory, searchQuery) {
        sampleProviders.filter { provider ->
            val matchesCategory =
                selectedCategory == "All" || provider.serviceType == selectedCategory

            val q = searchQuery.trim().lowercase()
            val matchesSearch = q.isEmpty() ||
                    provider.name.lowercase().contains(q) ||
                    provider.city.lowercase().contains(q)

            matchesCategory && matchesSearch
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .statusBarsPadding()   //
    ) {

        // ---------------- HEADER ----------------
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Column {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "HandyHub",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "Logout",
                        fontSize = 14.sp,
                        color = Color(0xFFB00020),
                        modifier = Modifier.clickable { onLogout() }
                    )
                }

                Text(
                    text = "Find trusted home service providers.",
                    fontSize = 14.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(bottom = 12.dp)
                )

                // ---------------- SEARCH ----------------
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search by name or city") },
                    singleLine = true,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(12.dp))

                // ---------------- FILTERS ----------------
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

        // ---------------- PROVIDER LIST ----------------
        if (filteredProviders.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No providers match your search.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(filteredProviders) { provider ->
                    ProviderCard(provider)
                }
            }
        }
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
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(text = label, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = textColor)
    }
}

@Composable
fun ProviderCard(provider: ServiceProvider) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
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
                modifier = Modifier.padding(top = 2.dp, bottom = 4.dp)
            )

            Text(text = "City: ${provider.city}", fontSize = 13.sp)

            Text(
                text = "Rating: ★ ${provider.rating}",
                fontSize = 13.sp,
                color = Color(0xFFFF8F00),
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}
