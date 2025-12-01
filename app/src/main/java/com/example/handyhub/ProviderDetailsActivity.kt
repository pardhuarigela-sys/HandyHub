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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.statusBarsPadding
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
                finish()
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
            .statusBarsPadding()          // ✅ pushes content below notch
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Spacer(modifier = Modifier.height(16.dp))

        // Optional small title
        Text(
            text = "Provider Details",
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.Gray,
            modifier = Modifier
                .fillMaxWidth(),
            textAlign = TextAlign.Start
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Info card
        Card(
            modifier = Modifier
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color.White
            ),
            elevation = CardDefaults.cardElevation(4.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = name,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = serviceType,
                    fontSize = 16.sp,
                    color = Color(0xFF1565C0),
                    fontWeight = FontWeight.Medium
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(text = "City: $city", fontSize = 14.sp)
                Text(text = "Rating: ★ $rating", fontSize = 14.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Book appointment button
        Button(
            onClick = { onBookAppointment() },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF1565C0),
                contentColor = Color.White
            )
        ) {
            Text("Book Appointment", fontSize = 16.sp)
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Call provider button
        Button(
            onClick = {
                if (phoneNumber.isNotBlank()) {
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phoneNumber")
                    }
                    context.startActivity(dialIntent)
                } else {
                    Toast.makeText(context, "Phone number not available", Toast.LENGTH_SHORT).show()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(24.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF455A64),
                contentColor = Color.White
            )
        ) {
            Text("Call Provider", fontSize = 16.sp)
        }
    }
}
