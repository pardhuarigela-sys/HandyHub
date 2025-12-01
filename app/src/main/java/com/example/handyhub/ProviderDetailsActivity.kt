package com.example.handyhub

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.handyhub.ui.theme.HandyHubTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ProviderDetailsActivity : ComponentActivity() {

    private val db: FirebaseFirestore by lazy { FirebaseFirestore.getInstance() }
    private val auth: FirebaseAuth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val name = intent.getStringExtra("name") ?: "Provider"
        val serviceType = intent.getStringExtra("serviceType") ?: ""
        val city = intent.getStringExtra("city") ?: ""
        val rating = intent.getDoubleExtra("rating", 0.0)
        val phoneNumber = intent.getStringExtra("phoneNumber") ?: ""

        setContent {
            HandyHubTheme {
                ProviderDetailsScreen(
                    name = name,
                    serviceType = serviceType,
                    city = city,
                    rating = rating,
                    phoneNumber = phoneNumber,
                    onBookAppointment = {
                        saveBooking(name, serviceType, city)
                    }
                )
            }
        }
    }

    private fun saveBooking(
        providerName: String,
        serviceType: String,
        city: String
    ) {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            Toast.makeText(this, "Please log in to book.", Toast.LENGTH_SHORT).show()
            return
        }

        val bookingData = hashMapOf(
            "providerName" to providerName,
            "providerServiceType" to serviceType,
            "providerCity" to city,
            "userId" to userId,
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("bookings")
            .add(bookingData)
            .addOnSuccessListener {
                Toast.makeText(this, "Booking created.", Toast.LENGTH_SHORT).show()
                finish() // go back to dashboard
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to save booking.", Toast.LENGTH_SHORT).show()
            }
    }
}

@Composable
fun ProviderDetailsScreen(
    name: String,
    serviceType: String,
    city: String,
    rating: Double,
    phoneNumber: String,
    onBookAppointment: () -> Unit
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF5F5F5))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        Text(text = name, fontSize = 24.sp, fontWeight = FontWeight.Bold)
        Text(text = serviceType, fontSize = 16.sp, color = Color(0xFF1565C0))
        Text(text = "City: $city", fontSize = 14.sp)
        Text(text = "Rating: ★ $rating", fontSize = 14.sp)

        Spacer(modifier = Modifier.height(24.dp))

        // Book appointment button -> saves to Firestore
        Button(
            onClick = { onBookAppointment() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Book Appointment")
        }

        // Call provider button
        Button(
            onClick = {
                if (phoneNumber.isNotBlank()) {
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phoneNumber")
                    }
                    context.startActivity(dialIntent)
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Call Provider")
        }
    }
}
