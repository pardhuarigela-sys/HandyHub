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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
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

// Sample data for now (no Firestore)
private val sampleProviders = listOf(
    ServiceProvider(
        id = "1",
        name = "Clean & Shine Services",
        serviceType = "Cleaner",
        rating = 4.5,
        city = "Middlesbrough"
    ),
    ServiceProvider(
        id = "2",
        name = "SparkPro Electricians",
        serviceType = "Electrician",
        rating = 4.8,
        city = "Newcastle"
    ),
    ServiceProvider(
        id = "3",
        name = "QuickFix Plumbing",
        serviceType = "Plumber",
        rating = 4.3,
        city = "Leeds"
    )
)

@Composable
fun DashboardScreen(
    onLogout: () -> Unit
) {
    // Use sample list for now
    val providers = remember { sampleProviders }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Top bar (title + logout)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
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
            modifier = Modifier.padding(bottom = 16.dp)
        )

        // Category chips (static for now)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            CategoryChip("Cleaner")
            CategoryChip("Electrician")
            CategoryChip("Plumber")
        }

        // Simple list using sample data
        if (providers.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text("No providers available.")
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize()
            ) {
                items(providers) { provider ->
                    ProviderCard(provider = provider)
                }
            }
        }
    }
}

@Composable
fun CategoryChip(text: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xFFE0F2F1))
            .padding(horizontal = 12.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun ProviderCard(provider: ServiceProvider) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = provider.name,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = provider.serviceType,
                fontSize = 14.sp,
                color = Color(0xFF1565C0)
            )
            Text(
                text = "City: ${provider.city}",
                fontSize = 13.sp
            )
            Text(
                text = "Rating: ${provider.rating}",
                fontSize = 13.sp,
                color = Color(0xFFFF8F00)
            )
        }
    }
}
